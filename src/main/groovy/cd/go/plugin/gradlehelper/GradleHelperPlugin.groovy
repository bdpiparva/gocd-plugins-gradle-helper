package cd.go.plugin.gradlehelper

import org.gradle.api.Plugin
import org.gradle.api.Project

class GradleHelperPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create('gocdPlugin', GradleHelperExtension, project.objects, project)
        project.tasks.create('extensionInfo', ExtensionInfoTask)
        project.tasks.create('hello', HelloTask)
    }
}
