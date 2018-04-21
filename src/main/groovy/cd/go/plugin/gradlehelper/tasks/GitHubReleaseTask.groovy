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

import cd.go.plugin.gradlehelper.GitInfoProvider
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import okhttp3.*
import okhttp3.internal.http.StatusLine
import org.gradle.api.GradleException
import org.zeroturnaround.zip.ZipUtil

class GitHubReleaseTask extends AbstractTask {
    private static final JsonSlurper SLURPER = new JsonSlurper()
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8")
    private static final MediaType BINARY = MediaType.parse("application/octet-stream; charset=utf-8")
    private static final String USER_AGENT = "gocd-plugins-gradle-helper"
    private static final OkHttpClient CLIENT = new OkHttpClient()

    @Override
    void doTaskAction() {
        if (!GitInfoProvider.isGitRepo(projectDirPath)) {
            throw new GradleException("${projectDirPath} is not a git repo.")
        }

        HttpUrl httpUrl = HttpUrl.parse(extension.github.baseUrl).newBuilder()
                .addPathSegment("repos")
                .addPathSegment(extension.github.owner)
                .addPathSegment(extension.github.repo)
                .addPathSegment("releases")
                .build()

        Request request = new Request.Builder()
                .post(githubReleaseRequestBody())
                .url(httpUrl)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Authorization", "token ${extension.github.token}")
                .addHeader("Accept", extension.github.acceptHeader)
                .addHeader("Content-Type", JSON.toString())
                .build()


        logger.info "\n Creating GitHub release ${httpUrl.toString()}\n"
        Response response = CLIENT.newCall(request).execute()

        if (response.isSuccessful()) {
            logger.debug "GitHub release ${extension.github.tagName} successfully created."
            if (hasAssetsToUpload()) {
                logger.debug "Uploading assets to GitHub release ${extension.github.tagName}."
                uploadAssets(SLURPER.parseText(response.body().string()).upload_url)
            }
        } else {
            logger.error "Error while creating GitHub release ${extension.github.tagName}."
            logger.error "Request header: \n\n${request.headers().collect { "< $it" }.join('\n')}"
            logger.error "Response headers: \n\n${response.headers().collect { "< $it" }.join('\n')}"
            throw new GradleException("Failed to create GitHub release for repo ${extension.github.repo}:" +
                    " \n\t\t ${StatusLine.get(response)} ${response.body().string()}")
        }

    }

    private boolean hasAssetsToUpload() {
        return extension.github.assets != null &&
                extension.github.assets.length > 0
    }


    RequestBody githubReleaseRequestBody() {
        def jsonString = new JsonBuilder([
                name            : extension.github.name ?: "Version - ${pluginInfo.version}",
                tag_name        : extension.github.tagName ?: pluginInfo.version,
                body            : extension.github.body,
                prerelease      : extension.github.prerelease,
                draft           : extension.github.draft,
                target_commitish: extension.github.targetCommitish ?: 'master'
        ]).toPrettyString()

        logger.debug "Request body: ${jsonString}"
        return RequestBody.create(JSON, jsonString)
    }

    void uploadAssets(String uploadUrl) {
        extension.github.assets.each { asset ->
            if (asset == null || asset?.trim()?.length() == 0) {
                return
            }

            File file = getFileToUpload(asset)

            def newUploadUrl = uploadUrl.replace('{?name,label}', "?name=${file.name}&label=${file.name}")
            logger.debug "Asset upload url: ${newUploadUrl}"

            final Request uploadAssetRequest = new Request.Builder()
                    .post(RequestBody.create(BINARY, file))
                    .url(HttpUrl.parse(newUploadUrl))
                    .addHeader("User-Agent", USER_AGENT)
                    .addHeader("Authorization", "token ${extension.github.token}")
                    .addHeader("Accept", extension.github.acceptHeader)
                    .build()

            Response response = CLIENT.newCall(uploadAssetRequest).execute()

            if (response.isSuccessful()) {
                logger.debug "Asset ${asset} successfully uploaded."
                cleanIfRequiredFile(file)
            } else {
                logger.error("Failed to upload asset ${asset}")
                cleanIfRequiredFile(file)
            }
        }
    }

    File getFileToUpload(String asset) {
        File file = new File(asset)

        if (!file.exists()) {
            logger.info "Asset ${asset} does not exist."
            return
        }

        if (file.isFile()) {
            return file
        }

        String newFileName = file.getName() + ".zip"
        File zipFile = new File(file.parentFile, newFileName)
        ZipUtil.pack(file, zipFile)
        return zipFile
    }

    static void cleanIfRequiredFile(File file) {
        if (file.exists() && file.name.endsWith(".zip")) {
            file.delete()
        }
    }
}
