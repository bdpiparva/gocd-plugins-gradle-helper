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
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class BaseTest extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()
    protected File testProjectDir
    protected File buildFile
    protected List<File> pluginClasspath

    void setup() {
        testProjectDir = temporaryFolder.root
        pluginClasspath = buildPluginClasspathWithTestClasspath()
        buildFile = new File(testProjectDir, "build.gradle")
        buildFile << """
            plugins {
                id 'cd.go.plugin.gradlehelper'
            }
            
            repositories {
                mavenCentral()
            }
        """
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

    private static List<File> buildPluginClasspathWithTestClasspath() {
        def classpath = PluginUnderTestMetadataReading.readImplementationClasspath()
        return classpath + classpath.collect {
            new File(it.parentFile, "test")
        }
    }
}
