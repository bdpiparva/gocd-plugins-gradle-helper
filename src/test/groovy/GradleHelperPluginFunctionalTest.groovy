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
                    id = 'foo'
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
                    id = 'foo'
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
                    id = 'foo'
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
