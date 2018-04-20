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

package cd.go.plugin.gradlehelper

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.junit.Assert.*

class GitInfoProviderTest extends Specification {
    @Rule
    TemporaryFolder projectDir = new TemporaryFolder()

    void shouldReturnFalseIfProjectIsNotAGitProject() {
        given:
        def dir = projectDir.getRoot()

        when:
        def status = GitInfoProvider.isGitRepo(dir.getAbsolutePath())

        then:
        assertFalse status
    }

    void shouldReturnTrueIfProjectIsAGitProject() {
        given:
        def dir = projectDir.getRoot()
        "git init".execute(null, dir).waitFor()

        when:
        def status = GitInfoProvider.isGitRepo(dir.getAbsolutePath())

        then:
        assertTrue status
    }

    void shouldReturnRevisionOfGitRepo() {
        given:
        def dir = projectDir.getRoot()
        "git init".execute(null, dir).waitFor()

        when:
        def sha = GitInfoProvider.gitRevision(dir.getAbsolutePath())

        then:
        assertNull sha
    }

    void shouldErrorOutWhenDirIsNotAGitRepo() {
        given:
        def dir = projectDir.getRoot()

        when:
        GitInfoProvider.gitRevision(dir.getAbsolutePath())

        then:
        thrown RuntimeException
    }

    void shouldReturnRevisionCountOfGitRepo() {
        given:
        def dir = projectDir.getRoot()
        "git init".execute(null, dir).waitFor()
        new File(dir, "index.html").createNewFile()
        "git add .".execute(null, dir).waitFor()
        "git commit -a --no-edit --allow-empty --allow-empty-message".execute(null, dir).waitFor()

        when:
        def count = GitInfoProvider.gitRevisionCount(dir.getAbsolutePath())

        then:
        count == 1
    }

    void gitRevisionCount_shouldErrorOutWhenDirIsNotAGitRepo() {
        given:
        def dir = projectDir.getRoot()

        when:
        GitInfoProvider.gitRevisionCount(dir.getAbsolutePath())

        then:
        thrown RuntimeException
    }
}
