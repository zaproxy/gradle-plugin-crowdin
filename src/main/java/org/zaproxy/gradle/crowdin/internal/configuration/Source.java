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

/** The source files to translate. */
public class Source {

    private String dir;
    private String outputDir;

    private DirFilenamePair crowdinPath;
    private DirFilenamePair exportPattern;

    private List<FileSet> includes;

    /**
     * Gets the directory from where to read the source files and to where to copy the translated
     * files (if in the same directory),
     *
     * @return the directory from where the source files are read, never {@code null} nor empty.
     * @see #getOutputDir()
     */
    public String getDir() {
        return dir;
    }

    /**
     * Gets the directory to where the translated files should be copied (if different directory
     * than the source files).
     *
     * @return the directory to where the translated files are copied, might be {@code null}.
     * @see #getDir()
     */
    public String getOutputDir() {
        return outputDir;
    }

    /**
     * Gets the path (dir/filename) that the files should have in Crowdin.
     *
     * @return the path for the files in Crowdin, never {@code null}.
     */
    public DirFilenamePair getCrowdinPath() {
        return crowdinPath;
    }

    /**
     * Gets the path (dir/filename) that the files should have in the translation package.
     *
     * @return the path for the translation package, never {@code null}.
     */
    public DirFilenamePair getExportPattern() {
        return exportPattern;
    }

    /**
     * Gets the files included in the directory for translation.
     *
     * @return a list containing the files included, never {@code null}.
     */
    public List<FileSet> getIncludes() {
        return includes;
    }

    void resolve(TokenReplacer tokenReplacer) {
        dir = tokenReplacer.replace(dir);
        ValidationUtils.validateNotEmpty("source dir", dir);

        outputDir = tokenReplacer.replace(outputDir);

        ValidationUtils.validateNotNull("source crowdinPath", crowdinPath);
        crowdinPath.resolve(tokenReplacer);

        ValidationUtils.validateNotNull("source exportPattern", exportPattern);
        exportPattern.resolve(tokenReplacer);

        ValidationUtils.validateNotNull("source includes", includes);
        includes.forEach(
                e -> {
                    ValidationUtils.validateNotNull("source includes pattern", e);
                    e.resolve(tokenReplacer);
                });
    }
}
