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

package com.github.jk1.license.render

import com.github.jk1.license.LicenseReport
import com.github.jk1.license.ModuleData

/**
 * Abstract class for renderers using only one license per module
 */
abstract class SingleInfoReportRenderer implements ReportRenderer {

    /**
     * @deprecated Use {@link com.github.jk1.license.render.LicenseDataCollector#singleModuleLicenseInfo} instead
     */
    @Deprecated
    protected List<String> moduleLicenseInfo(LicenseReport config, ModuleData data) {
        return LicenseDataCollector.singleModuleLicenseInfo(data)
    }
}
