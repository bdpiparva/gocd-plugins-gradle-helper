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
