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

import cd.go.plugin.gradlehelper.utils.StringUtils


class GitInfoProvider {
    static boolean isGitRepo(String dir) {
        return "git -C ${dir} rev-parse".execute().waitFor() == 0
    }

    static String gitRevision(String dir) {
        if (isGitRepo(dir)) {
            def process = "git log -n 1 --format=%H".execute(null, new File(dir))
            process.waitFor()
            return success(process) ? StringUtils.chomp(process.text) : null
        }

        throw new RuntimeException("Ouch! Not a git repo.")
    }

    static long gitRevisionCount(String dir) {
        if (isGitRepo(dir)) {
            def process = "git rev-list HEAD --count".execute(null, new File(dir))
            process.waitFor()
            return success(process) ? Long.parseLong(process.text.trim()) : 0
        }

        throw new RuntimeException("Ouch! Not a git repo.")
    }

    private static boolean success(Process process) {
        return process.exitValue() == 0
    }
}
