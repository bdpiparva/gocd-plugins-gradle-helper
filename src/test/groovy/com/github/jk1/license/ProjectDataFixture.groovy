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

package com.github.jk1.license

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class ProjectDataFixture {
    private static Project project = null
    static def GRADLE_PROJECT() {
        if (project == null) {
            project = ProjectBuilder.builder().withName("my-project").build()
            project.pluginManager.apply 'cd.go.plugin.gradlehelper'
        }
        project
    }

    static License APACHE2_LICENSE() {
        new License(
            name: "Apache License, Version 2.0",
            url: "https://www.apache.org/licenses/LICENSE-2.0",
            distribution: "repo",
            comments: "A business-friendly OSS license"
        )
    }
    static License MIT_LICENSE() {
        new License(
            name: "MIT License",
            url: "https://opensource.org/licenses/MIT",
            distribution: "repo",
            comments: "A short and simple permissive license"
        )
    }
    static License LGPL_LICENSE() {
        new License(
            name: "GNU LESSER GENERAL PUBLIC LICENSE, Version 3",
            url: "https://www.gnu.org/licenses/lgpl-3.0",
            distribution: "repo",
            comments: "A weak copyleft license"
        )
    }
}
