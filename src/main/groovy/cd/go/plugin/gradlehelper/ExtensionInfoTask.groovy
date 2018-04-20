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

package cd.go.plugin.gradlehelper

import groovy.text.GStringTemplateEngine
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class ExtensionInfoTask extends DefaultTask {

    @TaskAction
    void perform() {
        URL[] fileNames = [
                getClass().getResource('/plugin.xml'),
                getClass().getResource('/plugin.properties')
        ]

        def engine = new GStringTemplateEngine()

        File resourceDir = new File(project.buildDir.absolutePath, 'resources/main/')
        if (!resourceDir.exists()) {
            resourceDir.mkdirs()
        }

        Map<String, String> pluginInfo = addGitInfo()
        fileNames.each { f ->
            def template = engine.createTemplate(f).make(pluginInfo.withDefault { '' })
            new File(resourceDir, new File(f.file).getName()).write(template.toString())
        }

    }

    private Map<String, String> addGitInfo() {
        Map<String, String> config = getProject().gocdPlugin.pluginInfo.toHash()
        if (!GitInfoProvider.isGitRepo(project.projectDir.absolutePath)) {
            return config
        }

        String revision = GitInfoProvider.gitRevision(project.projectDir.absolutePath)
        String distVersion = GitInfoProvider.gitRevisionCount(project.projectDir.absolutePath)

        config += [
                'distVersion'  : distVersion,
                'gitRevision'  : revision == null ? '' : revision,
                "fullVersion"  : project.version,
                'pluginVersion': project.version,
        ]

        config
    }

}
