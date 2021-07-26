/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2021 The ZAP Development Team
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
package org.zaproxy.gradle.crowdin.internal.configuration;

final class ValidationUtils {

    private ValidationUtils() {}

    static void validateNotEmpty(String name, String value) {
        validateNotNull(name, value);

        if (value.isEmpty()) {
            throw new ConfigurationException("The " + name + " must not be empty.");
        }
    }

    static void validateNotNull(String name, Object value) {
        if (value == null) {
            throw new ConfigurationException("The " + name + " must be defined.");
        }
    }
}
