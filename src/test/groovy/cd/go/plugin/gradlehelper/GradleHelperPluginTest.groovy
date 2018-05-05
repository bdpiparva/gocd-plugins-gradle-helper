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

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.internal.PluginUnderTestMetadataReading
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class GradleHelperPluginTest extends Specification {
    File testProjectDir = new File("build/test-project")
    File buildFile
    List<File> pluginClasspath

    void setup() {
        testProjectDir.deleteDir()
        testProjectDir.mkdirs()
        pluginClasspath = buildPluginClasspathWithTestClasspath()
        buildFile = new File(testProjectDir, "build.gradle")
        buildFile << """
            plugins {
                id 'cd.go.plugin.gradlehelper'
            }
            
            configurations {
                forTesting
            }
            repositories {
                mavenCentral()
            }

            gocdPlugin {
                pluginInfo {
                    id = 'cd.go.scm.github'
                    name = "GitHub plugin"
                    version = '0.0.1'
                    group = 'cd.go'
                    serverVersion = '18.2.0'
                }
                
                licenseReport {
                    configurations = ['forTesting'] 
                }
            }
        """
    }

    def "should generate license report"() {
        given:
        buildFile << """
            dependencies {
                forTesting "org.jetbrains:annotations:13.0"     // license-name: "The Apache Software License, Version 2.0"
                forTesting "io.netty:netty-common:4.1.17.Final" // license-name: "Apache License, Version 2.0"
                forTesting group: 'cd.go.plugin', name: 'go-plugin-api', version: '18.2.0'
                forTesting group: 'com.google.code.gson', name: 'gson', version: '2.8.2'
                forTesting group: 'org.apache.commons', name: 'commons-lang3', version: '3.7'
                forTesting group: 'com.google.guava', name: 'guava', version: '24.0-jre'
            }
        """

        when:
        def result = runGradleBuild("generateLicenseReport")

        then:
        def lines = new File(testProjectDir, "build/reports/dependency-license/NOTICE.txt").readLines()
        println lines
        println "================================"
        println result.output
        result.task(":generateLicenseReport").outcome == SUCCESS
    }

    protected def runGradleBuild(String task, String... additionalArguments) {
        List<String> args = [task, '--stacktrace']

        GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments(args + Arrays.asList(additionalArguments))
                .withPluginClasspath(pluginClasspath)
                .forwardOutput()
                .build()
    }

    static List<File> buildPluginClasspathWithTestClasspath() {
        def classpath = PluginUnderTestMetadataReading.readImplementationClasspath()
        return classpath + classpath.collect {
            new File(it.parentFile, "test")
        }
    }
}
