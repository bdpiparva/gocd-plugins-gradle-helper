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

import com.github.jk1.license.ModuleData
import com.github.jk1.license.ProjectData
import com.github.jk1.license.render.ReportRenderer
import com.github.jk1.license.render.SimpleHtmlReportRenderer
import com.github.jk1.license.render.SingleInfoReportRenderer
import groovy.transform.CompileStatic
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

@CompileStatic
class NoticeFileGenerator extends SingleInfoReportRenderer implements ReportRenderer {
    private static final Logger LOGGER = Logging.getLogger(NoticeFileGenerator.class)
    ReportRenderer toDecorate
    String licenseFolder
    final static List<String> allowedLicenses = [
            'Apache License, Version 2.0',
            'Apache 2',
            'Apache 2.0',
            'The Apache Software License, Version 2.0',
            'The Apache License, Version 2.0',
            'MIT license',
            'The MIT License',
    ]

    NoticeFileGenerator(String licenseFolder, List<String> allowedLicenses) {
        this.toDecorate = new TeeRenderer(new SimpleHtmlReportRenderer(), allowedLicenses)
        this.licenseFolder = licenseFolder
    }

    NoticeFileGenerator(String licenseFolder) {
        this(licenseFolder, allowedLicenses)
    }

    @Override
    void render(ProjectData projectData) {
        toDecorate.render(projectData)

        def noticeFile = new File(licenseFolder + 'NOTICE.txt')
        List<String> allEntries = []
        projectData.allDependencies.collect { data ->
            copyOverNoticeFile(allEntries, data)
            addToNoticeFileFromPomData(allEntries, data)
        }

        noticeFile.append(allEntries.join("\n"))
    }

    private void addToNoticeFileFromPomData(List<String> allEntries, ModuleData data) {
        if (!data.poms.empty) {
            data.poms.each { pom ->
                if (pom != null && pom.organization != null) {
                    if (pom.organization.name && !allEntries.join(" ").contains(pom.organization.name)) {
                        StringBuilder builder = new StringBuilder("This product includes software from the ")
                        builder.append(pom.organization.name)
                        if (pom.organization.url) {
                            builder.append("($pom.organization.url)")
                        }
                        builder.append(",")
                        pom.licenses.each { license ->
                            builder.append("\n").append(license.name)
                        }
                        allEntries.add(builder.append("\n").toString())
                    }
                }
            }
        }
    }

    private void copyOverNoticeFile(List<String> allEntries, ModuleData data) {
        if (!data.licenseFiles.empty) {
            data.licenseFiles.each { licenseFileData ->
                licenseFileData.files.each { file ->
                    if (new File(file).name.equalsIgnoreCase("NOTICE.txt")) {
                        allEntries.add(new File(licenseFolder + file).getText('UTF-8'))
                    }
                }
            }
        }
    }
}
