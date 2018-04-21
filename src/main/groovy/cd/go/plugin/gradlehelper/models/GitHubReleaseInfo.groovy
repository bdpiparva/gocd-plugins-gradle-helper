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

package cd.go.plugin.gradlehelper.models

class GitHubReleaseInfo {
    String baseUrl = "https://api.github.com"
    String acceptHeader = 'application/vnd.github.v3+json'
    String owner
    String repo
    String token
    String tagName
    String targetCommitish = "master"
    String name
    String body
    String[] assets
    boolean prerelease = false
    boolean draft = false
}
