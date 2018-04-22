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

package cd.go.plugin.gradlehelper.license_report.readers

import cd.go.plugin.gradlehelper.models.ProjectInfo
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

class ProjectLibReader {
    private static final ConfigurationReader configurationReader = new ConfigurationReader()

    void read(Project project) {
        ProjectInfo projectInfo = new ProjectInfo(
                project,
                configurationReader.read(project,configurationToReport(project))
        )
    }

    private static Set<Configuration> configurationToReport(Project project) {
        Set<Configuration> toReport = project.getConfigurations().matching { configuration ->
            if (containsIgnoreCase(project, configuration.name)) {
                if (configuration.hasProperty("canBeResolved") && !configuration.canBeResolved) {
                    throw new GradleException("Project Library Reader : " +
                            "The specified configuration \"${configuration.name}\" can't be resolved. " +
                            "Try specifying a more specific configuration by adding flavor(s) and/or build type.")
                }
                return true
            }
            return false
        }

        addNestedConfiguration(toReport)

        return toReport
    }

    private static Set<Configuration> addNestedConfiguration(Set<Configuration> configurations) {
        for (int previousRoundSize = 0; configurations.size() != previousRoundSize; previousRoundSize = configurations.size()) {
            for (Configuration configuration : new ArrayList<Configuration>(configurations)) {
                configurations.addAll(configuration.getExtendsFrom())
            }
        }
    }

    private static boolean containsIgnoreCase(Project project, String configurationName) {
        for (String configuration : project.extensions.gocdPlugin.licenseReport.configurations) {
            if (configuration.equalsIgnoreCase(configurationName)) {
                return true
            }
        }
        return false
    }
}
