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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.zaproxy.gradle.crowdin.internal.configuration.CrowdinProjectAssert.assertThat;
import static org.zaproxy.gradle.crowdin.internal.configuration.DirFilenamePairAssert.assertThat;
import static org.zaproxy.gradle.crowdin.internal.configuration.FileSetAssert.assertThat;
import static org.zaproxy.gradle.crowdin.internal.configuration.SourceAssert.assertThat;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Unit test for {@link CrowdinConfiguration}. */
class CrowdinConfigurationUnitTest {

    @Test
    void shouldCreateConfigurationWithJustProjects() {
        // Given
        Path file = getResource("valid-only-projects.yml");
        // When
        CrowdinConfiguration configuration =
                CrowdinConfiguration.from(file, Collections.emptyMap());
        // Then
        assertThat(configuration.getProjects()).hasSize(2);
        assertThat(configuration.getProjects().get(0)).hasId(123L);
        assertThat(configuration.getProjects().get(1)).hasId(345L);
    }

    @Test
    void shouldReadValidConfiguration() {
        // Given
        Path file = getResource("valid-all-properties.yml");
        // When
        CrowdinConfiguration configuration =
                CrowdinConfiguration.from(file, Collections.emptyMap());
        // Then
        assertThat(configuration.getProjects()).hasSize(1);
        CrowdinProject project = configuration.getProjects().get(0);
        assertThat(project).hasId(123L);
        assertThat(project.getSources()).hasSize(1);
        Source source = project.getSources().get(0);
        assertThat(source).hasDir("src/main/resources");
        assertThat(source).hasOutputDir("src/main/resources/translated");
        assertThat(source.getCrowdinPath()).hasDir("/data");
        assertThat(source.getCrowdinPath()).hasFilename("%file_pathname%");
        assertThat(source.getExportPattern()).hasDir("/data/export");
        assertThat(source.getExportPattern()).hasFilename("export_%file_pathname%");
        assertThat(source.getIncludes()).hasSize(1);
        FileSet fileSet = source.getIncludes().get(0);
        assertThat(fileSet).hasPattern("Messages.properties");
        assertThat(fileSet).hasType("properties");
        assertThat(fileSet).hasCrowdinPathFilename("Strings.properties");
        assertThat(fileSet).hasExportPatternFilename("MessagesA.properties");
        assertThat(fileSet.getTranslatableElements()).hasSize(1);
        assertThat(fileSet.getTranslatableElements().get(0)).isEqualTo("xpath");
    }

    @Test
    void shouldReadValidConfigurationWithDirsAsSlash() {
        // Given
        Path file = getResource("valid-all-properties-dir-slash.yml");
        // When
        CrowdinConfiguration configuration =
                CrowdinConfiguration.from(file, Collections.emptyMap());
        // Then
        assertThat(configuration.getProjects()).hasSize(1);
        CrowdinProject project = configuration.getProjects().get(0);
        assertThat(project.getSources()).hasSize(1);
        Source source = project.getSources().get(0);
        assertThat(source.getCrowdinPath()).hasDir("/");
        assertThat(source.getExportPattern()).hasDir("/");
    }

    @Test
    void shouldReadValidConfigurationStrippingSlashAtEndInDirs() {
        // Given
        Path file = getResource("valid-all-properties-dir-slash-end.yml");
        // When
        CrowdinConfiguration configuration =
                CrowdinConfiguration.from(file, Collections.emptyMap());
        // Then
        assertThat(configuration.getProjects()).hasSize(1);
        CrowdinProject project = configuration.getProjects().get(0);
        assertThat(project.getSources()).hasSize(1);
        Source source = project.getSources().get(0);
        assertThat(source.getCrowdinPath()).hasDir("/path/a");
        assertThat(source.getExportPattern()).hasDir("/path/b");
    }

    @Test
    void shouldReplaceTokensInConfiguration() {
        // Given
        Path file = getResource("valid-all-properties-tokens.yml");
        Map<String, String> tokens = new HashMap<>();
        tokens.put("%tokenA%", "A");
        tokens.put("%tokenB%", "B");
        // When
        CrowdinConfiguration configuration = CrowdinConfiguration.from(file, tokens);
        // Then
        assertThat(configuration.getProjects()).hasSize(1);
        CrowdinProject project = configuration.getProjects().get(0);
        assertThat(project).hasId(123L);
        assertThat(project.getSources()).hasSize(1);
        Source source = project.getSources().get(0);
        assertThat(source).hasDir("dir/A/B");
        assertThat(source).hasOutputDir("outputDir/A/B");
        assertThat(source.getCrowdinPath()).hasDir("/crowdinPath/dir/A/B");
        assertThat(source.getCrowdinPath()).hasFilename("crowdinPath/filename/A B");
        assertThat(source.getExportPattern()).hasDir("/exportPattern/dir/A/B");
        assertThat(source.getExportPattern()).hasFilename("exportPattern/filename/A B");
        assertThat(source.getIncludes()).hasSize(1);
        FileSet fileSet = source.getIncludes().get(0);
        assertThat(fileSet).hasPattern("pattern/A/B.html");
        assertThat(fileSet).hasType("type A B");
        assertThat(fileSet).hasCrowdinPathFilename("crowdinPathFilename A B");
        assertThat(fileSet).hasExportPatternFilename("exportPatternFilename A B");
        assertThat(fileSet.getTranslatableElements()).hasSize(2);
        assertThat(fileSet.getTranslatableElements().get(0)).isEqualTo("translatableElement1 A B");
        assertThat(fileSet.getTranslatableElements().get(1)).isEqualTo("translatableElement2 A B");
    }

    @Nested
    class Validations {

