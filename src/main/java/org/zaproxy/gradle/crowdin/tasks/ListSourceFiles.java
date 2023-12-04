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

import java.util.function.Function;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import org.zaproxy.gradle.crowdin.internal.VfsNode;
import org.zaproxy.gradle.crowdin.internal.configuration.CrowdinConfiguration;
import org.zaproxy.gradle.crowdin.internal.configuration.CrowdinProject;
import org.zaproxy.gradle.crowdin.internal.local.LocalFile;

public abstract class ListSourceFiles extends CrowdinTask {

    @SuppressWarnings("this-escape")
    public ListSourceFiles() {
        setDescription("Lists the source files.");

        getExportPattern().convention(false);
    }

    @Optional
    @Override
    public abstract Property<String> getAuthToken();

    @Input
    @Optional
    public abstract Property<Boolean> getExportPattern();

    @Option(
            option = "exportPattern",
            description = "If the nodes should also display the export pattern.")
    public void includeExportPattern() {
        getExportPattern().set(true);
    }

    @TaskAction
    void list() {
        CrowdinConfiguration crowdinConfiguration = getCrowdinConfiguration();

        for (CrowdinProject project : crowdinConfiguration.getProjects()) {
            System.out.println("Project " + project.getId());
            createLocalVfs(project).print(System.out, getAction());
            System.out.println();
        }
    }

    private Function<VfsNode<LocalFile>, String> getAction() {
        boolean includeExportPattern = getExportPattern().get();
        if (includeExportPattern) {
            return node ->
                    node.getData() == null ? null : " [" + node.getData().getExportPattern() + "]";
        }
        return node -> null;
    }
}
