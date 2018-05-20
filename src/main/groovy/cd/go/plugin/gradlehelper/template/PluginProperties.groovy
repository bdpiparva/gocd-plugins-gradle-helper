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

package cd.go.plugin.gradlehelper.template

class PluginProperties implements Template {
    static final name = "plugin.properties"
    static final content = '''#
# Copyright ${year} ThoughtWorks, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
pluginId=${id}
name=${name}
description=${description}
vendorName=${vendorName}
vendorUrl=${vendorUrl}
requiredServerVersion=${serverVersion}
# Plugin version info
pluginVersion=${version}
gitRevision=${gitRevision}
distVersion=${distVersion}
fullVersion=${fullVersion}
    '''

    @Override
    String name() {
        return name
    }

    @Override
    String content() {
        return content
    }
}
