package cd.go.plugin.gradlehelper.license_report

import cd.go.plugin.gradlehelper.GradleHelperExtension
import com.github.jk1.license.ProjectData
import com.github.jk1.license.reader.ProjectReader
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class LicenseReportTask extends DefaultTask {
    private Logger LOGGER = Logging.getLogger(LicenseReportTask.class)
    GradleHelperExtension extension

    @Input
    String getConfigurationSnapshot() {
        return extension.licenseReport.snapshot()
    }

    @OutputDirectory
    File getOutputFolder() {
        return new File(extension.licenseReport.outputDir)
    }

    @TaskAction
    void generateReport() {
        println extension
        LOGGER.info("Processing dependencies for project ${getProject().name}")
        LicenseReportConfig config = extension.licenseReport

        new File(config.outputDir).mkdirs()

        ProjectData data = new ProjectReader().read(getProject())

        LOGGER.info("Importing external dependency data. A total of ${config.importers.length} configured.")
        config.importers.each {
            data.importedModules.addAll(it.doImport())
        }

        LOGGER.info("Applying dependency filters. A total of ${config.filters.length} configured.")
        config.filters.each {
            data = it.filter(data)
        }

        LOGGER.info("Building report for project ${getProject().name}")
        config.renderer.render(data)
        LOGGER.info("Dependency license report for project ${getProject().name} created in ${config.outputDir}")
    }
}
