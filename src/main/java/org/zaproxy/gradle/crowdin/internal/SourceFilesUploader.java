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

import com.crowdin.client.sourcefiles.model.Directory;
import com.crowdin.client.sourcefiles.model.FileInfo;
import java.util.ArrayDeque;
import org.zaproxy.gradle.crowdin.internal.configuration.CrowdinProject;
import org.zaproxy.gradle.crowdin.internal.configuration.Source;
import org.zaproxy.gradle.crowdin.internal.local.LocalFile;
import org.zaproxy.gradle.crowdin.internal.local.LocalVfs;
import org.zaproxy.gradle.crowdin.internal.remote.RemoteItem;
import org.zaproxy.gradle.crowdin.internal.remote.RemoteVfs;

public class SourceFilesUploader {

    private final ApiClient clientWrapper;
    private final CrowdinProject project;
    private final LocalVfs localVfs;
    private final RemoteVfs remoteVfs;
    private final ArrayDeque<VfsNode<RemoteItem>> nodesToRemove;

    public SourceFilesUploader(
            ApiClient clientWrapper,
            CrowdinProject project,
            LocalVfs localVfs,
            RemoteVfs remoteVfs) {
        this.clientWrapper = clientWrapper;
        this.project = project;
        this.localVfs = localVfs;
        this.remoteVfs = remoteVfs;
        this.nodesToRemove = new ArrayDeque<>();
    }

    public void upload() {
        for (Source source : project.getSources()) {
            String crowdinDir = source.getCrowdinPath().getDir();
            if (localVfs.get(crowdinDir) == null) {
                continue;
            }

            localVfs.diff(remoteVfs, crowdinDir).traverse(this::processResults);
        }
        nodesToRemove.forEach(node -> clientWrapper.removeItem(project.getId(), node.getData()));
    }

    private void processResults(String path, VfsNode<DiffResult<LocalFile, RemoteItem>> node) {
        DiffResult<LocalFile, RemoteItem> result = node.getData();

        switch (result.getState()) {
            case ADDED:
                add(result.getRight());
                break;

            case SAME:
                update(result.getRight(), result.getLeft());
                break;

            case REMOVED:
                nodesToRemove.addFirst(result.getLeft());
                break;

            default:
        }
    }

    private void add(VfsNode<LocalFile> local) {
        if (local.hasData()) {
            uploadFile(local);
        } else {
            createDirectories(local);
        }
    }

    private void uploadFile(VfsNode<LocalFile> local) {
        Long parentId = getId(remoteVfs.get(local.getParent().getPath()));
        FileInfo remoteFile = clientWrapper.createFile(project.getId(), parentId, local.getData());
        remoteVfs.add(remoteFile);
    }

    private void createDirectories(VfsNode<LocalFile> local) {
        local.walk(
                (path, node) -> {
                    VfsNode<RemoteItem> directory = remoteVfs.get(path);
                    if (directory != null) {
                        return;
                    }

                    Long parentDirectoryId = null;
                    if (node.getParent() != null && !node.getParent().isRoot()) {
                        parentDirectoryId = getId(remoteVfs.get(node.getParent().getPath()));
                    }

                    Directory remoteDirectory =
                            clientWrapper.createDirectory(
                                    project.getId(), parentDirectoryId, node.getName());
                    remoteVfs.add(remoteDirectory);
                });
    }

    private void update(VfsNode<LocalFile> local, VfsNode<RemoteItem> remote) {
        if (local.hasData()) {
            FileInfo remoteFile =
                    clientWrapper.updateFile(project.getId(), getId(remote), local.getData());
            remoteVfs.add(remoteFile);
        }
    }

    private static Long getId(VfsNode<RemoteItem> node) {
        if (node.hasData()) {
            return node.getData().getId();
        }
        return null;
    }
}
