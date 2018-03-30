package cd.go.plugin.gradlehelper

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class ExtensionInfoTask extends DefaultTask {
    @TaskAction
    void printInfo() {
        println "Extension configuration"
        println(true)
        GradleHelperExtension extension = getProject().gocdPlugin
        println(extension)
    }
}
