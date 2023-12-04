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

import com.crowdin.client.sourcefiles.model.AddDirectoryRequest;
import com.crowdin.client.sourcefiles.model.AddFileRequest;
import com.crowdin.client.sourcefiles.model.Directory;
import com.crowdin.client.sourcefiles.model.FileInfo;
import com.crowdin.client.sourcefiles.model.UpdateFileRequest;
import com.crowdin.client.storage.model.Storage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.api.tasks.TaskAction;
import org.zaproxy.gradle.crowdin.CrowdinPluginException;
import org.zaproxy.gradle.crowdin.internal.ApiClient;
import org.zaproxy.gradle.crowdin.internal.SourceFilesUploader;
import org.zaproxy.gradle.crowdin.internal.configuration.CrowdinConfiguration;
import org.zaproxy.gradle.crowdin.internal.configuration.CrowdinProject;
import org.zaproxy.gradle.crowdin.internal.local.LocalFile;
import org.zaproxy.gradle.crowdin.internal.remote.RemoteItem;

public abstract class UploadSourceFiles extends CrowdinTask {

    @SuppressWarnings("this-escape")
    public UploadSourceFiles() {
        setDescription("Uploads the source files to Crowdin.");
    }

    @TaskAction
    void upload() {
        CrowdinConfiguration configuration = getCrowdinConfiguration();

        Wrapper client = new Wrapper();
        for (CrowdinProject project : configuration.getProjects()) {
            new SourceFilesUploader(
                            client, project, createLocalVfs(project), createCrowdinVfs(project))
                    .upload();
        }
    }

    private class Wrapper implements ApiClient {

        private Storage addStorage(String name, Path path) {
            try (InputStream is = Files.newInputStream(path)) {
                return apiRequest(api -> api.getStorageApi().addStorage(name, is).getData());
            } catch (IOException e) {
                throw new CrowdinPluginException(
                        "An error occurred while reading a local file, cause: " + e.getMessage(),
                        e);
            }
        }

        @Override
        public Directory createDirectory(long projectId, Long parentDirectoryId, String name) {
            AddDirectoryRequest addDirectory = new AddDirectoryRequest();
            addDirectory.setName(name);
            addDirectory.setDirectoryId(parentDirectoryId);
            return apiRequest(
                    api -> api.getSourceFilesApi().addDirectory(projectId, addDirectory).getData());
        }

        @Override
        public FileInfo createFile(long projectId, Long parentId, LocalFile localFile) {
            Storage storage = addStorage(localFile.getName(), localFile.getPath());

            AddFileRequest addFile = new AddFileRequest();
            addFile.setName(localFile.getName());
            addFile.setStorageId(storage.getId());
            addFile.setDirectoryId(parentId);
            addFile.setType(localFile.getType());
            addFile.setImportOptions(localFile.getImportOptions());
            addFile.setExportOptions(localFile.getExportOptions());

            return apiRequest(api -> api.getSourceFilesApi().addFile(projectId, addFile).getData());
        }

        @Override
        public FileInfo updateFile(long projectId, long fileId, LocalFile localFile) {
            Storage storage = addStorage(localFile.getName(), localFile.getPath());
            UpdateFileRequest updateFile = new UpdateFileRequest();
            updateFile.setStorageId(storage.getId());
            updateFile.setImportOptions(localFile.getImportOptions());
            updateFile.setExportOptions(localFile.getExportOptions());

            return apiRequest(
                    api ->
                            api.getSourceFilesApi()
                                    .updateOrRestoreFile(projectId, fileId, updateFile)
                                    .getData());
        }

        @Override
        public void removeItem(long projectId, RemoteItem remoteBaseItem) {
            apiRequest(
                    api -> {
                        RemoteItem item = remoteBaseItem;
                        if (item.isDirectory()) {
                            api.getSourceFilesApi().deleteDirectory(projectId, item.getId());
                        } else {
                            api.getSourceFilesApi().deleteFile(projectId, item.getId());
                        }
                        return null;
                    });
        }
    }
}
