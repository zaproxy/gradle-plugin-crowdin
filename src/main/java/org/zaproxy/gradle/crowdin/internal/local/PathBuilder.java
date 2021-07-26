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

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.zaproxy.gradle.crowdin.internal.TokenReplacer;
import org.zaproxy.gradle.crowdin.internal.configuration.DirFilenamePair;

/**
 * A path builder.
 *
 * <p>Allows to create the paths for Crowdin and Export Pattern.
 */
class PathBuilder extends TokenReplacer {

    private static final String BASE_DIRNAME_TOKEN = "%base_dirname%";
    private static final String FILE_PATHNAME_TOKEN = "%file_pathname%";
    private static final String FILE_NAME_TOKEN = "%file_name%";
    private static final String FILE_EXTENSION_TOKEN = "%file_extension%";

    private final Map<String, String> tokens;

    /**
     * Constructs a {@code PathBuilder} with the given base dirname.
     *
     * @param baseDirname the bas dirname.
     */
    PathBuilder(String baseDirname) {
        Objects.requireNonNull(baseDirname);

        tokens = new HashMap<>();
        tokens.put(BASE_DIRNAME_TOKEN, baseDirname);
    }

    /**
     * Builds the path from the given data.
     *
     * @param dirFilenamePair the dir/filename pair.
     * @param customFilename the custom filename, overrides the filename of the {@code
     *     dirFilenamePair} if not {@code null}.
     * @param baseDir the base directory where the files are.
     * @param file the file.
     * @return the path.
     */
    String build(DirFilenamePair dirFilenamePair, String customFilename, Path baseDir, Path file) {
        String dir = dirFilenamePair.getDir();
        String filename = dirFilenamePair.getFilename();
        if (customFilename != null) {
            filename = customFilename;
        }

        String fileName = file.getFileName().toString();
        tokens.put(FILE_PATHNAME_TOKEN, baseDir.relativize(file).toString());
        tokens.put(FILE_NAME_TOKEN, extractName(fileName));
        tokens.put(FILE_EXTENSION_TOKEN, extractExtension(fileName));
        updateTokens(tokens);

        return super.replace(dir + "/" + filename);
    }

    /**
     * @throws UnsupportedOperationException use {@link #build(DirFilenamePair, String, Path, Path)}
     *     instead.
     */
    @Override
    public String replace(String value) {
        throw new UnsupportedOperationException("Use build method instead.");
    }

    /**
     * Extracts the name portion of the given filename.
     *
     * @param filename the filename to extract the name from.
     * @return the name, or {@code filename} if it has no extension.
     */
    private static String extractName(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx != -1) {
            return filename.substring(0, idx);
        }
        return filename;
    }

    /**
     * Extracts the extension portion of the given filename.
     *
     * @param filename the filename to extract the extension from.
     * @return the extension (includes the dot), or empty if none.
     */
    private static String extractExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx != -1) {
            return filename.substring(idx);
        }
        return "";
    }
}
