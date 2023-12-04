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

import com.crowdin.client.Client;
import com.crowdin.client.core.http.exceptions.HttpBadRequestException;
import com.crowdin.client.core.http.exceptions.HttpException;
import com.crowdin.client.core.model.Credentials;
import com.crowdin.client.core.model.ResponseList;
import com.crowdin.client.core.model.ResponseObject;
import com.crowdin.client.sourcefiles.model.Directory;
import com.crowdin.client.sourcefiles.model.FileInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.zaproxy.gradle.crowdin.CrowdinPluginException;
import org.zaproxy.gradle.crowdin.internal.SimpleLogger;
import org.zaproxy.gradle.crowdin.internal.configuration.ConfigurationException;
import org.zaproxy.gradle.crowdin.internal.configuration.CrowdinConfiguration;
import org.zaproxy.gradle.crowdin.internal.configuration.CrowdinProject;
import org.zaproxy.gradle.crowdin.internal.local.LocalVfs;
import org.zaproxy.gradle.crowdin.internal.remote.RemoteVfs;

public abstract class CrowdinTask extends DefaultTask {

    protected static final int PAGE_SIZE = 250;

    private CrowdinConfiguration crowdinConfiguration;
    private Client crowdinClient;
    private SimpleLogger simpleLogger;

    @SuppressWarnings("this-escape")
    protected CrowdinTask() {
        setGroup("Crowdin");

        getConfigurationTokens().convention(Collections.emptyMap());
    }

    @Input
    public abstract Property<String> getAuthToken();

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getConfigurationFile();

    @Input
    public abstract MapProperty<String, String> getConfigurationTokens();

    @Internal
    protected CrowdinConfiguration getCrowdinConfiguration() {
        if (crowdinConfiguration == null) {
            try {
                crowdinConfiguration =
                        CrowdinConfiguration.from(
                                getConfigurationFile().getAsFile().get().toPath(),
                                getConfigurationTokens().get());
            } catch (ConfigurationException e) {
                throw new CrowdinPluginException(e);
            }
        }
        return crowdinConfiguration;
    }

    @Internal
    protected Client getCrowdinClient() {
        if (crowdinClient == null) {
            crowdinClient = new Client(new Credentials(getAuthToken().getOrNull(), null));
        }
        return crowdinClient;
    }

    @Internal
    protected SimpleLogger getSimpleLogger() {
        if (simpleLogger == null) {
            simpleLogger = new LoggerWrapper(getLogger());
        }
        return simpleLogger;
    }

    protected <R> R apiRequest(Function<Client, R> access) {
        try {
            return access.apply(getCrowdinClient());
        } catch (HttpException e) {
            throw exceptionFor(e);
        } catch (HttpBadRequestException e) {
            throw exceptionFor(e);
        }
    }

    protected LocalVfs createLocalVfs(CrowdinProject crowdinProject) {
        try {
            return new LocalVfs(
                    getProject().getProjectDir().toPath(), crowdinProject, getSimpleLogger());
        } catch (IOException e) {
            throw new CrowdinPluginException(
                    "An error occurred while enumerating the local files, cause: " + e.getMessage(),
                    e);
        }
    }

    protected RemoteVfs createCrowdinVfs(CrowdinProject crowdinProject) {
        List<Directory> directories = new ArrayList<>();
        List<FileInfo> files = new ArrayList<>();
        try {
            fetchDirectories(crowdinProject.getId(), directories, PAGE_SIZE, 0);
            fetchFiles(crowdinProject.getId(), files, PAGE_SIZE, 0);
        } catch (HttpException e) {
            throw exceptionFor(e);
        } catch (HttpBadRequestException e) {
            throw exceptionFor(e);
        }
        return new RemoteVfs(directories, files);
    }

    private void fetchDirectories(long projectId, List<Directory> sink, int pageSize, int offset) {
        List<ResponseObject<Directory>> data =
                getCrowdinClient()
                        .getSourceFilesApi()
                        .listDirectories(projectId, null, null, null, null, pageSize, offset)
                        .getData();
        data.stream().map(ResponseObject::getData).forEach(sink::add);
        if (data.size() == pageSize) {
            fetchDirectories(projectId, sink, pageSize, offset + pageSize);
        }
    }

    private void fetchFiles(long projectId, List<FileInfo> sink, int pageSize, int offset) {
        ResponseList<? extends FileInfo> list =
                getCrowdinClient()
                        .getSourceFilesApi()
                        .listFiles(projectId, null, null, null, null, pageSize, offset);
        list.getData().stream().map(ResponseObject::getData).forEach(sink::add);
        if (list.getData().size() == pageSize) {
            fetchFiles(projectId, sink, pageSize, offset + pageSize);
        }
    }

    protected static CrowdinPluginException exceptionFor(HttpException e) {
        return new CrowdinPluginException(
                "An error occurred while accessing the Crowdin API: " + e.getError(), e);
    }

    protected static CrowdinPluginException exceptionFor(HttpBadRequestException e) {
        return new CrowdinPluginException(
                "An error occurred while accessing the Crowdin API: " + e.getErrors(), e);
    }
}
