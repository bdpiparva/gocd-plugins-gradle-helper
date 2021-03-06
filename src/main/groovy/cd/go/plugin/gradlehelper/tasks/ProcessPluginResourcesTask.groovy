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

package cd.go.plugin.gradlehelper.tasks

import cd.go.plugin.gradlehelper.template.PluginProperties
import cd.go.plugin.gradlehelper.template.PluginXML
import groovy.text.GStringTemplateEngine
import org.apache.commons.io.FileUtils

class ProcessPluginResourcesTask extends AbstractTask {

    @Override
    void doTaskAction() {
        FileUtils.copyFile(new File("${project.buildDir}/reports/dependency-license", "NOTICE.txt"), new File(resourceOutDir, 'NOTICE.txt'))
        FileUtils.copyFile(new File("${project.buildDir}/reports/dependency-license", "index.html"), new File(resourceOutDir, 'license-report.html'))

        def engine = new GStringTemplateEngine()
        [new PluginXML(), new PluginProperties()].each { template ->
            def expandedContent = engine.createTemplate(template.content()).make(pluginInfo.toHash().withDefault { '' })
            def pluginResourceFile = new File(resourceOutDir, template.name())
            logger.info "Writing plugin resource file ${pluginResourceFile.absolutePath}"
            pluginResourceFile.write(expandedContent.toString())
        }
    }
}
