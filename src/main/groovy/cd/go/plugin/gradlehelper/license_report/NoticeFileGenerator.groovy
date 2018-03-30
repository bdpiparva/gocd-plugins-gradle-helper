package cd.go.plugin.gradlehelper.license_report

import com.github.jk1.license.ProjectData
import com.github.jk1.license.render.ReportRenderer
import com.github.jk1.license.render.SimpleHtmlReportRenderer
import com.github.jk1.license.render.SingleInfoReportRenderer

class NoticeFileGenerator extends SingleInfoReportRenderer implements ReportRenderer {
    ReportRenderer toDecorate
    String licenseFolder

    NoticeFileGenerator(String licenseFolder, List<String> allowedLicenses) {
        this.toDecorate = new TeeRenderer(new SimpleHtmlReportRenderer(), allowedLicenses)
        this.licenseFolder = licenseFolder
    }

    @Override
    void render(ProjectData projectData) {
        toDecorate.render(projectData)

        projectData.allDependencies.collect { data ->
            def noticeFile = new File(licenseFolder + 'NOTICE.txt')
            if (!data.licenseFiles.empty) {
                data.licenseFiles.first().files.collect { file ->
                    if (new File(file).name.equals("NOTICE.txt")) {
                        noticeFile.append(new File(licenseFolder + file).getText('UTF-8'))
                        noticeFile.append('\n')
                    }
                }
            }
        }
    }
}
