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

import cd.go.plugin.gradlehelper.pretty_test.PrettyTestLogger
import cd.go.plugin.gradlehelper.tasks.ExtensionInfoTask
import cd.go.plugin.gradlehelper.tasks.GitHubReleaseTask
import cd.go.plugin.gradlehelper.tasks.HelloTask
import com.github.jk1.license.LicenseReportPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.testing.Test

class GradleHelperPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.apply(LicenseReportPlugin.class)
        project.pluginManager.apply(JavaPlugin.class)
        project.extensions.create('gocdPlugin', GradleHelperExtension, project.objects, project)

        project.tasks.create('extensionInfo', ExtensionInfoTask)
        project.tasks.create('githubRelease', GitHubReleaseTask)
        project.tasks.create('hello', HelloTask)

        project.afterEvaluate {
            //TODO: Move it to doFirst of possible
            GradleHelperExtension gocdPlugin = project.extensions.gocdPlugin
            gocdPlugin.pluginInfo.validate()

            if (GitInfoProvider.isGitRepo(project.projectDir.absolutePath)) {
                project.version = "${gocdPlugin.pluginInfo.version}-${GitInfoProvider.gitRevisionCount(project.projectDir.absolutePath)}"
            } else {
                project.version = gocdPlugin.pluginInfo.version
            }

            if (gocdPlugin.prettyTestOutput) {
                project.tasks.withType(Test) { test ->
                    if (project.plugins.findPlugin('java')) {
                        test.testLogging.minGranularity = -2
                    }
                    test.addTestListener(new PrettyTestLogger())
                    test.addTestOutputListener(new PrettyTestLogger())
                }
            }
        }
    }
}
