package cd.go.plugin.gradlehelper

import groovy.text.GStringTemplateEngine
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class ExtensionInfoTask extends DefaultTask {

    @TaskAction
    void perform() {
        URL[] fileNames = [
                getClass().getResource('/plugin.xml'),
                getClass().getResource('/plugin.properties')
        ]

        def engine = new GStringTemplateEngine()

        File resourceDir = new File(project.buildDir.absolutePath, 'resources/main/')
        if (!resourceDir.exists()) {
            resourceDir.mkdirs()
        }

        Map<String, String> pluginInfo = addGitInfo()
        fileNames.each { f ->
            def template = engine.createTemplate(f).make(pluginInfo.withDefault { '' })
            new File(resourceDir, new File(f.file).getName()).write(template.toString())
        }

    }

    private Map<String, String> addGitInfo() {
        Map<String, String> config = getProject().gocdPlugin.pluginInfo.toHash()
        if (!GitInfoProvider.isGitRepo(project.projectDir.absolutePath)) {
            return config
        }

        String revision = GitInfoProvider.gitRevision(project.projectDir.absolutePath)
        String distVersion = GitInfoProvider.gitRevisionCount(project.projectDir.absolutePath)

        config += [
                'distVersion'  : distVersion,
                'gitRevision'  : revision == null ? '' : revision,
                "fullVersion"  : project.version,
                'pluginVersion': project.version,
        ]

        config
    }

}
