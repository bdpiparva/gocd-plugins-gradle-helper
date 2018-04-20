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

package cd.go.plugin.gradlehelper.utils

class StringUtils {
    static String chomp(String str) {
        if (str?.trim()?.isEmpty()) {
            return str
        } else if (str.length() == 1) {
            char ch = str.charAt(0)
            return ch != '\r' && ch != '\n' ? str : ""
        } else {
            int lastIdx = str.length() - 1
            char last = str.charAt(lastIdx)
            if (last == '\n') {
                if (str.charAt(lastIdx - 1) == '\r') {
                    --lastIdx
                }
            } else if (last != '\r') {
                ++lastIdx
            }

            return str.substring(0, lastIdx)
        }
    }
}
