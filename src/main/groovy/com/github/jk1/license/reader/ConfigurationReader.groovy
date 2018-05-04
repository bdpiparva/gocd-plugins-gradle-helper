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

package com.github.jk1.license.reader

import com.github.jk1.license.ConfigurationData
import com.github.jk1.license.LicenseReport
import com.github.jk1.license.ReportTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging


class ConfigurationReader {

    private Logger LOGGER = Logging.getLogger(ReportTask.class)
    private ModuleReader moduleReader = new ModuleReader()
    private LicenseReport config

    ConfigurationData read(Project project, Configuration configuration) {
        config = project.gocdPlugin.licenseReport

        ConfigurationData data = new ConfigurationData()
        data.name = configuration.name

        if ( configuration.hasProperty("canBeResolved") && !configuration.canBeResolved) {
            LOGGER.info("Skipping configuration [$configuration] as it can't be resolved")
            return data
        }

        LOGGER.info("Processing configuration [$configuration], configuration will be resolved")
        configuration.resolvedConfiguration // force configuration resolution

        Set<ResolvedDependency> dependencies = new TreeSet<ResolvedDependency>(new ResolvedDependencyComparator())
        for (ResolvedDependency dependency : configuration.resolvedConfiguration.getFirstLevelModuleDependencies()) {
            collectDependencies(dependencies, dependency)
        }
        LOGGER.info("Processing dependencies for configuration [$configuration]: " + dependencies.join(','))
        for (ResolvedDependency dependency : dependencies) {
            LOGGER.debug("Processing dependency: $dependency")
            data.dependencies.add(moduleReader.read(project, dependency))
        }
        return data
    }

    private Set<ResolvedDependency> collectDependencies(Set<ResolvedDependency> accumulator, ResolvedDependency root){
        // avoiding dependency cycles
        if (!accumulator.contains(root)) {
            LOGGER.debug("Collecting dependency ${root.name}")
            if (!config.isExcluded(root)) {
                LOGGER.debug("Not collecting dependency ${root.name} due to explicit exclude configured")
                accumulator.add(root)
            }
            root.children.each {collectDependencies(accumulator, it)}
        }
        accumulator
    }

    private static class ResolvedDependencyComparator implements Comparator<ResolvedDependency>{
        @Override
        int compare(ResolvedDependency first, ResolvedDependency second) {
            first.moduleGroup <=> second.moduleGroup ?:
                first.moduleName <=> second.moduleName
        }
    }
}
