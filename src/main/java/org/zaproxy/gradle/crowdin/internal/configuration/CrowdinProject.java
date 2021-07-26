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

import java.util.List;
import org.zaproxy.gradle.crowdin.internal.TokenReplacer;

/** A Crowdin project and its source files. */
public class CrowdinProject {

    private Long id;

    private List<Source> sources;

    /**
     * Gets the ID of the project.
     *
     * @return the id, never {@code null}.
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the sources of the project.
     *
     * <p>The sources are not required to allow to build and download the translation packages
     * without defining the whole configuration. For example, download in one location and use in
     * another.
     *
     * @return the sources, might be {@code null}.
     */
    public List<Source> getSources() {
        return sources;
    }

    void resolve(TokenReplacer tokenReplacer) {
        ValidationUtils.validateNotNull("project ID", id);

        if (sources != null) {
            sources.forEach(
                    e -> {
                        ValidationUtils.validateNotNull("source dir", e);
                        e.resolve(tokenReplacer);
                    });
        }
    }
}
