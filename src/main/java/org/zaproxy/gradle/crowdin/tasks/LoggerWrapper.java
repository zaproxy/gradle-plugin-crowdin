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
package org.zaproxy.gradle.crowdin.tasks;

import java.util.Objects;
import org.gradle.api.logging.Logger;
import org.zaproxy.gradle.crowdin.internal.SimpleLogger;

class LoggerWrapper implements SimpleLogger {

    private final Logger logger;

    LoggerWrapper(Logger logger) {
        this.logger = Objects.requireNonNull(logger);
    }

    @Override
    public void lifecycle(String message, Object... objects) {
        logger.lifecycle(message, objects);
    }

    @Override
    public void warn(String message, Object... objects) {
        logger.warn(message, objects);
    }

    @Override
    public void error(String message, Object... objects) {
        logger.error(message, objects);
    }
}
