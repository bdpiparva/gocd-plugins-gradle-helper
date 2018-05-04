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

package com.github.jk1.license.filter

import com.github.jk1.license.AbstractGradleRunnerFunctionalSpec
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Ignore

class LicenseBundleNormalizerFuncSpec extends AbstractGradleRunnerFunctionalSpec {

    File licenseResultJsonFile
    File normalizerFile

    def setup() {
        licenseResultJsonFile = new File(outputDir, "index.json")

        normalizerFile = testProjectDir.newFile('test-normalizer-config.json')

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

            import com.github.jk1.license.filter.*
            import com.github.jk1.license.render.*
            import com.github.jk1.license.reader.*
            gocdPlugin {
                pluginInfo {
                    id = 'cd.go.scm.github'
                    name = "GitHub plugin"
                    version = '0.0.1'
                    group = 'cd.go'
                    serverVersion = '18.2.0'
                }
                licenseReport {
                    outputDir = "$outputDir.absolutePath"
                    filters = new LicenseBundleNormalizer(bundlePath: "$normalizerFile.absolutePath", createDefaultTransformationRules: false)
                    renderer = new MultiReportRenderer(new JsonReportRenderer(onlyOneLicensePerModule: false), new RawProjectDataJsonRenderer())
                    configurations = ['forTesting']
                }
            }
        """

        normalizerFile << """
            {
              "bundles" : [
                { "bundleName" : "apache1", "licenseName" : "Apache Software License, Version 1.1", "licenseUrl" : "https://www.apache.org/licenses/LICENSE-1.1" },
                { "bundleName" : "apache2", "licenseName" : "Apache License, Version 2.0", "licenseUrl" : "https://www.apache.org/licenses/LICENSE-2.0" },
                { "bundleName" : "cddl1", "licenseName" : "COMMON DEVELOPMENT AND DISTRIBUTION LICENSE Version 1.0", "licenseUrl" : "https://opensource.org/licenses/CDDL-1.0" },
                { "bundleName" : "gpl2", "licenseName" : "GNU GENERAL PUBLIC LICENSE, Version 2", "licenseUrl" : "https://www.gnu.org/licenses/gpl-2.0" }
              ],
        """
    }

    def "normalizes dependencies by configured license name"() {
        buildFile << """
            dependencies {
                forTesting "org.jetbrains:annotations:13.0"     // license-name: "The Apache Software License, Version 2.0"
                forTesting "io.netty:netty-common:4.1.17.Final" // license-name: "Apache License, Version 2.0"
            }
        """
        normalizerFile << """
              "transformationRules" : [
                { "bundleName" : "apache2", "licenseNamePattern" : ".*The Apache Software License, Version 2.0.*" }
              ]
            }
        """

        when:
        def runResult = runGradleBuild()

        def result = jsonSlurper.parse(licenseResultJsonFile)

        then:
        runResult.task(":generateLicenseReport").outcome == TaskOutcome.SUCCESS

        result.dependencies.size() == 2
        result.dependencies*.moduleLicenses.flatten()*.moduleLicense.toSet() == ["Apache License, Version 2.0"].toSet()
        result.dependencies*.moduleLicenses.flatten()*.moduleLicenseUrl.toSet() == ["https://www.apache.org/licenses/LICENSE-2.0", "http://www.apache.org/licenses/LICENSE-2.0"].toSet()
    }

    def "normalizes dependencies by configured license url"() {
        buildFile << """
            dependencies {
                forTesting "org.jetbrains:annotations:13.0"     // license-url: "http://www.apache.org/licenses/LICENSE-2.0.txt"
                forTesting "io.netty:netty-common:4.1.17.Final" // license-url: "http://www.apache.org/licenses/LICENSE-2.0"
            }
        """
        normalizerFile << """
              "transformationRules" : [
                { "bundleName" : "apache2", "licenseUrlPattern" : ".*www.apache.org/licenses/LICENSE-2.0.*" }
              ]
            }
        """

        when:
        def runResult = runGradleBuild()

        def result = jsonSlurper.parse(licenseResultJsonFile)

        then:
        runResult.task(":generateLicenseReport").outcome == TaskOutcome.SUCCESS

        result.dependencies.size() == 2
        result.dependencies*.moduleLicenses.flatten()*.moduleLicense.toSet() == ["Apache License, Version 2.0"].toSet()
        result.dependencies*.moduleLicenses.flatten()*.moduleLicenseUrl.toSet() == ["https://www.apache.org/licenses/LICENSE-2.0"].toSet()
    }

    def "normalizes manifest with pom data"() {
        buildFile << """
            dependencies {
                forTesting "joda-time:joda-time:2.9.7" // has manifest and pom information
            }
        """
        normalizerFile << """
              "transformationRules" : [
                   { "bundleName" : "apache2", "licenseNamePattern" : "Apache 2.*" }
              ]
            }
        """

        when:
        def runResult = runGradleBuild()

        def result = jsonSlurper.parse(licenseResultJsonFile)

        then:
        runResult.task(":generateLicenseReport").outcome == TaskOutcome.SUCCESS

        result.dependencies*.moduleLicenses.flatten()*.moduleLicense.toSet() == ["Apache License, Version 2.0"].toSet()
        result.dependencies*.moduleLicenses.flatten()*.moduleLicenseUrl.toSet() == ["https://www.apache.org/licenses/LICENSE-2.0"].toSet()
    }

    def "an error is raised when a normalizer file is specified but not available"() {
        normalizerFile.delete()

        when:
        runGradleBuild()

        then:
        thrown(Exception)
    }

    def "default filter file is used when nothing else specified"() {
        buildFile.setText("") // clear the file first
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

            import com.github.jk1.license.filter.*
            import com.github.jk1.license.render.*
            gocdPlugin {
                pluginInfo {
                    id = 'cd.go.scm.github'
                    name = "GitHub plugin"
                    version = '0.0.1'
                    group = 'cd.go'
                    serverVersion = '18.2.0'
                }
                licenseReport {
                    outputDir = "$licenseResultJsonFile.parentFile.absolutePath"
                    filters = new LicenseBundleNormalizer()
                    renderer = new JsonReportRenderer(onlyOneLicensePerModule: false)
                    configurations = ['forTesting']
                }
            }
            dependencies {
                forTesting "org.jetbrains:annotations:13.0"     // license-url: "http://www.apache.org/licenses/LICENSE-2.0.txt"
                forTesting "io.netty:netty-common:4.1.17.Final" // license-url: "http://www.apache.org/licenses/LICENSE-2.0"
                forTesting "org.apache.commons:commons-lang3:3.7"
            }
        """

        when:
        def runResult = runGradleBuild()

        then:
        runResult.task(":generateLicenseReport").outcome == TaskOutcome.SUCCESS

        licenseResultJsonFile.text == """{
    "dependencies": [
        {
            "moduleName": "io.netty:netty-common",
            "moduleVersion": "4.1.17.Final",
            "moduleUrls": [
                "http://netty.io/"
            ],
            "moduleLicenses": [
                {
                    "moduleLicense": "Apache License, Version 2.0",
                    "moduleLicenseUrl": "http://www.apache.org/licenses/LICENSE-2.0"
                }
            ]
        },
        {
            "moduleName": "org.apache.commons:commons-lang3",
            "moduleVersion": "3.7",
            "moduleUrls": [
                "http://commons.apache.org/proper/commons-lang/"
            ],
            "moduleLicenses": [
                {
                    "moduleLicense": "Apache License, Version 2.0",
                    "moduleLicenseUrl": "http://www.apache.org/licenses/LICENSE-2.0"
                }
            ]
        },
        {
            "moduleName": "org.jetbrains:annotations",
            "moduleVersion": "13.0",
            "moduleUrls": [
                "http://www.jetbrains.org"
            ],
            "moduleLicenses": [
                {
                    "moduleLicense": "Apache License, Version 2.0",
                    "moduleLicenseUrl": "http://www.apache.org/licenses/LICENSE-2.0"
                }
            ]
        }
    ]
}"""
    }

    def "licenseFileDetails are extended with the license information"() {
        buildFile << """
            dependencies {
                forTesting "joda-time:joda-time:2.9.9"
                forTesting "org.apache.commons:commons-lang3:3.7"
            }
        """
        normalizerFile << """
              "transformationRules" : [
                { "bundleName" : "apache2", "licenseFileContentPattern" : ".*Apache License, Version 2.0.*" }
              ]
            }
        """

        when:
        def runResult = runGradleBuild()
        def resultFileGPath = jsonSlurper.parse(rawJsonFile)
        def licenseFilesGPath = resultFileGPath.configurations*.dependencies.flatten().licenseFiles.flatten()
        def licenseFileString = AbstractGradleRunnerFunctionalSpec.prettyPrintJson(licenseFilesGPath)

        then:
        runResult.task(":generateLicenseReport").outcome == TaskOutcome.SUCCESS

        licenseFileString == """[
    {
        "fileDetails": [
            {
                "licenseUrl": "https://www.apache.org/licenses/LICENSE-2.0",
                "file": "joda-time-2.9.9.jar/META-INF/LICENSE.txt",
                "license": "Apache License, Version 2.0"
            },
            {
                "licenseUrl": null,
                "file": "joda-time-2.9.9.jar/META-INF/NOTICE.txt",
                "license": null
            }
        ],
        "files": [
            "joda-time-2.9.9.jar/META-INF/LICENSE.txt",
            "joda-time-2.9.9.jar/META-INF/NOTICE.txt"
        ]
    },
    {
        "fileDetails": [
            {
                "licenseUrl": null,
                "file": "commons-lang3-3.7.jar/META-INF/NOTICE.txt",
                "license": null
            },
            {
                "licenseUrl": "https://www.apache.org/licenses/LICENSE-2.0",
                "file": "commons-lang3-3.7.jar/META-INF/LICENSE.txt",
                "license": "Apache License, Version 2.0"
            }
        ],
        "files": [
            "commons-lang3-3.7.jar/META-INF/NOTICE.txt",
            "commons-lang3-3.7.jar/META-INF/LICENSE.txt"
        ]
    }
]"""
    }

    def "normalizes dependencies by configured license text and exports result to json"() {
        buildFile << """
            dependencies {
                forTesting "joda-time:joda-time:2.9.9"
                forTesting "org.apache.commons:commons-lang3:3.7"
            }
        """
        normalizerFile << """
              "transformationRules" : [
                { "bundleName" : "apache2", "licenseFileContentPattern" : ".*Apache License, Version 2.0.*" }
              ]
            }
        """

        when:
        def runResult = runGradleBuild()

        then:
        runResult.task(":generateLicenseReport").outcome == TaskOutcome.SUCCESS

        licenseResultJsonFile.text == """{
    "dependencies": [
        {
            "moduleName": "joda-time:joda-time",
            "moduleVersion": "2.9.9",
            "moduleUrls": [
                "http://www.joda.org/joda-time/"
            ],
            "moduleLicenses": [
                {
                    "moduleLicense": "Apache 2",
                    "moduleLicenseUrl": "http://www.apache.org/licenses/LICENSE-2.0.txt"
                },
                {
                    "moduleLicense": "Apache 2.0",
                    "moduleLicenseUrl": null
                },
                {
                    "moduleLicense": "Apache License, Version 2.0",
                    "moduleLicenseUrl": "https://www.apache.org/licenses/LICENSE-2.0"
                }
            ]
        },
        {
            "moduleName": "org.apache.commons:commons-lang3",
            "moduleVersion": "3.7",
            "moduleUrls": [
                "http://commons.apache.org/proper/commons-lang/"
            ],
            "moduleLicenses": [
                {
                    "moduleLicense": "Apache License, Version 2.0",
                    "moduleLicenseUrl": "https://www.apache.org/licenses/LICENSE-2.0.txt"
                }
            ]
        }
    ]
}"""
    }

    @Ignore("Think about multiple licenses in a module AND multiple licenses within one license-name")
    def "normalizes combined licenses without losing information of one of the single licenses"() {
        buildFile << """
            dependencies {
                forTesting "javax.annotation:javax.annotation-api:1.3.2" // CDDL + GPL
            }
        """
        normalizerFile << """
              "transformationRules" : [
                { "bundleName" : "cddl1", "licenseNamePattern" : "CDDL + GPLv2 with classpath exception" },
                { "bundleName" : "gpl2", "licenseNamePattern" : "CDDL + GPLv2 with classpath exception" }
              ]
            }
        """

        when:
        def runResult = runGradleBuild()

        def result = jsonSlurper.parse(licenseResultJsonFile)

        then:
        runResult.task(":generateLicenseReport").outcome == TaskOutcome.SUCCESS

        result.dependencies.size() == 1
        result.dependencies*.moduleLicense.toSet() == ["Apache License, Version 2.0", "COMMON DEVELOPMENT AND DISTRIBUTION LICENSE Version 1.0"].toSet()
        result.dependencies*.moduleLicenseUrl.toSet() == ["http://www.apache.org/licenses/LICENSE-2.0", "http://opensource.org/licenses/CDDL-1.0"].toSet()
    }
}
