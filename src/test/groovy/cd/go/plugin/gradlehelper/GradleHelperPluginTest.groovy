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

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class GradleHelperPluginTest extends BaseTest {

    def "should generate notice file"() {
        given:
        buildFile << """
            configurations {
                forTesting
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
        def actual = new File(testProjectDir, "build/reports/dependency-license/NOTICE.txt").readLines().join("\n")
        def expected = "This product includes software from the Codehaus(http://codehaus.org),\n" +
                "MIT license\n" +
                "\n" +
                "Apache Commons Lang\n" +
                "Copyright 2001-2017 The Apache Software Foundation\n" +
                "\n" +
                "This product includes software developed at\n" +
                "The Apache Software Foundation (http://www.apache.org/).\n" +
                "\n" +
                "This product includes software from the Spring Framework,\n" +
                "under the Apache License 2.0 (see: StringUtils.containsWhitespace())\n" +
                "\n" +
                "This product includes software from the The Netty Project(http://netty.io/),\n" +
                "Apache License, Version 2.0"
        actual == expected
        result.task(":generateLicenseReport").outcome == SUCCESS
    }
}
