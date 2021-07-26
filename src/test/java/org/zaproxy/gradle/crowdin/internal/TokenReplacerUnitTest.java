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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit test for {@link TokenReplacer}. */
class TokenReplacerUnitTest {

    @Test
    void shouldThrowExceptionIfTokensIsNull() {
        // Given
        Map<String, String> tokens = null;
        // When / Then
        assertThrows(NullPointerException.class, () -> new TokenReplacer(tokens));
    }

    @Test
    void shouldThrowExceptionIfTokenValueIsNull() {
        // Given
        Map<String, String> tokens = new HashMap<>();
        tokens.put("$token1", "A");
        tokens.put("$token2", null);
        // When / Then
        assertThrows(NullPointerException.class, () -> new TokenReplacer(tokens));
    }

    @Test
    void shouldThrowExceptionIfTokensIsNullWhenUpdatingTokens() {
        // Given
        Map<String, String> tokens = null;
        TokenReplacer replacer = new TokenReplacer();
        // When / Then
        assertThrows(NullPointerException.class, () -> replacer.updateTokens(tokens));
    }

    @Test
    void shouldThrowExceptionIfTokenValueIsNullWhenUpdatingTokens() {
        // Given
        Map<String, String> tokens = new HashMap<>();
        tokens.put("$token1", "A");
        tokens.put("$token2", null);
        TokenReplacer replacer = new TokenReplacer();
        // When / Then
        assertThrows(NullPointerException.class, () -> replacer.updateTokens(tokens));
    }

    @Test
    void shouldNotReplaceIfNoTokens() {
        // Given
        Map<String, String> tokens = Collections.emptyMap();
        TokenReplacer replacer = new TokenReplacer(tokens);
        String value = "no tokens";
        // When
        String result = replacer.replace(value);
        // Then
        assertThat(result).isEqualTo(value);
    }

    @Test
    void shouldNotReplaceIfNoTokensSpecified() {
        // Given
        TokenReplacer replacer = new TokenReplacer();
        String value = "no tokens";
        // When
        String result = replacer.replace(value);
        // Then
        assertThat(result).isEqualTo(value);
    }

    @Test
    void shouldNotReplaceIfNullValue() {
        // Given
        Map<String, String> tokens = Collections.emptyMap();
        TokenReplacer replacer = new TokenReplacer(tokens);
        String value = null;
        // When
        String result = replacer.replace(value);
        // Then
        assertThat(result).isNull();
    }

    @Test
    void shouldReplaceTokens() {
        // Given
        Map<String, String> tokens = new HashMap<>();
        tokens.put("$token1", "A");
        tokens.put("$token2", "B");
        TokenReplacer replacer = new TokenReplacer(tokens);
        String value = "$token1 $token2";
        // When
        String result = replacer.replace(value);
        // Then
        assertThat(result).isEqualTo("A B");
    }

    @Test
    void shouldUpdateAndReplaceTokens() {
        // Given
        Map<String, String> oldTokens = new HashMap<>();
        oldTokens.put("$oldToken", "1");
        TokenReplacer replacer = new TokenReplacer(oldTokens);
        String value = "$oldToken $newToken1 $newToken2";
        Map<String, String> tokens = new HashMap<>();
        tokens.put("$newToken1", "A");
        tokens.put("$newToken2", "B");
        // When
        String result1 = replacer.replace(value);
        replacer.updateTokens(tokens);
        String result2 = replacer.replace(value);
        // Then
        assertThat(result1).isEqualTo("1 $newToken1 $newToken2");
        assertThat(result2).isEqualTo("$oldToken A B");
    }
}
