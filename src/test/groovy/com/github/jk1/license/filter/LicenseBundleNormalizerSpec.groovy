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

import com.github.jk1.license.ProjectBuilder
import com.github.jk1.license.ProjectData
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class LicenseBundleNormalizerSpec extends Specification {
    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()
    File normalizerFile
    File pluginOutputDir

    ProjectBuilder builder = new ProjectBuilder()

    def setup() {
        testProjectDir.create()
        pluginOutputDir = new File(com.github.jk1.license.ProjectDataFixture.GRADLE_PROJECT().gocdPlugin.licenseReport.outputDir)
        pluginOutputDir.mkdirs()

        normalizerFile = testProjectDir.newFile('test-normalizer-config.json')
        normalizerFile << """
            {
              "bundles" : [
                { "bundleName" : "apache1", "licenseName" : "Apache Software License, Version 1.1", "licenseUrl" : "http://www.apache.org/licenses/LICENSE-1.1" },
                { "bundleName" : "apache2", "licenseName" : "Apache License, Version 2.0", "licenseUrl" : "https://www.apache.org/licenses/LICENSE-2.0" }
              ]"""

        // copy apache2 license file
        def apache2LicenseFile = new File(getClass().getResource("/apache2-license.txt").toURI())
        new File(pluginOutputDir, "apache2-license.txt") << apache2LicenseFile.text
    }

    def "normalizer constructor can be called with named parameters"() {
        when:
        new LicenseBundleNormalizer()
        new LicenseBundleNormalizer(bundlePath: null)
        new LicenseBundleNormalizer(createDefaultTransformationRules: false)
        new LicenseBundleNormalizer(bundlePath: null, createDefaultTransformationRules: false)

        then:
        noExceptionThrown()
    }

    def "normalize license of manifest (when stored as name)"() {
        normalizerFile << """,
            "transformationRules" : [
                { "bundleName" : "apache2", "licenseNamePattern" : "Apache 2.*" }
              ]
            }"""

        ProjectData projectData = builder.project {
            configuration("runtime") {
                module("mod1") {
                    manifest("mani1") {
                        license("Apache 2")
                    }
                }
            }
        }
        ProjectData expected = builder.project {
            configuration("runtime") {
                module("mod1") {
                    manifest("mani1") {
                        license("Apache License, Version 2.0")
                    }
                }
            }
        }

        when:
        def result = newNormalizer().filter(projectData)

        then:
        com.github.jk1.license.ProjectBuilder.json(result) == com.github.jk1.license.ProjectBuilder.json(expected)
    }

    def "normalize the manifests license or to the appropriate bundle-license-name"() {
        normalizerFile << """,
            "transformationRules" : [
                { "bundleName" : "apache2", "licenseUrlPattern" : "http://www.apache.org/licenses/LICENSE-2.0.*" }
              ]
            }"""

        ProjectData projectData = builder.project {
            configuration("runtime") {
                module("mod1") {
                    manifest("mani1") {
                        license("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
        }
        ProjectData expected = builder.project {
            configuration("runtime") {
                module("mod1") {
                    manifest("mani1") {
                        license("Apache License, Version 2.0")
                    }
                }
            }
        }

        when:
        def result = newNormalizer().filter(projectData)

        then:
        com.github.jk1.license.ProjectBuilder.json(result) == com.github.jk1.license.ProjectBuilder.json(expected)
    }

    def "all poms of all configurations are normalized"() {
        normalizerFile << """,
            "transformationRules" : [
                { "bundleName" : "apache2", "licenseNamePattern" : "Apache 2.*" }
              ]
            }"""

        ProjectData projectData = builder.project {
            configuration("runtime") {
                module("mod1") {
                    pom("pom1") {
                        license(com.github.jk1.license.ProjectDataFixture.APACHE2_LICENSE(), name: "Apache 2")
                    }
                }
            }
            configuration("test") {
                module("mod1") {
                    pom("pom1") {
                        license(com.github.jk1.license.ProjectDataFixture.APACHE2_LICENSE(), name: "Apache 2")
                    }
                }
            }
        }
        ProjectData expected = builder.project {
            configuration("runtime") {
                module("mod1") {
                    pom("pom1") {
                        license(com.github.jk1.license.ProjectDataFixture.APACHE2_LICENSE())
                    }
                }
            }
            configuration("test") {
                module("mod1") {
                    pom("pom1") {
                        license(com.github.jk1.license.ProjectDataFixture.APACHE2_LICENSE())
                    }
                }
            }
        }

        when:
        def result = newNormalizer().filter(projectData)

        then:
        com.github.jk1.license.ProjectBuilder.json(result) == com.github.jk1.license.ProjectBuilder.json(expected)
    }

    def "all manifests of all configurations are normalized"() {
        normalizerFile << """,
            "transformationRules" : [
                { "bundleName" : "apache2", "licenseNamePattern" : "Apache 2.*" }
              ]
            }"""

        ProjectData projectData = builder.project {
            configuration("runtime") {
                module("mod1") {
                    manifest("mani1") {
                        license("Apache 2")
                    }
                }
            }
            configuration("test") {
                module("mod1") {
                    manifest("mani1") {
                        license("Apache 2")
                    }
                }
            }
        }
        ProjectData expected = builder.project {
            configuration("runtime") {
                module("mod1") {
                    manifest("mani1") {
                        license("Apache License, Version 2.0")
                    }
                }
            }
            configuration("test") {
                module("mod1") {
                    manifest("mani1") {
                        license("Apache License, Version 2.0")
                    }
                }
            }
        }

        when:
        def result = newNormalizer().filter(projectData)

        then:
        com.github.jk1.license.ProjectBuilder.json(result) == com.github.jk1.license.ProjectBuilder.json(expected)
    }

    def "all license files of all configurations are normalized"() {
        normalizerFile << """,
            "transformationRules" : [
                { "bundleName" : "apache2", "licenseFileContentPattern" : ".*Apache License, Version 2.0.*" }
              ]
            }"""

        ProjectData projectData = builder.project {
            configuration("runtime") {
                module("mod1") {
                    licenseFiles {
                        licenseFileDetails(file: "apache2-license.txt")
                    }
                }
            }
            configuration("test") {
                module("mod1") {
                    licenseFiles {
                        licenseFileDetails(file: "apache2-license.txt")
                    }
                }
            }
        }
        ProjectData expected = builder.project {
            configuration("runtime") {
                module("mod1") {
                    licenseFiles {
                        licenseFileDetails(file: "apache2-license.txt", license: "Apache License, Version 2.0", licenseUrl: "https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
            }
            configuration("test") {
                module("mod1") {
                    licenseFiles {
                        licenseFileDetails(file: "apache2-license.txt", license: "Apache License, Version 2.0", licenseUrl: "https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
            }
        }

        when:
        def result = newNormalizer().filter(projectData)

        then:
        com.github.jk1.license.ProjectBuilder.json(result) == com.github.jk1.license.ProjectBuilder.json(expected)
    }

    def "the licence-file is normalized by name-pattern (not by content-pattern)"() {
        normalizerFile << """,
            "transformationRules" : [
                { "bundleName" : "apache2", "licenseNamePattern" : "Apache 2.*" }
              ]
            }"""

        ProjectData projectData = builder.project {
            configuration("runtime") {
                module("mod1") {
                    licenseFiles {
                        licenseFileDetails(file: "apache2-license.txt", license: "Apache 2")
                    }
                }
            }
        }
        ProjectData expected = builder.project {
            configuration("runtime") {
                module("mod1") {
                    licenseFiles {
                        licenseFileDetails(file: "apache2-license.txt", license: "Apache License, Version 2.0", licenseUrl: "https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
            }
        }

        when:
        def result = newNormalizer().filter(projectData)

        then:
        com.github.jk1.license.ProjectBuilder.json(result) == com.github.jk1.license.ProjectBuilder.json(expected)
    }

    def "the licence-file is normalized by url-pattern (not by content-pattern)"() {
        normalizerFile << """,
            "transformationRules" : [
                { "bundleName" : "apache2", "licenseUrlPattern" : "http://www.apache.org/licenses/LICENSE-2.0.txt" }
              ]
            }"""

        ProjectData projectData = builder.project {
            configuration("runtime") {
                module("mod1") {
                    licenseFiles {
                        licenseFileDetails(file: "apache2-license.txt", licenseUrl: "http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
        }
        ProjectData expected = builder.project {
            configuration("runtime") {
                module("mod1") {
                    licenseFiles {
                        licenseFileDetails(file: "apache2-license.txt", license: "Apache License, Version 2.0", licenseUrl: "https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
            }
        }

        when:
        def result = newNormalizer().filter(projectData)

        then:
        com.github.jk1.license.ProjectBuilder.json(result) == com.github.jk1.license.ProjectBuilder.json(expected)
    }

    def "duplicate licenses within a pom are unified"() {
        normalizerFile << """,
            "transformationRules" : [
                { "bundleName" : "apache2", "licenseNamePattern" : "Apache 2.0" }
              ]
            }"""

        ProjectData projectData = builder.project {
            configuration("runtime") {
                module("mod1") {
                    pom("pom1") {
                        license(com.github.jk1.license.ProjectDataFixture.APACHE2_LICENSE(), name: "The Apache 2 License") // should stay
                        license(com.github.jk1.license.ProjectDataFixture.APACHE2_LICENSE(), name: "Apache 2.0") // should be unified with the last one
                        license(com.github.jk1.license.ProjectDataFixture.APACHE2_LICENSE())
                    }
                }
            }
        }
        ProjectData expected = builder.project {
            configuration("runtime") {
                module("mod1") {
                    pom("pom1") {
                        license(com.github.jk1.license.ProjectDataFixture.APACHE2_LICENSE(), name: "The Apache 2 License")
                        license(com.github.jk1.license.ProjectDataFixture.APACHE2_LICENSE())
                    }
                }
            }
        }

        when:
        def result = newNormalizer().filter(projectData)

        then:
        com.github.jk1.license.ProjectBuilder.json(result) == com.github.jk1.license.ProjectBuilder.json(expected)
    }

    def "duplicate licenses files within a pom are unified"() {
        normalizerFile << """,
            "transformationRules" : [
                { "bundleName" : "apache2", "licenseNamePattern" : "Apache 2.0" }
              ]
            }"""

        ProjectData projectData = builder.project {
            configuration("runtime") {
                module("mod1") {
                    licenseFiles {
                        licenseFileDetails(file: "apache2-license.txt", licenseUrl: "http://www.apache.org/licenses/LICENSE-2.0.txt")
                        licenseFileDetails(file: "apache2-license.txt", licenseUrl: "http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
        }
        ProjectData expected = builder.project {
            configuration("runtime") {
                module("mod1") {
                    licenseFiles {
                        licenseFileDetails(file: "apache2-license.txt", licenseUrl: "http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
        }

        when:
        def result = newNormalizer().filter(projectData)

        then:
        com.github.jk1.license.ProjectBuilder.json(result) == com.github.jk1.license.ProjectBuilder.json(expected)
    }

    def "the name/url isn't applied to the license, when configured not to do so"() {
        normalizerFile << """,
            "transformationRules" : [
                { "bundleName" : "apache2", "licenseNamePattern" : "name1", "transformUrl" : false },
                { "bundleName" : "apache1", "licenseUrlPattern" :  "url2",  "transformUrl" : false },
                { "bundleName" : "apache2", "licenseNamePattern" : "name3", "transformName" : false },
                { "bundleName" : "apache2", "licenseUrlPattern" :  "url4",  "transformName" : false },
                { "bundleName" : "apache2", "licenseNamePattern" :  "name5",  "transformName" : false, "transformUrl" : false }
              ]
            }"""

        ProjectData projectData = builder.project {
            configuration("runtime") {
                module("mod1") {
                    pom("pom1") {
                        license(name: "name1", url: "url1")
                        license(name: "name2", url: "url2")
                        license(name: "name3", url: "url3")
                        license(name: "name4", url: "url4")
                        license(name: "name5", url: "url5")
                    }
                }
            }
        }
        ProjectData expected = builder.project {
            configuration("runtime") {
                module("mod1") {
                    pom("pom1") {
                        license(name: "Apache License, Version 2.0", url: "url1")
                        license(name: "Apache Software License, Version 1.1", url: "url2")
                        license(name: "name3", url: "https://www.apache.org/licenses/LICENSE-2.0")
                        license(name: "name4", url: "https://www.apache.org/licenses/LICENSE-2.0")
                        license(name: "name5", url: "url5")
                    }
                }
            }
        }

        when:
        def result = newNormalizer().filter(projectData)

        then:
        com.github.jk1.license.ProjectBuilder.json(result) == com.github.jk1.license.ProjectBuilder.json(expected)
    }

    def "the name/url is applied to the license, when configured to do so"() {
        normalizerFile << """,
            "transformationRules" : [
                { "bundleName" : "apache2", "licenseNamePattern" : "name1", "transformUrl" : true },
                { "bundleName" : "apache2", "licenseUrlPattern" :  "url2",  "transformUrl" : true },
                { "bundleName" : "apache2", "licenseNamePattern" : "name3", "transformName" : true },
                { "bundleName" : "apache2", "licenseUrlPattern" :  "url4",  "transformName" : true },
                { "bundleName" : "apache2", "licenseNamePattern" :  "name5",  "transformName" : true, "transformUrl" : true }
              ]
            }"""

        ProjectData projectData = builder.project {
            configuration("runtime") {
                module("mod1") {
                    pom("pom1") {
                        license(name: "name1", url: "url1")
                        license(name: "name2", url: "url2")
                        license(name: "name3", url: "url3")
                        license(name: "name4", url: "url4")
                        license(name: "name5", url: "url5")
                    }
                }
            }
        }
        ProjectData expected = builder.project {
            configuration("runtime") {
                module("mod1") {
                    pom("pom1") {
                        license(name: "Apache License, Version 2.0", url: "https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
            }
        }

        when:
        def result = newNormalizer().filter(projectData)

        then:
        com.github.jk1.license.ProjectBuilder.json(result) == com.github.jk1.license.ProjectBuilder.json(expected)
    }

    def "a transformation rule is created for each licence-bundle-name as exact match"() {
        normalizerFile << """}"""

        ProjectData projectData = builder.project {
            configuration("runtime") {
                module("mod1") {
                    pom("pom1") {
                        license(com.github.jk1.license.ProjectDataFixture.APACHE2_LICENSE(), url: "different url") // should be normalized because name matches the bundle-name
                        license(com.github.jk1.license.ProjectDataFixture.APACHE2_LICENSE(), name: "Apache 2.0", url: "different url")   // should stay, because name is different
                    }
                }
            }
        }
        ProjectData expected = builder.project {
            configuration("runtime") {
                module("mod1") {
                    pom("pom1") {
                        license(com.github.jk1.license.ProjectDataFixture.APACHE2_LICENSE())
                        license(com.github.jk1.license.ProjectDataFixture.APACHE2_LICENSE(), name: "Apache 2.0", url: "different url")
                    }
                }
            }
        }

        when:
        def result = newNormalizer(true).filter(projectData)

        then:
        com.github.jk1.license.ProjectBuilder.json(result) == com.github.jk1.license.ProjectBuilder.json(expected)
    }

    def "a transformation rule is created for each licence-bundle-url as exact match"() {
        normalizerFile << """}"""

        ProjectData projectData = builder.project {
            configuration("runtime") {
                module("mod1") {
                    pom("pom1") {
                        license(com.github.jk1.license.ProjectDataFixture.APACHE2_LICENSE(), name: "different name") // should be normalized because url matches the bundle-url
                        license(com.github.jk1.license.ProjectDataFixture.APACHE2_LICENSE(), name: "Apache 2.0", url: "different url")   // should stay, because url is different
                    }
                }
            }
        }
        ProjectData expected = builder.project {
            configuration("runtime") {
                module("mod1") {
                    pom("pom1") {
                        license(com.github.jk1.license.ProjectDataFixture.APACHE2_LICENSE())
                        license(com.github.jk1.license.ProjectDataFixture.APACHE2_LICENSE(), name: "Apache 2.0", url: "different url")
                    }
                }
            }
        }

        when:
        def result = newNormalizer(true).filter(projectData)

        then:
        com.github.jk1.license.ProjectBuilder.json(result) == com.github.jk1.license.ProjectBuilder.json(expected)
    }

    def "not creating default transformation rules when turned off"() {
        normalizerFile << """}"""

        ProjectData projectData = builder.project {
            configuration("runtime") {
                module("mod1") {
                    pom("pom1") {
                        license(com.github.jk1.license.ProjectDataFixture.APACHE2_LICENSE(), url: "different url")
                        license(com.github.jk1.license.ProjectDataFixture.APACHE2_LICENSE(), name: "different name")
                        license(com.github.jk1.license.ProjectDataFixture.APACHE2_LICENSE(), name: "Apache 2.0", url: "different url")
                    }
                }
            }
        }
        ProjectData expected = builder.project {
            configuration("runtime") {
                module("mod1") {
                    pom("pom1") {
                        license(com.github.jk1.license.ProjectDataFixture.APACHE2_LICENSE(), url: "different url")
                        license(com.github.jk1.license.ProjectDataFixture.APACHE2_LICENSE(), name: "different name")
                        license(com.github.jk1.license.ProjectDataFixture.APACHE2_LICENSE(), name: "Apache 2.0", url: "different url")
                    }
                }
            }
        }

        when:
        def result = newNormalizer(false).filter(projectData)

        then:
        com.github.jk1.license.ProjectBuilder.json(result) == com.github.jk1.license.ProjectBuilder.json(expected)
    }

    private LicenseBundleNormalizer newNormalizer(boolean createDefaultTransformationRules = false) {
        new LicenseBundleNormalizer(bundlePath: normalizerFile.absolutePath, createDefaultTransformationRules: createDefaultTransformationRules)
    }
}
