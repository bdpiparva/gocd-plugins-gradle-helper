package cd.go.plugin.gradlehelper

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class HelloTask extends DefaultTask {
    @TaskAction
    void doSomethin() {
        println GitInfoProvider.isGitRepo()
    }
}
