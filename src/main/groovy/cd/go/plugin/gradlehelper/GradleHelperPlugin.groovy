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
import cd.go.plugin.gradlehelper.tasks.GitHubReleaseTask
import cd.go.plugin.gradlehelper.tasks.ProcessPluginResourcesTask
import cd.go.plugin.gradlehelper.tasks.PublishToS3Task
import com.github.jk1.license.LicenseReportPlugin
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar

class GradleHelperPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.with {
            apply LicenseReportPlugin
            apply JavaPlugin
        }
        GradleHelperExtension gocdPlugin = project.extensions.create('gocdPlugin', GradleHelperExtension, project)
        def processPluginResourcesTask = project.tasks.create('processPluginResources', ProcessPluginResourcesTask)
        project.tasks.create('githubRelease', GitHubReleaseTask)
        project.tasks.create('publishToS3', PublishToS3Task)

        project.afterEvaluate {
            //TODO: Move it to doFirst if possible
            initLicenseReportPlugin(project, gocdPlugin.licenseReport)
            gocdPlugin.pluginInfo.validate()
            processPluginResourcesTask.dependsOn('generateLicenseReport')
            project.tasks.compileJava.dependsOn(processPluginResourcesTask)
            handlePluginJar(project, gocdPlugin)

            setupProjectVersion(project, gocdPlugin)
            configurePrettyTestLogging(project, gocdPlugin)
            showCompilationWarnings(project, gocdPlugin)
        }

    }

    private DomainObjectCollection<Jar> handlePluginJar(Project project, gocdPlugin) {
        project.tasks.withType(Jar) { jarTask ->
            jarTask.from(project.configurations.compile) {
                jarTask.into "lib/"
            }

            ['MD5', 'SHA1', 'SHA-256'].each { algo ->
                jarTask.outputs.files("${jarTask.archivePath}.${algo}")
                jarTask.doLast {
                    jarTask.ant.checksum file: jarTask.archivePath, format: 'MD5SUM', algorithm: algo
                }
            }

            jarTask.manifest {
                attributes(
                        'Go-Version': gocdPlugin.pluginInfo.serverVersion,
                        'Plugin-Revision': gocdPlugin.pluginInfo.version,
                        'Implementation-Title': gocdPlugin.pluginInfo.name,
                        'Implementation-Version': gocdPlugin.pluginInfo.fullVersion,
                        'Source-Compatibility': project.sourceCompatibility,
                        'Target-Compatibility': project.targetCompatibility
                )
            }
        }
    }

    private void initLicenseReportPlugin(Project project, LicenseReportPlugin.LicenseReportExtension licenseReport) {
        project.licenseReport {
            filters = licenseReport.filters
            excludes = licenseReport.excludes
            renderer = licenseReport.renderer
            outputDir = licenseReport.outputDir
            importers = licenseReport.importers
            excludeGroups = licenseReport.excludeGroups
            configurations = licenseReport.configurations
        }
    }

    private static void showCompilationWarnings(Project project, GradleHelperExtension gocdPlugin) {
        if (gocdPlugin.showJavaCompilationWarnings) {
            project.tasks.withType(JavaCompile) { java ->
                java.options.deprecation = true
                java.options.warnings = true
                java.options.encoding = 'utf-8'
                java.options.compilerArgs << "-Xlint:all"
                java.options.compilerArgs << "-Xlint:-serial"
                java.logger
            }
        }
    }

    private static void setupProjectVersion(Project project, GradleHelperExtension gocdPlugin) {
        if (GitInfoProvider.isGitRepo(project.projectDir.absolutePath)) {
            project.version = "${gocdPlugin.pluginInfo.version}-${GitInfoProvider.gitRevisionCount(project.projectDir.absolutePath)}"
        } else {
            project.version = gocdPlugin.pluginInfo.version
        }
    }

    private static void configurePrettyTestLogging(Project project, GradleHelperExtension gocdPlugin) {
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
