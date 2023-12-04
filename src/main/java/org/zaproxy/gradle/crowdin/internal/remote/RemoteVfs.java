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
package org.zaproxy.gradle.crowdin.internal.remote;

import com.crowdin.client.sourcefiles.model.Directory;
import com.crowdin.client.sourcefiles.model.FileInfo;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.zaproxy.gradle.crowdin.internal.VfsNode;

public class RemoteVfs extends VfsNode<RemoteItem> {

    private final Map<Long, Directory> mapIdDirectory;

    @SuppressWarnings("this-escape")
    public RemoteVfs(List<Directory> directories, List<FileInfo> files) {
        mapIdDirectory =
                directories.stream()
                        .collect(Collectors.toMap(Directory::getId, Function.identity()));

        directories.forEach(this::add);
        files.forEach(this::add);
    }

    public void add(Directory directory) {
        if (add(createPath(directory), new RemoteDirectory(directory)) != null) {
            mapIdDirectory.put(directory.getId(), directory);
        }
    }

    private String createPath(Directory dir) {
        if (dir == null) {
            return "";
        }

        if (dir.getDirectoryId() == null) {
            return "/" + dir.getName();
        }
        return createPath(mapIdDirectory.get(dir.getDirectoryId())) + "/" + dir.getName();
    }

    public void add(FileInfo file) {
        add(createPath(file), new RemoteFile(file));
    }

    private String createPath(FileInfo file) {
        return createPath(mapIdDirectory.get(file.getDirectoryId())) + "/" + file.getName();
    }
}
