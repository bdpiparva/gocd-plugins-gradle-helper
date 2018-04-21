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

import org.gradle.api.GradleException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PluginInfo {
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginInfo.class)
    String id = ''
    String name = ''
    String group = ''
    String version = ''
    String serverVersion = ''
    String vendorName = ''
    String vendorUrl = ''

    Map<String, String> toHash() {
        return ['id'           : id,
                'name'         : name,
                'group'        : group,
                'version'      : version,
                'vendorUrl'    : vendorUrl,
                'vendorName'   : vendorName,
                'serverVersion': serverVersion,
        ]
    }

    void validate() {
        LOGGER.debug "Validating plugin info ${toHash()}"
        String.metaClass.isBlank = { delegate?.trim()?.isEmpty() }
        if (id.isBlank()) {
            throw new GradleException("Plugin `id` must not be blank.")
        }

        if (name.isBlank()) {
            throw new GradleException("Plugin name must not be blank.")
        }

        if (group.isBlank()) {
            throw new GradleException("Plugin `group` must not be blank.")
        }

        if (version.isBlank()) {
            throw new GradleException("Plugin `version` must not be blank.")
        }

        if (serverVersion.isBlank()) {
            throw new GradleException("Plugin GoCD server version must not be blank. Use property `serverVersion` to specify required GoCD server version for the plugin.")
        }
    }

}
