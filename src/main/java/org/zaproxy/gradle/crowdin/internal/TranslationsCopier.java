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
package org.zaproxy.gradle.crowdin.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.zaproxy.gradle.crowdin.internal.configuration.CrowdinProject;
import org.zaproxy.gradle.crowdin.internal.configuration.DirFilenamePair;
import org.zaproxy.gradle.crowdin.internal.configuration.Source;

public class TranslationsCopier {

    private final SimpleLogger logger;
    private final Path packagesDir;

    public TranslationsCopier(Path packagesDir, SimpleLogger logger) {
        this.packagesDir = packagesDir;
        this.logger = logger;
    }

    public void copy(CrowdinProject project, Path baseOutputDir) {
        if (project.getSources() == null || project.getSources().isEmpty()) {
            logger.lifecycle(
                    "No files to copy for project {}, no sources defined in the configuration.",
                    project.getId());
        }

        Path translationsPackage = packagesDir.resolve(project.getId() + ".zip").toAbsolutePath();
        if (Files.notExists(translationsPackage)) {
            logger.warn(
                    "No translation package found for project {} in {}",
                    project.getId(),
                    translationsPackage.getParent());
            return;
        }

        try (ZipFile translations = new ZipFile(translationsPackage.toFile())) {
            for (Source source : project.getSources()) {
                Path baseDir = baseOutputDir.resolve(getOutputDir(source));
                copyFiles(translations, source.getExportPattern(), baseDir);
            }
        } catch (IOException e) {
            throw new CrowdinException(
                    "Failed to read the translations package for project "
                            + project.getId()
                            + ", cause: "
                            + e.getMessage(),
                    e);
        }
    }

    private static String getOutputDir(Source source) {
        String outputDir = source.getOutputDir();
        if (outputDir != null) {
            return outputDir;
        }
        return source.getDir();
    }

    private void copyFiles(
            ZipFile translations, DirFilenamePair exportPattern, Path baseOutputDir) {
        String path = normalizeDir(exportPattern.getDir());
        int idx = path.length() + 1;
        translations.stream()
                .filter(e -> e.getName().startsWith(path))
                .forEach(
                        e -> {
                            if (e.isDirectory()) {
                                return;
                            }

                            Path outputFile = baseOutputDir.resolve(e.getName().substring(idx));
                            copyTo(outputFile, translations, e);
                        });
    }

    private void copyTo(Path outputFile, ZipFile translations, ZipEntry entry) {
        try {
            Files.createDirectories(outputFile.getParent());

            try (InputStream is = translations.getInputStream(entry)) {
                Files.copy(is, outputFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new CrowdinException(
                    "Failed to copy file from translation package, cuase: " + e.getMessage(), e);
        }
    }

    private static String normalizeDir(String dir) {
        return dir.substring(1, dir.length());
    }
}
