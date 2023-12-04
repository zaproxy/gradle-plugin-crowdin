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

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/** A class that replaces defined tokens in a string. */
public class TokenReplacer {

    private String[] names;
    private String[] values;

    /** Constructs a {@code TokenReplacer} with no tokens. */
    protected TokenReplacer() {
        this(Collections.emptyMap());
    }

    /**
     * Constructs a {@code TokenReplacer} with the given (name/value pair) tokens.
     *
     * @param tokens the tokens to replace.
     * @throws NullPointerException is tokens it {@code null}.
     */
    public TokenReplacer(Map<String, String> tokens) {
        updateTokens(tokens);
    }

    /**
     * Updates the replacer with the given (name/value pair) tokens.
     *
     * <p>Existing tokens are discarded.
     *
     * @param tokens the tokens to replace.
     * @throws NullPointerException is tokens it {@code null}.
     */
    protected final void updateTokens(Map<String, String> tokens) {
        Objects.requireNonNull(tokens, "The tokens must not be null.");

        names = new String[tokens.size()];
        values = new String[tokens.size()];

        int i = 0;
        for (Iterator<Map.Entry<String, String>> it = tokens.entrySet().iterator();
                it.hasNext();
                i++) {
            Map.Entry<String, String> entry = it.next();
            names[i] = entry.getKey();
            values[i] =
                    Objects.requireNonNull(entry.getValue(), "The token value must not be null.");
        }
    }

    /**
     * Replaces the tokens present in the given value.
     *
     * @param value the value where to replace the tokens, might be {@code null}.
     * @return the value with the tokens replaced.
     */
    public String replace(String value) {
        if (value == null) {
            return value;
        }
        return StringUtils.replaceEach(value, names, values);
    }
}
