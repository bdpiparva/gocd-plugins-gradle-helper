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

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory

class LicenseReportTask extends AbstractTask {
    private Logger LOGGER = Logging.getLogger(LicenseReportTask.class)

    @Input
    String getConfigurationSnapshot() {
        return extension.licenseReport.snapshot
    }

    @OutputDirectory
    File getOutputFolder() {
        return new File(extension.licenseReport.outputDir)
    }

    @Override
    void doTaskAction() {
        LOGGER.info("Processing dependencies for project ${project.name}")
        getOutputFolder().mkdirs()

    }
}
