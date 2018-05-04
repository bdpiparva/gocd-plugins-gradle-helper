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

package cd.go.plugin.gradlehelper.license_report.models

import org.gradle.internal.impldep.com.google.common.io.Files

enum FileType {
    POM{
        @Override
        boolean isTypeOf(String filename) {
            return filename == "pom.xml" || getFileExtension(filename) == "pom"
        }
    }, ARCHIVE{
        @Override
        boolean isTypeOf(String filename) {
            String extension = getFileExtension(filename)
            return extension == "jar" || extension == "zip"
        }
    },
    UNKNOWN{
        @Override
        boolean isTypeOf(String filename) {
            return false
        }
    }

    private String getFileExtension(String filename) {
        Files.getFileExtension(filename)?.toLowerCase()
    }

    abstract boolean isTypeOf(String filename)

    static FileType fromFilename(String filename) {
        for (FileType type : values()) {
            if (type.isTypeOf(filename)) {
                return type
            }
        }

        return UNKNOWN
    }
}
