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

/** The files that should be translated. */
public class FileSet {

    private String pattern;
    private String type;
    private String crowdinPathFilename;
    private String exportPatternFilename;
    private List<String> translatableElements;

    /**
     * Gets the glob pattern to select the files in the directory.
     *
     * @return the pattern, never {@code null} nor empty.
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Gets the type of the file(s).
     *
     * <p>Can be left undefined to use the default of the file.
     *
     * @return the type of the file, might be {@code null}.
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the filename that the files should have in Crowdin.
     *
     * <p>If not specified it is used the one defined in the {@code Source}.
     *
     * @return the filename for the files in Crowdin, might be {@code null}.
     */
    public String getCrowdinPathFilename() {
        return crowdinPathFilename;
    }

    /**
     * Gets the filename that the files should have in the translation package.
     *
     * <p>If not specified it is used the one defined in the {@code Source}.
     *
     * @return the filename for the translation package, might be {@code null}.
     */
    public String getExportPatternFilename() {
        return exportPatternFilename;
    }

    /**
     * Gets the translatable elements of a XML file.
     *
     * @return the translatable elements, might be {@code null}.
     */
    public List<String> getTranslatableElements() {
        return translatableElements;
    }

    void resolve(TokenReplacer tokenReplacer) {
        pattern = tokenReplacer.replace(pattern);
        ValidationUtils.validateNotEmpty("include pattern", pattern);
        type = tokenReplacer.replace(type);
        crowdinPathFilename = tokenReplacer.replace(crowdinPathFilename);
        exportPatternFilename = tokenReplacer.replace(exportPatternFilename);

        if (translatableElements != null) {
            translatableElements.replaceAll(e -> tokenReplacer.replace(e));
        }
    }
}
