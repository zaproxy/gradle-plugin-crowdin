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
package org.zaproxy.gradle.crowdin.tasks;

import com.crowdin.client.core.model.DownloadLink;
import com.crowdin.client.core.model.ResponseObject;
import com.crowdin.client.translations.model.ProjectBuild;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.zaproxy.gradle.crowdin.CrowdinPluginException;
import org.zaproxy.gradle.crowdin.internal.configuration.CrowdinConfiguration;
import org.zaproxy.gradle.crowdin.internal.configuration.CrowdinProject;

public abstract class DownloadProjectTranslation extends CrowdinTask {

    public DownloadProjectTranslation() {
        setDescription("Downloads the latest project translation package.");

        // Translation package might change any time.
        getOutputs().upToDateWhen(t -> false);
    }

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    void download() {
        CrowdinConfiguration configuration = getCrowdinConfiguration();
        Path outputDirectory = getOutputDirectory().getAsFile().get().toPath();

        for (CrowdinProject project : configuration.getProjects()) {
            Optional<ProjectBuild> finishedBuild = getFinishedBuild(project);
            if (!finishedBuild.isPresent()) {
                getLogger().lifecycle("No builds finished for project {}.", project.getId());
                continue;
            }

            download(finishedBuild.get(), outputDirectory);
        }
    }

    private Optional<ProjectBuild> getFinishedBuild(CrowdinProject project) {
        return apiRequest(
                api ->
                        api.getTranslationsApi()
                                .listProjectBuilds(project.getId(), null, null, null).getData()
                                .stream()
                                .map(ResponseObject::getData)
                                .filter(e -> "finished".equals(e.getStatus()))
                                .findFirst());
    }

    private void download(ProjectBuild build, Path directory) {
        DownloadLink downloadLink =
                apiRequest(
                        api ->
                                api.getTranslationsApi()
                                        .downloadProjectTranslations(
                                                build.getProjectId(), build.getId())
                                        .getData());
        URL url;
        try {
            url = URI.create(downloadLink.getUrl()).toURL();
        } catch (MalformedURLException e) {
            throw new CrowdinPluginException(
                    "Failed to create the download URL for project "
                            + build.getProjectId()
                            + ", cause: "
                            + e.getMessage(),
                    e);
        }

        long projectId = build.getProjectId();
        Path file = directory.resolve(projectId + ".zip");
        try (InputStream in = url.openStream()) {
            Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new CrowdinPluginException(
                    "Failed to download the package for project "
                            + projectId
                            + ", cause: "
                            + e.getMessage(),
                    e);
        }
    }
}
