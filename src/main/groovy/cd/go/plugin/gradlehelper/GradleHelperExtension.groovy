package cd.go.plugin.gradlehelper

import cd.go.plugin.gradlehelper.license_report.LicenseReportConfig
import groovy.json.JsonBuilder
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory

@CompileStatic
class GradleHelperExtension {
    boolean prettyTestOutput
    final PluginInfo pluginInfo
    final LicenseReportConfig licenseReport

    @javax.inject.Inject
    GradleHelperExtension(ObjectFactory objectFactory, Project project) {
        pluginInfo = objectFactory.newInstance(PluginInfo)
        licenseReport = objectFactory.newInstance(LicenseReportConfig, project)
    }

    void pluginInfo(Action<? super PluginInfo> action) {
        action.execute(pluginInfo)
    }

    void licenseReport(Action<? super LicenseReportConfig> action) {
        action.execute(licenseReport)
    }


    @Override
    String toString() {
        return new JsonBuilder(
                ['prettyTestOutput'   : prettyTestOutput,
                 'pluginInfo'         : pluginInfo.toHash(),
                 'licenseReportConfig': licenseReport.toHash()
                ]).toPrettyString()
    }
}