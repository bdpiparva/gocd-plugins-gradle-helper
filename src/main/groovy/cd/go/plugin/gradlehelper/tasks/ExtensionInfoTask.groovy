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

import cd.go.plugin.gradlehelper.GitInfoProvider
import groovy.text.GStringTemplateEngine

class ExtensionInfoTask extends AbstractTask {

    @Override
    void doTaskAction() {
        URL[] fileURLs = [
                getClass().getResource('/plugin.xml'),
                getClass().getResource('/plugin.properties')
        ]

        def engine = new GStringTemplateEngine()

        Map<String, String> pluginInfo = addGitInfo()
        fileURLs.each { fileUrl ->
            def template = engine.createTemplate(fileUrl).make(pluginInfo.withDefault { '' })
            new File(resourceOutDir, new File(fileUrl.file).getName()).write(template.toString())
        }
    }

    private Map<String, String> addGitInfo() {
        Map<String, String> config = pluginInfo.toHash()
        if (!GitInfoProvider.isGitRepo(projectDirPath)) {
            return config
        }

        String revision = GitInfoProvider.gitRevision(projectDirPath)
        String distVersion = GitInfoProvider.gitRevisionCount(projectDirPath)

        config += [
                'distVersion'  : distVersion,
                'gitRevision'  : revision == null ? '' : revision,
                "fullVersion"  : project.version,
                'pluginVersion': pluginInfo.version,
        ]

        config
    }
}
