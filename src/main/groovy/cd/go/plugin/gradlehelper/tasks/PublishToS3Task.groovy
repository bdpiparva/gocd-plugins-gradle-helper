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

package cd.go.plugin.gradlehelper.tasks

import groovy.json.JsonBuilder
import org.apache.commons.io.FileUtils
import org.gradle.api.GradleException

import java.security.MessageDigest

class PublishToS3Task extends AbstractTask {

    @Override
    void doTaskAction() {
        File deployDir = prepareDeployDirectory()
        createMetadataJsonFile(deployDir)

    }

    void createMetadataJsonFile(File deployDir) {
        File jsonFile = new File(deployDir, "metadata.json")
        def data = [
                version      : pluginInfo.fullVersion,
                location     : "plugins/${project.name}/${pluginInfo.fullVersion}/${extension.s3.pluginJar.name}",
                introduced_on: new Date().format("yyyy-MM-dd'T'hh:mm:ssXXX"),
                checksums    : [
                        md5   : checksum('MD5', extension.s3.pluginJar),
                        sha1  : checksum('SHA-1', extension.s3.pluginJar),
                        sha256: checksum('SHA-256', extension.s3.pluginJar),
                        sha512: checksum('SHA-512', extension.s3.pluginJar)
                ]
        ]

        jsonFile.write(new JsonBuilder(data).toPrettyString())
    }

    static String checksum(String type, File file) {
        return MessageDigest.getInstance(type).digest(file.readBytes()).encodeHex().toString()
    }

    File prepareDeployDirectory() {
        File deployDir = new File(buildDirPath, "deploy-dir/${pluginInfo.fullVersion}")
        FileUtils.deleteDirectory(deployDir)

        if (!deployDir.mkdirs()) {
            throw new GradleException("Failed to create ${deployDir.absolutePath}.")
        }

        deployDir.deleteOnExit()

        if (extension.s3.pluginJar.exists() && extension.s3.pluginJar.isFile()) {
            FileUtils.copyFileToDirectory(extension.s3.pluginJar, deployDir)
        } else {
            throw new GradleException(extension.s3.pluginJar.exists() ? "Plugin jar[${extension.s3.pluginJar.absolutePath}] is a directory." : "Plugin jar[${extension.s3.pluginJar.absolutePath}] does not exist.")
        }

        return deployDir
    }

}
