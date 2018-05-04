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

package cd.go.plugin.gradlehelper.models

import org.gradle.api.Project

class LicenseReport {

    boolean licenseCheck
    List<String> allowedLicenses = [
            'Apache License, Version 2.0',
            'Apache 2',
            'Apache 2.0',
            'The Apache Software License, Version 2.0',
            'The Apache License, Version 2.0',
            'MIT license',
            'The MIT License',
    ]

    String outputDir
    String[] configurations
    String[] excludeGroups
    String[] excludes

    LicenseReport(Project project) {
        outputDir = "${project.buildDir}/reports/dependency-license"
        configurations = ['runtime']
        excludeGroups = ["${project.group}"]
        excludes = []
    }

    Map<String, String> toHash() {
        return ['licenseCheck'   : licenseCheck,
                'allowedLicenses': allowedLicenses,
                'outputDir'      : outputDir,
                'configurations' : configurations,
                'excludes'       : excludes,
                'excludeGroups'  : excludeGroups
        ] as Map<String, String>
    }

}
