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

import com.crowdin.client.sourcefiles.model.GeneralFileExportOptions;
import com.crowdin.client.sourcefiles.model.ImportOptions;
import com.crowdin.client.sourcefiles.model.XmlFileImportOptions;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.zaproxy.gradle.crowdin.internal.SimpleLogger;
import org.zaproxy.gradle.crowdin.internal.VfsNode;
import org.zaproxy.gradle.crowdin.internal.configuration.CrowdinProject;
import org.zaproxy.gradle.crowdin.internal.configuration.FileSet;
import org.zaproxy.gradle.crowdin.internal.configuration.Source;

public class LocalVfs extends VfsNode<LocalFile> {

    @SuppressWarnings("this-escape")
    public LocalVfs(Path projectDir, CrowdinProject crowdinProject, SimpleLogger logger)
            throws IOException {
        PathBuilder pathBuilder = new PathBuilder(projectDir.getFileName().toString());

        for (Source source : crowdinProject.getSources()) {
            Path dir = getDirectory(projectDir, source.getDir());

            for (FileSet fileSet : source.getIncludes()) {
                for (Path file : enumerateFiles(dir, fileSet.getPattern())) {
                    if (Files.size(file) == 0) {
                        logger.lifecycle(
                                "Ignoring empty file in project {}: {}",
                                crowdinProject.getId(),
                                file);
                        continue;
                    }

                    addFile(pathBuilder, source, dir, fileSet, file);
                }
            }
        }
    }

    private void addFile(
            PathBuilder pathBuilder, Source source, Path dir, FileSet fileSet, Path file) {
        String path =
                pathBuilder.build(
                        source.getCrowdinPath(), fileSet.getCrowdinPathFilename(), dir, file);
        boolean fileAdded =
                add(
                                path,
                                createLocalFile(
                                        extractName(path), pathBuilder, source, fileSet, dir, file))
                        != null;

        if (!fileAdded) {
            throw new UnsupportedOperationException(
                    "Crowdin file path ["
                            + path
                            + "] clash with ["
                            + get(path).getData().getPath().toAbsolutePath()
                            + "] and ["
                            + file.toAbsolutePath()
                            + "]");
        }
    }

    private static String extractName(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private static LocalFile createLocalFile(
            String name,
            PathBuilder pathBuilder,
            Source source,
            FileSet fileSet,
            Path baseDir,
            Path file) {
        ImportOptions importOptions = null;
        List<String> elements = fileSet.getTranslatableElements();
        if (elements != null && !elements.isEmpty()) {
            XmlFileImportOptions xmlOptions = new XmlFileImportOptions();
            xmlOptions.setTranslatableElements(elements);
            importOptions = xmlOptions;
        }

        String exportPattern =
                pathBuilder.build(
                        source.getExportPattern(),
                        fileSet.getExportPatternFilename(),
                        baseDir,
                        file);
        GeneralFileExportOptions exportOptions = new GeneralFileExportOptions();
        exportOptions.setExportPattern(exportPattern);

        return new LocalFile(
                name, file, fileSet.getType(), importOptions, exportOptions, exportPattern);
    }

    private static Path getDirectory(Path baseDir, String path) {
        Path dir = baseDir.resolve(path);

        if (Files.notExists(dir)) {
            throw new IllegalArgumentException(
                    "The path provided ["
                            + dir
                            + "] does not exist (resolved using ["
                            + baseDir
                            + "] and ["
                            + path
                            + "]).");
        }

        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException(
                    "The path provided ["
                            + dir
                            + "] is not a directory (resolved using ["
                            + baseDir
                            + "] and ["
                            + path
                            + "]).");
        }

        return dir;
    }

    private static List<Path> enumerateFiles(Path dir, String pattern) throws IOException {
        EnumeratorFileVisitor enumerator = new EnumeratorFileVisitor(dir, pattern);
        Files.walkFileTree(dir, Collections.emptySet(), Integer.MAX_VALUE, enumerator);
        return enumerator.getIncludedFiles();
    }

    private static class EnumeratorFileVisitor extends SimpleFileVisitor<Path> {

        private final Path baseDir;
        private final PathMatcher matcher;
        private final List<Path> includedFiles;

        public EnumeratorFileVisitor(Path baseDir, String pattern) {
            this.baseDir = baseDir;
            this.matcher = baseDir.getFileSystem().getPathMatcher("glob:" + pattern);
            this.includedFiles = new ArrayList<>();
        }

        public List<Path> getIncludedFiles() {
            return includedFiles;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (matcher.matches(baseDir.relativize(file))) {
                includedFiles.add(file);
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
