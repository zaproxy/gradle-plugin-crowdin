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
package org.zaproxy.gradle.crowdin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;
import org.zaproxy.gradle.crowdin.tasks.BuildProjectTranslation;
import org.zaproxy.gradle.crowdin.tasks.CopyProjectTranslations;
import org.zaproxy.gradle.crowdin.tasks.DownloadProjectTranslation;
import org.zaproxy.gradle.crowdin.tasks.ListAllCrowdinFiles;
import org.zaproxy.gradle.crowdin.tasks.ListCrowdinFiles;
import org.zaproxy.gradle.crowdin.tasks.ListSourceFiles;
import org.zaproxy.gradle.crowdin.tasks.ListTranslationProgress;
import org.zaproxy.gradle.crowdin.tasks.UploadSourceFiles;

/** The plugin to integrate with Crowdin. */
public class CrowdinPlugin implements Plugin<Project> {

    /** The name of the extension to configure the plugin. */
    public static final String EXTENSION_NAME = "crowdin";

    /** The name of the extension to specify the configuration for Crowdin. */
    public static final String CONFIGURATION_EXTENSION_NAME = "configuration";

    public static final String UPLOAD_SOURCE_FILES_TASK_NAME = "crowdinUploadSourceFiles";

    public static final String BUILD_PROJECT_TRANSLATION_TASK_NAME =
            "crowdinBuildProjectTranslation";

    public static final String DOWNLOAD_PROJECT_TRANSLATION_TASK_NAME =
            "crowdinDownloadProjectTranslation";

    public static final String COPY_PROJECT_TRANSLATIONS_TASK_NAME =
            "crowdinCopyProjectTranslations";

    public static final String LIST_SOURCE_FILES_TASK_NAME = "crowdinListSourceFiles";

    public static final String LIST_CROWDIN_FILES_TASK_NAME = "crowdinListFiles";

    public static final String LIST_ALL_CROWDIN_FILES_TASK_NAME = "crowdinListAllFiles";

    public static final String LIST_TRANSLATION_PROGRESS_TASK_NAME =
            "crowdinListTranslationProgress";

    @Override
    public void apply(Project project) {
        CrowdinExtension extension =
                project.getExtensions().create(EXTENSION_NAME, CrowdinExtension.class);
        extension
                .getConfiguration()
                .getTranslationsPackageDirectory()
                .convention(
                        project.getLayout().getBuildDirectory().dir("crowdinTranslationPackages"));

        TaskContainer tasks = project.getTasks();

        tasks.register(
                UPLOAD_SOURCE_FILES_TASK_NAME,
                UploadSourceFiles.class,
                t -> {
                    t.getAuthToken().set(extension.getCredentials().getToken());
                    t.getConfigurationFile().set(extension.getConfiguration().getFile());
                    t.getConfigurationTokens().set(extension.getConfiguration().getTokens());
                });

        tasks.register(
                BUILD_PROJECT_TRANSLATION_TASK_NAME,
                BuildProjectTranslation.class,
                t -> {
                    t.getAuthToken().set(extension.getCredentials().getToken());
                    t.getConfigurationFile().set(extension.getConfiguration().getFile());
                    t.getConfigurationTokens().set(extension.getConfiguration().getTokens());
                });

        tasks.register(
                DOWNLOAD_PROJECT_TRANSLATION_TASK_NAME,
                DownloadProjectTranslation.class,
                t -> {
                    t.getAuthToken().set(extension.getCredentials().getToken());
                    t.getConfigurationFile().set(extension.getConfiguration().getFile());
                    t.getConfigurationTokens().set(extension.getConfiguration().getTokens());
                    t.getOutputDirectory()
                            .set(extension.getConfiguration().getTranslationsPackageDirectory());
                });

        tasks.register(
                COPY_PROJECT_TRANSLATIONS_TASK_NAME,
                CopyProjectTranslations.class,
                t -> {
                    t.getAuthToken().set(extension.getCredentials().getToken());
                    t.getConfigurationFile().set(extension.getConfiguration().getFile());
                    t.getConfigurationTokens().set(extension.getConfiguration().getTokens());
                    t.getTranslationsPackageDirectory()
                            .set(extension.getConfiguration().getTranslationsPackageDirectory());
                });

        tasks.register(
                LIST_SOURCE_FILES_TASK_NAME,
                ListSourceFiles.class,
                t -> {
                    t.getAuthToken().set(extension.getCredentials().getToken());
                    t.getConfigurationFile().set(extension.getConfiguration().getFile());
                    t.getConfigurationTokens().set(extension.getConfiguration().getTokens());
                });
        tasks.register(
                LIST_CROWDIN_FILES_TASK_NAME,
                ListCrowdinFiles.class,
                t -> {
                    t.getAuthToken().set(extension.getCredentials().getToken());
                    t.getConfigurationFile().set(extension.getConfiguration().getFile());
                    t.getConfigurationTokens().set(extension.getConfiguration().getTokens());
                });
        tasks.register(
                LIST_ALL_CROWDIN_FILES_TASK_NAME,
                ListAllCrowdinFiles.class,
                t -> {
                    t.getAuthToken().set(extension.getCredentials().getToken());
                    t.getConfigurationFile().set(extension.getConfiguration().getFile());
                    t.getConfigurationTokens().set(extension.getConfiguration().getTokens());
                });
        tasks.register(
                LIST_TRANSLATION_PROGRESS_TASK_NAME,
                ListTranslationProgress.class,
                t -> {
                    t.getAuthToken().set(extension.getCredentials().getToken());
                    t.getConfigurationFile().set(extension.getConfiguration().getFile());
                    t.getConfigurationTokens().set(extension.getConfiguration().getTokens());
                });
    }
}
