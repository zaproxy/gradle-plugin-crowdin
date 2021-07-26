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

import org.gradle.api.tasks.TaskAction;
import org.zaproxy.gradle.crowdin.internal.VfsNode;
import org.zaproxy.gradle.crowdin.internal.configuration.CrowdinConfiguration;
import org.zaproxy.gradle.crowdin.internal.configuration.CrowdinProject;
import org.zaproxy.gradle.crowdin.internal.configuration.Source;
import org.zaproxy.gradle.crowdin.internal.remote.RemoteItem;
import org.zaproxy.gradle.crowdin.internal.remote.RemoteVfs;

public abstract class ListCrowdinFiles extends CrowdinTask {

    public ListCrowdinFiles() {
        setDescription("Lists the files in Crowdin.");
    }

    @TaskAction
    void list() {
        CrowdinConfiguration crowdinConfiguration = getCrowdinConfiguration();

        for (CrowdinProject project : crowdinConfiguration.getProjects()) {
            System.out.println("Project " + project.getId());

            RemoteVfs crowdinVfs = createCrowdinVfs(project);
            for (Source source : project.getSources()) {
                VfsNode<RemoteItem> node = crowdinVfs.get(source.getCrowdinPath().getDir());
                if (node != null) {
                    node.print(System.out);
                } else {
                    System.err.println("No files present in Crowdin.");
                }
            }
            System.out.println();
        }
    }
}
