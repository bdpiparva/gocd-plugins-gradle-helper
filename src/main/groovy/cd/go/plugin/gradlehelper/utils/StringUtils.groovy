package cd.go.plugin.gradlehelper.utils

class StringUtils {
    static String chomp(String str) {
        if (str?.trim()?.isEmpty()) {
            return str
        } else if (str.length() == 1) {
            char ch = str.charAt(0)
            return ch != '\r' && ch != '\n' ? str : ""
        } else {
            int lastIdx = str.length() - 1
            char last = str.charAt(lastIdx)
            if (last == '\n') {
                if (str.charAt(lastIdx - 1) == '\r') {
                    --lastIdx
                }
            } else if (last != '\r') {
                ++lastIdx
            }

            return str.substring(0, lastIdx)
        }
    }
}
