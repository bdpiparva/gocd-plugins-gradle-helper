package cd.go.plugin.gradlehelper.license_report

import com.github.jk1.license.filter.DependencyFilter
import com.github.jk1.license.importer.DependencyDataImporter
import com.github.jk1.license.render.ReportRenderer
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedDependency

import javax.inject.Inject

@CompileStatic
class LicenseReportConfig {
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
    ReportRenderer renderer
    DependencyDataImporter[] importers
    DependencyFilter[] filters
    String[] configurations
    String[] excludeGroups
    String[] excludes

    @Inject
    LicenseReportConfig(Project project) {
        outputDir = "${project.buildDir}/reports/dependency-license"
        renderer = new NoticeFileGenerator(outputDir, allowedLicenses)
        configurations = ['runtime']
        excludeGroups = [project.group]
        excludes = []
        importers = []
        filters = []
    }

    boolean isExcluded(ResolvedDependency module) {
        return excludeGroups.contains(module.moduleGroup) ||
                excludes.contains("$module.moduleGroup:$module.moduleName")
    }

    // configuration snapshot for the up-to-date check
    String snapshot() {
        StringBuilder builder = new StringBuilder()
        builder.append(renderer.class.name)
        importers.each { builder.append(it.class.name) }
        filters.each { builder.append(it.class.name) }
        configurations.each { builder.append(it) }
        excludeGroups.each { builder.append(it) }
        excludes.each { builder.append(it) }
        return builder.toString()
    }

    Map<String, String> toHash() {
        return ['licenseCheck'   : licenseCheck,
                'allowedLicenses': allowedLicenses,
                'outputDir'      : outputDir,
                'renderer'       : renderer.toString(),
                'importers'      : importers.length,
                'filters'        : filters.length,
                'configurations' : configurations,
                'excludes'       : excludes,
                'excludeGroups'  : excludeGroups
        ] as Map<String, String>
    }
}
