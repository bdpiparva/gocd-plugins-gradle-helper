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

package cd.go.plugin.gradlehelper.license_report

import cd.go.plugin.gradlehelper.models.ConfigurationInfo
import cd.go.plugin.gradlehelper.models.LicenseInfo
import cd.go.plugin.gradlehelper.models.LicenseReport
import cd.go.plugin.gradlehelper.models.ModuleInfo
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedDependency

class LiceseReader {
    public static final String CAN_BE_RESOLVED = "canBeResolved"
    private final LicenseInfo licenseInfo
    private final Project rootProject
    private final LicenseReport licenseReport

    LiceseReader(Project rootProject) {
        this.rootProject = rootProject
        this.licenseInfo = new LicenseInfo(rootProject)
        this.licenseReport = rootProject.gocdPlugin.licenseReport
    }

    void read() {
        Set<Configuration> configurations = rootProject.allprojects.each { p ->
            p.configurations.findAll { c -> return resolveOrBomb(c) }
        } as Set<Configuration>

        configurations.addAll(extendedConfig(configurations))

        readConfigurations(configurations)
    }

    void readConfigurations(Set<Configuration> configurations) {
        configurations.each { c ->
            Set<ResolvedDependency> resolvedDependencies = new HashSet<>()
            ConfigurationInfo configurationInfo = licenseInfo.createConfigData(c.name)
            c.resolvedConfiguration.getFirstLevelModuleDependencies().each { dependency ->
                collectResolvedDependency(resolvedDependencies, dependency)
            }

            readModules(configurationInfo, resolvedDependencies)
        }
    }

    def readModules(ConfigurationInfo configurationInfo, Set<ResolvedDependency> dependencies) {
        dependencies.each { d ->
            ModuleInfo moduleInfo = configurationInfo.addModuleInfo(d.moduleGroup, d.moduleName, d.moduleVersion)
            d.moduleArtifacts.each { artifact ->
                if (artifact.file.exists()) {

                }
            }
        }
    }

    private void collectResolvedDependency(Set<ResolvedDependency> resolvedDependencies, ResolvedDependency dependency) {
        if (!resolvedDependencies.contains(dependency)) {
            resolvedDependencies.add(dependency)
            dependency.children.each { child -> collectResolvedDependency(resolvedDependencies, child) }
        }
    }

    private Set<Configuration> extendedConfig(Set<Configuration> configurations) {
        configurations.collect { it.extendsFrom }
                .flatten()
                .findAll { isResolvable(it as Configuration) } as Set<Configuration>
    }

    private boolean resolveOrBomb(Configuration c) {
        if (c.name in licenseReport.configurations) {
            if (isResolvable(c)) {
                return true
            } else {
                throw new GradleException("Failed to resolve configuration$c")
            }
        }
    }

    private static boolean isResolvable(Configuration configuration) {
        configuration.hasProperty(CAN_BE_RESOLVED) &&
                configuration.canBeResolved
    }
}
