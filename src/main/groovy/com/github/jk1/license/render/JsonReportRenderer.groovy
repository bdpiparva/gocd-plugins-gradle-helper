/*
 * Copyright 2018 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.jk1.license.render

import com.github.jk1.license.ImportedModuleBundle
import com.github.jk1.license.LicenseReport
import com.github.jk1.license.ModuleData
import com.github.jk1.license.ProjectData
import groovy.json.JsonBuilder
import org.gradle.api.Project

/**
 *
 * This renderer has two modes:  single-license-per-module  and  all-licenses-per-module.
 * The mode can be controlled with the constructor parameter  onlyOneLicensePerModule   and depending on
 * the mode, the result looks differently:
 *
 * single-license-per-module
 * =========================
 * Renders a simply structured JSON dependency report
 *
 *  {
 *  "dependencies": [
 *   {
 *      "moduleName": "...",
 *      "moduleUrl": "...",
 *      "moduleVersion": "...",
 *      "moduleLicense": "...",
 *      "moduleLicenseUrl": "...",
 *   }, ...],
 *  "importedModules": [
 *   {
 *       "name": "...",
 *       "dependencies": [
 *           "moduleName": "...",
 *           "moduleUrl": "...",
 *           "moduleVersion": "...",
 *           "moduleLicense": "...",
 *           "moduleLicenseUrl": "..."
 *       ]
 *   }, ...]
 * }
 *
 *
 * all-licenses-per-module
 * =======================
 * Renders a structured JSON with all licenses per module
 *
 *  {
 *  "dependencies": [
 *   {
 *      "moduleName": "...",
 *      "moduleVersion": "...",
 *      "moduleUrls": [ "..." ],
 *      "moduleLicenses": [
 *          {
 *              "moduleLicense": "...",
 *              "moduleLicenseUrl": "..."
 *          }, ... ]
 *   }, ...],
 *  "importedModules": [
 *   {
 *       "name": "...",
 *       "dependencies": [
 *           "moduleName": "...",
 *           "moduleVersion": "...",
 *           "moduleUrl": "...",
 *           "moduleLicense": "...",
 *           "moduleLicenseUrl": "..."
 *       ]
 *   }, ...]
 * }
 *
 */

class JsonReportRenderer implements ReportRenderer {

    private String fileName
    private Project project
    private LicenseReport config
    private File output
    private Boolean onlyOneLicensePerModule

    JsonReportRenderer(String fileName = 'index.json', boolean onlyOneLicensePerModule = true) {
        this.fileName = fileName
        this.onlyOneLicensePerModule = onlyOneLicensePerModule
    }

    void render(ProjectData data) {
        project = data.project
        config = project?.gocdPlugin.licenseReport
        output = new File(config.outputDir, fileName)

        def jsonReport = [:]

        if (onlyOneLicensePerModule) {
            jsonReport.dependencies = renderSingleLicensePerModule(data.allDependencies)
        } else {
            jsonReport.dependencies = renderAllLicensesPerModule(data.allDependencies)
        }
        jsonReport.importedModules = readImportedModules(data.importedModules)

        output.text = new JsonBuilder(trimAndRemoveNullEntries(jsonReport)).toPrettyString()
    }

    def renderSingleLicensePerModule(Collection<ModuleData> allDependencies) {
        allDependencies.collect {
            String moduleName = "${it.group}:${it.name}"
            String moduleVersion = it.version
            def (String moduleUrl, String moduleLicense, String moduleLicenseUrl) = com.github.jk1.license.render.LicenseDataCollector.singleModuleLicenseInfo(it)
            trimAndRemoveNullEntries([moduleName      : moduleName,
                                      moduleUrl       : moduleUrl,
                                      moduleVersion   : moduleVersion,
                                      moduleLicense   : moduleLicense,
                                      moduleLicenseUrl: moduleLicenseUrl])
        }.sort { it.moduleName }
    }

    def renderAllLicensesPerModule(Collection<ModuleData> allDependencies) {
        allDependencies.collect {
            String moduleName = "${it.group}:${it.name}"
            String moduleVersion = it.version
            def info = com.github.jk1.license.render.LicenseDataCollector.multiModuleLicenseInfo(it)

            def jsonLicenseList = info.licenses.collect {
                [moduleLicense: it.name, moduleLicenseUrl: it.url]
            }

            trimAndRemoveNullEntries([moduleName    : moduleName,
                                      moduleVersion : moduleVersion,
                                      moduleUrls    : info.moduleUrls,
                                      moduleLicenses: jsonLicenseList])
        }.sort { it.moduleName }
    }

    static def readImportedModules(def incModules) {
        incModules.collect { ImportedModuleBundle importedModuleBundle ->
            trimAndRemoveNullEntries([moduleName  : importedModuleBundle.name,
                                      dependencies: readModuleDependencies(importedModuleBundle.modules)])
        }.sort { it.moduleName }
    }

    static def readModuleDependencies(def modules) {
        modules.collectEntries {
            trimAndRemoveNullEntries([moduleName      : it.name,
                                      moduleUrl       : it.projectUrl,
                                      moduleVersion   : it.version,
                                      moduleLicense   : it.license,
                                      moduleLicenseUrl: it.licenseUrl])
        }
    }

    static def trimAndRemoveNullEntries(def map) {
        map.collectEntries { k, v ->
            v ? [(k): v instanceof String ? v.trim() : v] : [:]
        }
    }
}
