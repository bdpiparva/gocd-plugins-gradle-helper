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

import cd.go.plugin.gradlehelper.models.ConfigurationInfo
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

class ConfigurationReader {

    void read(Project project, Set<Configuration> toReport) {
        def each = toReport.collect { configToReport ->
            ConfigurationInfo configurationInfo = new ConfigurationInfo(configToReport.name)

            if (configToReport.hasProperty("canBeResolved") && !configToReport) {
                return configurationInfo
            }

            configuration.resolvedConfiguration // force configuration resolution

//            Set<ResolvedDependency> dependencies = new TreeSet<ResolvedDependency>(new ResolvedDependencyComparator())
//            for (ResolvedDependency dependency : configuration.resolvedConfiguration.getFirstLevelModuleDependencies()) {
//                collectDependencies(dependencies, dependency)
//            }
        }
    }

    static Set<Configuration> toLibraryInfo(Set<Configuration> toReport) {
        def each = toReport.collect { configToReport ->
            return
        }
        for (Configuration configuration : toReport) {
//            data.configurations.add(configurationReader.read(project, configuration))
        }
    }
}
