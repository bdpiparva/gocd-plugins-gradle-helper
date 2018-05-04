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

import cd.go.plugin.gradlehelper.models.GitHubReleaseInfo
import cd.go.plugin.gradlehelper.models.PluginInfo
import cd.go.plugin.gradlehelper.models.S3Config
import groovy.json.JsonBuilder
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project

@CompileStatic
class GradleHelperExtension {
    boolean prettyTestOutput = true
    boolean showJavaCompilationWarnings = true
    final PluginInfo pluginInfo
    final GitHubReleaseInfo github
    final S3Config s3

    @javax.inject.Inject
    GradleHelperExtension(Project project) {
        pluginInfo = new PluginInfo(project)
        github = new GitHubReleaseInfo()
        s3 = new S3Config()
    }

    void pluginInfo(Action<? super PluginInfo> action) {
        action.execute(pluginInfo)
    }

    void github(Action<? super GitHubReleaseInfo> action) {
        action.execute(github)
    }

    void s3(Action<? super S3Config> action) {
        action.execute(s3)
    }

    @Override
    String toString() {
        return new JsonBuilder(
                ['prettyTestOutput': prettyTestOutput,
                 'pluginInfo'      : pluginInfo.toHash()
                ]).toPrettyString()
    }
}