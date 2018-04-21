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

package cd.go.plugin.gradlehelper.pretty_test

import org.gradle.api.tasks.testing.*

import static cd.go.plugin.gradlehelper.pretty_test.AnsiColorCode.*
import static org.gradle.api.tasks.testing.TestResult.ResultType.*

class PrettyTestLogger implements TestListener, TestOutputListener {

    @Override
    void beforeSuite(TestDescriptor suite) {
        if (suite.name.startsWith("Test Run") || suite.name.startsWith("Gradle Worker")) {
            // ignore
        } else if (suite.parent != null && suite.className != null) {
            println "$BOLD_WHITE$suite.name$RESET"
        }
    }

    @Override
    void afterSuite(TestDescriptor suite, TestResult result) {
        println "\n"
        String color = color(result)
        if (!suite.parent) {
            String summary = new StringBuilder()
                    .append("Result: ").append(color).append(result.resultType).append(RESET).append(" ")
                    .append("(").append(result.testCount).append(" tests, ")
                    .append(GREEN).append(result.successfulTestCount).append(" passed, ").append(RESET)
                    .append(RED).append(result.failedTestCount).append(" failed, ").append(RESET)
                    .append(YELLOW).append(result.skippedTestCount).append(" skipped").append(RESET)
                    .append(")")
                    .toString()

            println "--------------------------------------------------------------------------"
            println summary
            println "--------------------------------------------------------------------------"
        }
    }

    @Override
    void beforeTest(TestDescriptor testDescriptor) {
    }

    @Override
    void afterTest(TestDescriptor descriptor, TestResult result) {
        String symbol = AnsiSymbol.getSymbol(result.resultType)
        println "   ${color(result)}$symbol$RESET  $descriptor.name$YELLOW (${testExecTime(result.startTime, result.endTime)})$RESET"
        if (result.resultType == FAILURE) {
            println ""
            println String.format("%s", ExceptionUtils.stackTrace(result.exceptions, "\t"))
            println ""
        }
    }

    @Override
    void onOutput(TestDescriptor testDescriptor, TestOutputEvent outputEvent) {

    }

    private static String color(TestResult result) {
        String color = WHITE
        switch (result.resultType) {
            case SUCCESS:
                color = GREEN
                break
            case FAILURE:
                color = RED
                break
            case SKIPPED:
                color = YELLOW
                break
        }
        return color
    }

    private static String testExecTime(long start, long end) {
        def execTime = end - start
        if (execTime < 1000) {
            return execTime + " millis"
        }

        if (execTime < 60 * 1000) {
            return (execTime / 1000) + " seconds"
        }

        if (execTime < 60 * 60 * 1000) {
            return (execTime / 60000) + "minutes"
        }

        return (execTime / 3600000) + "hours"
    }
}
