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

import com.crowdin.client.core.http.exceptions.HttpBadRequestException;
import com.crowdin.client.core.http.exceptions.HttpException;
import com.crowdin.client.core.model.ResponseList;
import com.crowdin.client.core.model.ResponseObject;
import com.crowdin.client.translationstatus.model.LanguageProgress;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import org.zaproxy.gradle.crowdin.internal.configuration.CrowdinConfiguration;
import org.zaproxy.gradle.crowdin.internal.configuration.CrowdinProject;

public abstract class ListTranslationProgress extends CrowdinTask {

    public ListTranslationProgress() {
        setDescription("Lists the translation progress of the projects.");

        getSortByProgress().convention(false);
    }

    @Input
    public abstract Property<Boolean> getSortByProgress();

    @Option(
            option = "sort-progress",
            description =
                    "If the languages should be sorted by progress (most translated shown first).")
    public void sortProgress() {
        getSortByProgress().set(true);
    }

    @TaskAction
    void list() {
        CrowdinConfiguration crowdinConfiguration = getCrowdinConfiguration();

        for (CrowdinProject project : crowdinConfiguration.getProjects()) {
            System.out.println("Project " + project.getId());
            getProgress(project).stream()
                    .map(ListTranslationProgress::toLanguageEntry)
                    .sorted(getComparator())
                    .forEach(l -> System.out.println("  " + l));
            System.out.println();
        }
    }

    private List<LanguageProgress> getProgress(CrowdinProject project) {
        List<LanguageProgress> languages = new ArrayList<>();
        try {
            fetchLanguages(project.getId(), languages, PAGE_SIZE, 0);
        } catch (HttpException e) {
            throw exceptionFor(e);
        } catch (HttpBadRequestException e) {
            throw exceptionFor(e);
        }
        return languages;
    }

    private void fetchLanguages(
            long projectId, List<LanguageProgress> sink, int pageSize, int offset) {
        ResponseList<LanguageProgress> list =
                getCrowdinClient()
                        .getTranslationStatusApi()
                        .getProjectProgress(projectId, pageSize, offset, null);
        list.getData().stream().map(ResponseObject::getData).forEach(sink::add);
        if (list.getData().size() == pageSize) {
            fetchLanguages(projectId, sink, pageSize, offset + pageSize);
        }
    }

    private Comparator<? super LanguageEntry> getComparator() {
        boolean sortByProgress = getSortByProgress().get();
        if (sortByProgress) {
            return Comparator.comparing(LanguageEntry::getProgress).reversed();
        }
        return Comparator.comparing(LanguageEntry::getName);
    }

    private static LanguageEntry toLanguageEntry(LanguageProgress progress) {
        return new LanguageEntry(progress.getLanguageId(), progress.getTranslationProgress());
    }

    private static class LanguageEntry {

        private final String id;
        private final String name;
        private final int progress;

        LanguageEntry(String id, int progress) {
            this.id = id;
            this.name = createLocale(id).getDisplayName(Locale.ROOT);
            this.progress = progress;
        }

        String getName() {
            return name;
        }

        int getProgress() {
            return progress;
        }

        @Override
        public String toString() {
            return name + " [" + id + "] " + progress + "%";
        }

        private static Locale createLocale(String langId) {
            String[] parts = langId.split("-");
            if (parts.length == 1) {
                return new Locale(parts[0]);
            }
            return new Locale(parts[0], parts[1]);
        }
    }
}
