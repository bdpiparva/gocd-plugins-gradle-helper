package cd.go.plugin.gradlehelper

class GitInfoProvider {
    static boolean isGitRepo(String dir) {
        return "git -C ${dir} rev-parse".execute().waitFor() == 0
    }

    static String gitRevision(String dir) {
        if (isGitRepo(dir)) {
            def process = "git log -n 1 --format=%H".execute(null, new File(dir))
            process.waitFor()
            return success(process) ? process.text : null
        }

        throw new RuntimeException("Ouch! Not a git repo.")
    }

    static long gitRevisionCount(String dir) {
        if (isGitRepo(dir)) {
            def process = "git rev-list HEAD --count".execute(null, new File(dir))
            process.waitFor()
            return success(process) ? Long.parseLong(process.text.trim()) : 0
        }

        throw new RuntimeException("Ouch! Not a git repo.")
    }

    private static boolean success(Process process) {
        return process.exitValue() == 0
    }
}
