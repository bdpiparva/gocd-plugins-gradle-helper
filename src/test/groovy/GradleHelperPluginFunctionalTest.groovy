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

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class GradleHelperPluginFunctionalTest extends Specification {
    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    void setup() {
        buildFile = testProjectDir.newFile("build.gradle")
    }

    @Test
    def "should run extensionInfo task"() {
        given:
        buildFile << """
            plugins {
                id 'cd.go.plugin.gradlehelper'
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
                    licenseCheck = true
                }
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('extensionInfo')
                .withPluginClasspath()
                .build()

        then:
        println "================================"
        println result.output
        result.output.contains('true')
        result.task(":extensionInfo").outcome == SUCCESS
    }

    @Test
    def "should run licenseNotice task"() {
        given:
        buildFile << """
            plugins {
                id 'cd.go.plugin.gradlehelper'
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
                    licenseCheck = true
                }
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('hello', '--stacktrace')
                .withPluginClasspath()
                .build()

        then:
        println result.output
        result.task(":hello").outcome == SUCCESS
    }

    @Test
    def "should return true if git repo has no commit"() {
        given:
        buildFile << """
            plugins {
                id 'cd.go.plugin.gradlehelper'
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
                    licenseCheck = true
                }
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('hello', '--stacktrace')
                .withPluginClasspath()
                .build()

        then:
        println result.output
        result.task(":hello").outcome == SUCCESS
    }
}
