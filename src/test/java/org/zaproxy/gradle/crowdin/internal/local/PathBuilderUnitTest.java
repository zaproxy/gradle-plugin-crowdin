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
package org.zaproxy.gradle.crowdin.internal.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.zaproxy.gradle.crowdin.internal.configuration.DirFilenamePair;

/** Unit test for {@link PathBuilder}. */
class PathBuilderUnitTest {

    @Test
    void shouldThrowExceptionIfBaseDirnameIsNull() {
        // Given
        String baseDirname = null;
        // When / Then
        assertThrows(NullPointerException.class, () -> new PathBuilder(baseDirname));
    }

    @Test
    void shouldThrowExceptionIfTryingToReplaceDirectly() {
        // Given
        PathBuilder pathBuilder = new PathBuilder("baseDirname");
        String value = "file.txt";
        // When / Then
        assertThrows(UnsupportedOperationException.class, () -> pathBuilder.replace(value));
    }

    @Test
    void shouldBuildPathWithProvidedDirFilenameValues() {
        // Given
        String baseDirname = "baseDirname";
        PathBuilder replacer = new PathBuilder(baseDirname);
        DirFilenamePair dirFilename = new DirFilenamePair("/dir", "filename");
        String customFilename = null;
        Path baseDir = Paths.get("/dir");
        Path file = Paths.get("/path/to/file.txt");
        // When
        String result = replacer.build(dirFilename, customFilename, baseDir, file);
        // Then
        assertThat(result).isEqualTo("/dir/filename");
    }

    @Test
    void shouldBuildPathWithCustomFilename() {
        // Given
        String baseDirname = "baseDirname";
        PathBuilder replacer = new PathBuilder(baseDirname);
        DirFilenamePair dirFilename = new DirFilenamePair("/dir", "filename");
        String customFilename = "customFilename";
        Path baseDir = Paths.get("/dir");
        Path file = Paths.get("/path/to/file.txt");
        // When
        String result = replacer.build(dirFilename, customFilename, baseDir, file);
        // Then
        assertThat(result).isEqualTo("/dir/customFilename");
    }

    @Test
    void shouldBuildPathReplacingTokens() {
        // Given
        String baseDirname = "baseDirname";
        PathBuilder replacer = new PathBuilder(baseDirname);
        DirFilenamePair dirFilename =
                new DirFilenamePair(
                        "/x", "%base_dirname%/%file_pathname% | %file_name%%file_extension%");
        String customFilename = null;
        Path baseDir = Paths.get("/dir/a/");
        Path file = baseDir.resolve("path/to/file.txt");
        // When
        String result = replacer.build(dirFilename, customFilename, baseDir, file);
        // Then
        assertThat(result).isEqualTo("/x/baseDirname/path/to/file.txt | file.txt");
    }

    @Test
    void shouldBuildPathReplacingTokensAndUseCustomFilename() {
        // Given
        String baseDirname = "baseDirname";
        PathBuilder replacer = new PathBuilder(baseDirname);
        DirFilenamePair dirFilename =
                new DirFilenamePair(
                        "/x", "%base_dirname%/%file_pathname% | %file_name%%file_extension%");
        String customFilename =
                "custom filename: %base_dirname%/%file_pathname% | %file_name%%file_extension%";
        Path baseDir = Paths.get("/dir/a/");
        Path file = baseDir.resolve("path/to/file.txt");
        // When
        String result = replacer.build(dirFilename, customFilename, baseDir, file);
        // Then
        assertThat(result).isEqualTo("/x/custom filename: baseDirname/path/to/file.txt | file.txt");
    }

    @ParameterizedTest
    @ValueSource(strings = {"file", "file."})
    void shouldNotReplaceExtensionTokenEvenIfExtensionNotPresentInFile(String fileName) {
        // Given
        String baseDirname = "baseDirname";
        PathBuilder replacer = new PathBuilder(baseDirname);
        DirFilenamePair dirFilename =
                new DirFilenamePair(
                        "/x", "%base_dirname%/%file_pathname% | %file_name%%file_extension%");
        String customFilename = null;
        Path baseDir = Paths.get("/dir/a/");
        Path file = baseDir.resolve("path/to/" + fileName);
        // When
        String result = replacer.build(dirFilename, customFilename, baseDir, file);
        // Then
        assertThat(result).isEqualTo("/x/baseDirname/path/to/" + fileName + " | " + fileName);
    }
}
