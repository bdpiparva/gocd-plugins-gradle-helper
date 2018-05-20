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

import cd.go.plugin.gradlehelper.BaseTest

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class ProcessPluginResourcesTaskTest extends BaseTest {

    def "should  generate plugin.xml"() {
        given:
        buildFile << """
            gocdPlugin {
                pluginInfo {
                    id = 'cd.go.scm.github'
                    name = "GitHub plugin"
                    version = '0.0.1'
                    group = 'cd.go'
                    serverVersion = '18.2.0'
                }
            }
        """

        when:
        def result = runGradleBuild("processPluginResources")

        then:
        def actual = new File(buildFile.getParentFile(), "build/resources/main/plugin.xml").readLines().join("\n")

        actual.contains("<go-plugin id=\"cd.go.scm.github\" version=\"1\">\n" +
                "    <about>\n" +
                "        <name>GitHub plugin</name>\n" +
                "        <version>0.0.1</version>\n" +
                "        <target-go-version></target-go-version>\n" +
                "        <description></description>\n" +
                "        <vendor>\n" +
                "            <name></name>\n" +
                "            <url></url>\n" +
                "        </vendor>\n" +
                "    </about>\n" +
                "</go-plugin>")

        result.task(":processPluginResources").outcome == SUCCESS
    }
}