        @Nested
        class Projects {
            @Test
            void shouldFailWithNoProjects() {
                // Given
                Path file = getResource("invalid-projects-missing.yml");
                // When / Then
                assertInvalidConfiguration(file, "Failed to parse the configuration file: ");
            }

            @Test
            void shouldFailWithEmptyProjects() {
                // Given
                Path file = getResource("invalid-projects-empty.yml");
                // When / Then
                assertInvalidConfiguration(
                        file, "No projects specified in the configuration file.");
            }

            @ParameterizedTest
            @ValueSource(
                    strings = {"invalid-project-id-missing.yml", "invalid-project-id-empty.yml"})
            void shouldFailWithProjectIdAbsentOrEmpty(String path) {
                // Given
                Path file = getResource(path);
                // When / Then
                assertInvalidConfiguration(file, "The project ID must be defined.");
            }

            @Test
            void shouldFailWithProjectIdNotNumber() {
                // Given
                Path file = getResource("invalid-project-id-not-number.yml");
                // When / Then
                assertInvalidConfiguration(
                        file, "Failed to parse the configuration file: Cannot deserialize value");
            }

            @Nested
            class Sources {

                @Test
                void shouldNotFailWithEmptySources() {
                    // Given
                    Path file = getResource("valid-project-sources-empty.yml");
                    // When
                    CrowdinConfiguration configuration =
                            CrowdinConfiguration.from(file, Collections.emptyMap());
                    // Then
                    assertThat(configuration.getProjects()).hasSize(1);
                    assertThat(configuration.getProjects().get(0)).hasId(123L);
                    assertThat(configuration.getProjects().get(0).getSources()).isNull();
                }

                @Test
                void shouldFailWithSourceWithNoDir() {
                    // Given
                    Path file = getResource("invalid-project-sources-dir-missing.yml");
                    // When / Then
                    assertInvalidConfiguration(file, "The source dir must be defined.");
                }

                @Test
                void shouldFailWithSourceWithEmptyDir() {
                    // Given
                    Path file = getResource("invalid-project-sources-dir-empty.yml");
                    // When / Then
                    assertInvalidConfiguration(file, "The source dir must not be empty.");
                }

                @ParameterizedTest
                @ValueSource(
                        strings = {
                            "invalid-project-sources-crowdinpath-missing.yml",
                            "invalid-project-sources-crowdinpath-dir-missing.yml"
                        })
                void shouldFailWithSourceWithNoCrowdinPathNorDir(String path) {
                    // Given
                    Path file = getResource(path);
                    // When / Then
                    assertInvalidConfiguration(file, "The source crowdinPath must be defined.");
                }

                @Test
                void shouldFailWithSourceWithCrowdinPathDirInvalid() {
                    // Given
                    Path file = getResource("invalid-project-sources-crowdinpath-dir-invalid.yml");
                    // When / Then
                    assertInvalidConfiguration(file, "must start with /");
                }

                @Test
                void shouldFailWithSourceWithNoCrowdinPathFilename() {
                    // Given
                    Path file =
                            getResource("invalid-project-sources-crowdinpath-filename-missing.yml");
                    // When / Then
                    assertInvalidConfiguration(file, "The filename must be defined.");
                }

                @ParameterizedTest
                @ValueSource(
                        strings = {
                            "invalid-project-sources-exportpattern-missing.yml",
                            "invalid-project-sources-exportpattern-dir-missing.yml"
                        })
                void shouldFailWithSourceWithNoCExportPathNorDir(String path) {
                    // Given
                    Path file = getResource(path);
                    // When / Then
                    assertInvalidConfiguration(file, "The source exportPattern must be defined.");
                }

                @Test
                void shouldFailWithSourceWithExportPatternDirInvalid() {
                    // Given
                    Path file =
                            getResource("invalid-project-sources-exportpattern-dir-invalid.yml");
                    // When / Then
                    assertInvalidConfiguration(file, "must start with /");
                }

                @Test
                void shouldFailWithSourceWithNoExportPatternFilename() {
                    // Given
                    Path file =
                            getResource(
                                    "invalid-project-sources-exportpattern-filename-missing.yml");
                    // When / Then
                    assertInvalidConfiguration(file, "The filename must be defined.");
                }

                @Test
                void shouldFailWithSourceWithNoInclude() {
                    // Given
                    Path file = getResource("invalid-project-sources-include-missing.yml");
                    // When / Then
                    assertInvalidConfiguration(file, "The source includes must be defined.");
                }

                @Test
                void shouldFailWithSourceWithIncludeWithoutPattern() {
                    // Given
                    Path file = getResource("invalid-project-sources-include-pattern-missing.yml");
                    // When / Then
                    assertInvalidConfiguration(
                            file, "The source includes pattern must be defined.");
                }

                @Test
                void shouldFailWithSourceWithIncludeWithEmptyPattern() {
                    // Given
                    Path file = getResource("invalid-project-sources-include-pattern-empty.yml");
                    // When / Then
                    assertInvalidConfiguration(file, "The include pattern must not be empty.");
                }
            }
        }
    }

    private static void assertInvalidConfiguration(Path file, String message) {
        Map<String, String> tokens = Collections.emptyMap();
        ConfigurationException e =
                assertThrows(
                        ConfigurationException.class,
                        () -> CrowdinConfiguration.from(file, tokens));
        assertThat(e).hasMessageContaining(message);
    }

    private static Path getResource(String resourcePath) {
        try {
            URL resource = CrowdinConfigurationUnitTest.class.getResource(resourcePath);
            assertThat(resource).as("Resource not found: %s", resourcePath).isNotNull();
            return Paths.get(resource.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
