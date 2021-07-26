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

import com.crowdin.client.sourcefiles.model.ExportOptions;
import com.crowdin.client.sourcefiles.model.ImportOptions;
import java.nio.file.Path;
import java.util.Objects;

public class LocalFile {

    private final String name;
    private final Path path;
    private final String type;
    private final ImportOptions importOptions;
    private final ExportOptions exportOptions;
    private final String exportPattern;

    public LocalFile(
            String name,
            Path path,
            String type,
            ImportOptions importOptions,
            ExportOptions exportOptions,
            String exportPattern) {
        this.name = Objects.requireNonNull(name);
        this.path = path;
        this.type = type;
        this.importOptions = importOptions;
        this.exportOptions = Objects.requireNonNull(exportOptions);
        this.exportPattern = exportPattern;
    }

    public String getName() {
        return name;
    }

    public Path getPath() {
        return path;
    }

    public String getType() {
        return type;
    }

    public ImportOptions getImportOptions() {
        return importOptions;
    }

    public ExportOptions getExportOptions() {
        return exportOptions;
    }

    public String getExportPattern() {
        return exportPattern;
    }

    @Override
    public String toString() {
        return name;
    }
}
