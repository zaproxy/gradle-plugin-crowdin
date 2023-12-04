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

import java.util.Collections;
import org.zaproxy.gradle.crowdin.internal.TokenReplacer;
import org.zaproxy.gradle.crowdin.internal.VfsNode;

/** A representation of a path, split into its (base) dir and the filename. */
public class DirFilenamePair {

    private String dir;
    private String filename;

    public DirFilenamePair() {}

    // Only for tests.
    public DirFilenamePair(String dir, String filename) {
        this.dir = dir;
        this.filename = filename;

        resolve(new TokenReplacer(Collections.emptyMap()));
    }

    /**
     * Gets the directory.
     *
     * <p>The directory will always start with a {@code /} and not contain a {@code /} at the end.
     *
     * @return the directory, never {@code null} nor empty.
     */
    public String getDir() {
        return dir;
    }

    /**
     * Gets the filename.
     *
     * @return the filename, never {@code null} nor empty.
     */
    public String getFilename() {
        return filename;
    }

    final void resolve(TokenReplacer tokenReplacer) {
        dir = normalize(tokenReplacer.replace(dir));
        ValidationUtils.validateNotEmpty("dir", dir);

        if (!dir.startsWith(VfsNode.SEPARATOR)) {
            throw new ConfigurationException(
                    "The dir path " + dir + " must start with " + VfsNode.SEPARATOR);
        }

        filename = tokenReplacer.replace(filename);
        ValidationUtils.validateNotEmpty("filename", filename);
    }

    private static String normalize(String value) {
        if (VfsNode.SEPARATOR.equals(value)) {
            return value;
        }

        if (value.endsWith(VfsNode.SEPARATOR)) {
            return value.substring(0, value.length() - 1);
        }

        return value;
    }
}
