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

package cd.go.plugin.gradlehelper.license_report

import com.github.jk1.license.ProjectData
import com.github.jk1.license.render.ReportRenderer
import com.github.jk1.license.render.SingleInfoReportRenderer

class TeeRenderer extends SingleInfoReportRenderer implements ReportRenderer {
    ReportRenderer toDecorate
    private final List<String> allowedLicenses

    TeeRenderer(ReportRenderer toDecorate, List<String> allowedLicenses) {
        this.allowedLicenses = allowedLicenses
        this.toDecorate = toDecorate
    }

    @Override
    void render(ProjectData projectData) {
        toDecorate.render(projectData)

        def violations = []

        projectData.allDependencies.collect { data ->

            def moduleDesc = "${data.group}:${data.name}:${data.version}"

            if (data.poms.empty) {
                violations << "POM file for ${moduleDesc} does not contain license information"
            }

            def pomData = data.poms.first()
            if (pomData.licenses.empty) {
                violations << "POM file for ${moduleDesc} does not contain license information"
            }

            def hasValidLicense = pomData.licenses.any { license -> allowedLicenses.contains(license.name) }
            if (!hasValidLicense) {
                violations << "Unsupported license '${pomData.licenses}', from module '${moduleDesc}'"
            }
        }

        if (!violations.empty) {
            throw new RuntimeException("There were the following errors with enforcing licensing\n${violations.collect { "\t${it}" }.join("\n")}")
        }
    }
}
