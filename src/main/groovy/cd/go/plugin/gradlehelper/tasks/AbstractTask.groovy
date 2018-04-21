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

import cd.go.plugin.gradlehelper.GradleHelperExtension
import cd.go.plugin.gradlehelper.models.PluginInfo
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class AbstractTask extends DefaultTask {
    protected GradleHelperExtension extension
    protected PluginInfo pluginInfo
    protected String buildDirPath
    protected String projectDirPath
    protected File resourceOutDir

    @TaskAction
    void taskAction() {
        this.extension = project.extensions.gocdPlugin
        this.pluginInfo = extension.pluginInfo

        this.projectDirPath = project.projectDir.absolutePath
        this.buildDirPath = project.buildDir.absolutePath
        this.resourceOutDir = new File(buildDirPath, 'resources/main/')

        if (!resourceOutDir.exists()) {
            resourceOutDir.mkdirs()
        }

        doTaskAction()
    }

    abstract void doTaskAction()
}
