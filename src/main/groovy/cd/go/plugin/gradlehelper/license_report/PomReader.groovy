/*
 * Copyright 2018 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cd.go.plugin.gradlehelper.license_report

import cd.go.plugin.gradlehelper.license_report.models.FileType
import cd.go.plugin.gradlehelper.models.PomInfo
import groovy.util.slurpersupport.GPathResult
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedArtifact

import java.util.zip.ZipEntry
import java.util.zip.ZipFile

public class PomReader {
    private final Project project
    private final static XmlSlurper PARSER = new XmlSlurper(false, false)

    PomReader(Project project) {
        this.project = project
    }

    PomInfo read(ResolvedArtifact resolvedArtifact) {
        InputStream pomInputStream = getPomInputStream(resolvedArtifact.file)
        if (pomInputStream == null) {
//            return null
        }

        GPathResult result = PARSER.parse(pomInputStream)

        if (isPomFileValid(resolvedArtifact, result)) {
            return toPomInfo(result)
        }
    }

    PomInfo toPomInfo(GPathResult result) {
        if(result.parent) {
            return null
            //Implement me
        }
    }

    private InputStream getPomInputStream(File file) {
        switch (FileType.fromFilename(file.name)) {
            case FileType.POM:
                return new FileInputStream(file)
            case FileType.ARCHIVE:
                return getPomInputStreamFromArchive(file)
            case FileType.UNKNOWN:
                return null
        }
    }

    File getPomInputStreamFromArchive(File file) {
        ZipFile zipFile = new ZipFile(file, ZipFile.OPEN_READ)
        ZipEntry pomEntry = zipFile.entries().toList().find { ZipEntry entry ->
            entry.name.endsWith("pom.xml") || entry.name.endsWith(".pom")
        }

        if (pomEntry) {
            return zipFile.getInputStream(pomEntry)
        }

        return null
    }

    private static boolean isPomFileValid(ResolvedArtifact resolvedArtifact, GPathResult result) {
        if (result == null || resolvedArtifact == null) return false

        String groupIdFromPom = result.groupId?.text() ?: result.parent?.groupId?.text()

        return resolvedArtifact.moduleVersion.id.group == groupIdFromPom &&
                resolvedArtifact.moduleVersion.id.name == result.artifactId.text()
    }
}
