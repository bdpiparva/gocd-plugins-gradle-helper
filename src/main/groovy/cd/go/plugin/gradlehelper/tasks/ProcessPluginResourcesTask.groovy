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

class ProcessPluginResourcesTask extends AbstractTask {

    @Override
    void doTaskAction() {
        def engine = new GStringTemplateEngine()

        logger.info pluginInfo.toHash().toString()

        [new PluginXML(), new PluginProperties()].each { template ->
            def expandedContent = engine.createTemplate(template.content()).make(pluginInfo.toHash().withDefault { '' })
            def pluginResourceFile = new File(project.rootProject.projectDir.absolutePath + "/src/main/resources", template.name())
            logger.info "Writing plugin resource file ${pluginResourceFile.absolutePath}"
            pluginResourceFile.write(expandedContent.toString())
        }
    }
}
