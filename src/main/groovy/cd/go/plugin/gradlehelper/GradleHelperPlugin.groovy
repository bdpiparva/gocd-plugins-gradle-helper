package cd.go.plugin.gradlehelper

import com.github.jk1.license.LicenseReportPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class GradleHelperPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.apply(LicenseReportPlugin.class)
        project.extensions.create('gocdPlugin', GradleHelperExtension, project.objects, project)

        project.tasks.create('extensionInfo', ExtensionInfoTask)

        project.tasks.create('hello', HelloTask)

        project.afterEvaluate {
            GradleHelperExtension gocdPlugin = project.extensions.gocdPlugin
            gocdPlugin.pluginInfo.validate()

            if (GitInfoProvider.isGitRepo(project.projectDir.absolutePath)) {
                project.version = "${gocdPlugin.pluginInfo.version}-${GitInfoProvider.gitRevisionCount(project.projectDir.absolutePath)}"
            } else {
                project.version = gocdPlugin.pluginInfo.version
            }
        }
    }
}
