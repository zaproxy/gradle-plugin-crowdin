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

import java.nio.file.Path;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import org.zaproxy.gradle.crowdin.internal.TranslationsCopier;
import org.zaproxy.gradle.crowdin.internal.configuration.CrowdinConfiguration;
import org.zaproxy.gradle.crowdin.internal.configuration.CrowdinProject;

public abstract class CopyProjectTranslations extends CrowdinTask {

    @SuppressWarnings("this-escape")
    public CopyProjectTranslations() {
        setDescription("Copies the project translations to respective directories.");
    }

    @Optional
    @Override
    public abstract Property<String> getAuthToken();

    @InputDirectory
    public abstract DirectoryProperty getTranslationsPackageDirectory();

    @Option(
            option = "from",
            description = "The file system path to the directory with the translations package.")
    public void from(String dir) {
        getTranslationsPackageDirectory().set(getProject().file(dir));
    }

    @TaskAction
    void copy() {
        CrowdinConfiguration configuration = getCrowdinConfiguration();
        Path baseDir = getProject().getProjectDir().toPath();
        Path packagesDir = getTranslationsPackageDirectory().getAsFile().get().toPath();

        TranslationsCopier copier = new TranslationsCopier(packagesDir, getSimpleLogger());
        for (CrowdinProject project : configuration.getProjects()) {
            copier.copy(project, baseDir);
        }
    }
}
