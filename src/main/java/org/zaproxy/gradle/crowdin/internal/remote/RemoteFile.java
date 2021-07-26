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

import com.crowdin.client.sourcefiles.model.FileInfo;

public class RemoteFile implements RemoteItem {

    private final FileInfo data;

    public RemoteFile(FileInfo data) {
        this.data = data;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public Long getId() {
        return data.getId();
    }

    public FileInfo getData() {
        return data;
    }
}
