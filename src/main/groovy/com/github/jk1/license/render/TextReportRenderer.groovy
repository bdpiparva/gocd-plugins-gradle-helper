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

import com.github.jk1.license.*
import org.gradle.api.Project

class TextReportRenderer implements ReportRenderer{

    private Project project
    private LicenseReport config
    private File output
    private int counter
    private String fileName

    public TextReportRenderer(String filename = 'THIRD-PARTY-NOTICES.txt') {
        this.fileName = filename
    }

    void render(ProjectData data) {
        project = data.project
        config = project.gocdPlugin.licenseReport
        output = new File(config.outputDir, fileName)
        output.text = """
Dependency License Report for $project.name

Dependency License Report for $project.name ${if (!'unspecified'.equals(project.version)) project.version else ''}

"""
        printDependencies(data)
        output << """
This report was generated at ${new Date()}.

"""
    }

    private void printDependencies(ProjectData data) {
        data.allDependencies.sort().each {
            printDependency(it)
        }
    }

    private String printDependency(ModuleData data) {
        boolean projectUrlDone = false
        output << "${++counter}."
        if (data.group) output << " Group: $data.group "
        if (data.name) output << " Name: $data.name "
        if (data.version) output << " Version: $data.version\n\n"

        if (data.poms.isEmpty() && data.manifests.isEmpty()) {
            output << "No license information found\n\n"
            return
        }

        if (!data.manifests.isEmpty() && !data.poms.isEmpty()) {
            ManifestData manifest = data.manifests.first()
            PomData pomData = data.poms.first()
            if (manifest.url && pomData.projectUrl && manifest.url == pomData.projectUrl) {
                output << "Project URL: $manifest.url\n\n"
                projectUrlDone = true
            }
        }

        if (!data.manifests.isEmpty()) {
            ManifestData manifest = data.manifests.first()
            if (manifest.url && !projectUrlDone) {
                output << "Manifest Project URL: $manifest.url\n\n"
            }
            if (manifest.license) {
                if (manifest.license.startsWith("http")) {
                    output << "Manifest license URL: $manifest.license\n\n"
                } else if (manifest.hasPackagedLicense) {
                    output << "Packaged License File: $manifest.license\n\n"
                } else {
                    output << "Manifest License: $manifest.license (Not packaged)\n\n"
                }
            }
        }

        if (!data.poms.isEmpty()) {
            PomData pomData = data.poms.first()
            if (pomData.projectUrl && !projectUrlDone) {
                output << "POM Project URL: $pomData.projectUrl\n\n"
            }
            if (pomData.licenses) {
                pomData.licenses.each { License license ->
                    output << "POM License: $license.name"
                    if (license.url) {
                        if (license.url.startsWith("http")) {
                            output << " - $license.url\n\n"
                        } else {
                            output << "License: $license.url\n\n"
                        }
                    }
                }
            }
        }
        if (!data.licenseFiles.isEmpty() && !data.licenseFiles.first().fileDetails.isEmpty()) {
            output << 'Embedded license: '
            output << "\n\n"
            output << data.licenseFiles.first().fileDetails.collect({ "                    ****************************************                    \n\n" + new File("$config.outputDir/$it.file").text + "\n"}).join('')
        }
        output << "--------------------------------------------------------------------------------\n\n"
    }
}

