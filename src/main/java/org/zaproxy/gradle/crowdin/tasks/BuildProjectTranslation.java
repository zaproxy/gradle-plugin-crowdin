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

import com.crowdin.client.translations.model.CrowdinTranslationCreateProjectBuildForm;
import com.crowdin.client.translations.model.ProjectBuild;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.zaproxy.gradle.crowdin.internal.configuration.CrowdinConfiguration;
import org.zaproxy.gradle.crowdin.internal.configuration.CrowdinProject;

public abstract class BuildProjectTranslation extends CrowdinTask {

    @SuppressWarnings("this-escape")
    public BuildProjectTranslation() {
        setDescription("Builds the project translation package.");

        getTargetLanguageIds().convention(Collections.emptyList());
        getSkipUntranslatedStrings().convention(false);
        getSkipUntranslatedFiles().convention(false);
        getExportApprovedOnly().convention(false);

        getWaitForBuilds().convention(false);
    }

    @Input
    public abstract ListProperty<String> getTargetLanguageIds();

    @Input
    public abstract Property<Boolean> getSkipUntranslatedStrings();

    @Input
    public abstract Property<Boolean> getSkipUntranslatedFiles();

    @Input
    public abstract Property<Boolean> getExportApprovedOnly();

    @Input
    public abstract Property<Boolean> getWaitForBuilds();

    @TaskAction
    void build() {
        List<ProjectBuild> builds = startBuilds();

        boolean wait = getWaitForBuilds().get();
        if (wait) {
            waitForBuilds(builds);
        } else {
            getLogger().lifecycle("Not waiting for the builds to finish.");
        }
    }

    private List<ProjectBuild> startBuilds() {
        CrowdinTranslationCreateProjectBuildForm buildForm =
                new CrowdinTranslationCreateProjectBuildForm();
        buildForm.setTargetLanguageIds(getTargetLanguageIds().get());
        buildForm.setSkipUntranslatedStrings(getSkipUntranslatedStrings().get());
        buildForm.setSkipUntranslatedFiles(getSkipUntranslatedFiles().get());
        buildForm.setExportApprovedOnly(getExportApprovedOnly().get());

        CrowdinConfiguration configuration = getCrowdinConfiguration();
        List<ProjectBuild> builds = new ArrayList<>(configuration.getProjects().size());
        for (CrowdinProject project : configuration.getProjects()) {
            builds.add(startBuild(project, buildForm));
        }
        return builds;
    }

    private ProjectBuild startBuild(
            CrowdinProject project, CrowdinTranslationCreateProjectBuildForm buildForm) {
        ProjectBuild build =
                apiRequest(
                        api ->
                                api.getTranslationsApi()
                                        .buildProjectTranslation(project.getId(), buildForm)
                                        .getData());
        getLogger()
                .lifecycle(
                        "Build started for project {}, build ID: {}",
                        project.getId(),
                        build.getId());
        return build;
    }

    private void waitForBuilds(List<ProjectBuild> builds) {
        getLogger().lifecycle("Waiting for {} build(s) to finish.", builds.size());

        for (ProjectBuild build : builds) {
            if (waitForProjectBuildToFinish(build)) {
                getLogger().warn("Interrupted while waiting for builds to finish.");
                break;
            }
        }

        getLogger().lifecycle("All builds done.");
    }

    private boolean waitForProjectBuildToFinish(ProjectBuild build) {
        getLogger().lifecycle("Waiting for build of project {}", build.getProjectId());

        ProjectBuild currentBuild = build;
        while (isInProgressOrCreated(currentBuild)) {
            try {
                Thread.sleep(30_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return true;
            }
            currentBuild =
                    apiRequest(
                            api ->
                                    api.getTranslationsApi()
                                            .checkBuildStatus(build.getProjectId(), build.getId())
                                            .getData());
        }

        if ("finished".equals(currentBuild.getStatus())) {
            getLogger().lifecycle("Build done for project {}", build.getProjectId());
        } else {
            getLogger()
                    .lifecycle(
                            "Build not finished for project {}, status: {}",
                            build.getId(),
                            currentBuild.getStatus());
        }
        return false;
    }

    private static boolean isInProgressOrCreated(ProjectBuild build) {
        String status = build.getStatus();
        return "inProgress".equals(status) || "created".equals(status);
    }
}
