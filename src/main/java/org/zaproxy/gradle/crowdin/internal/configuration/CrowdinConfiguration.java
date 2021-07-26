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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.zaproxy.gradle.crowdin.internal.TokenReplacer;

/**
 * The configuration that defines the files to upload, how they are represented in Crowdin, and how
 * the translations are exported (copied) to the file system.
 */
public class CrowdinConfiguration {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    private List<CrowdinProject> projects;

    /**
     * Gets the projects of the configuration.
     *
     * @return the projects, never {@code null}.
     */
    public List<CrowdinProject> getProjects() {
        return projects;
    }

    private void resolve(TokenReplacer tokenReplacer) {
        if (projects == null) {
            throw new ConfigurationException("No projects specified in the configuration file.");
        }
        projects.forEach(e -> e.resolve(tokenReplacer));
    }

    /**
     * Creates a configuration from the given YAML file and using the given tokens.
     *
     * @param file the file containing the configuration.
     * @param tokens the tokens to replace in the configuration values.
     * @return the configuration, never {@code null}.
     * @throws ConfigurationException if an error occurred while reading the configuration or if it
     *     is invalid.
     */
    public static CrowdinConfiguration from(Path file, Map<String, String> tokens) {
        CrowdinConfiguration configuration;
        try {
            configuration = MAPPER.readValue(file.toFile(), CrowdinConfiguration.class);
        } catch (IOException e) {
            throw new ConfigurationException(
                    "Failed to parse the configuration file: " + e.getMessage(), e);
        }

        configuration.resolve(new TokenReplacer(tokens));

        return configuration;
    }
}
