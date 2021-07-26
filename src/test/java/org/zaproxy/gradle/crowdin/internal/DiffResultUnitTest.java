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
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.zaproxy.gradle.crowdin.internal.DiffResultAssert.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.zaproxy.gradle.crowdin.internal.DiffResult.State;

/** Unit test for {@link DiffResult}. */
class DiffResultUnitTest {

    @ParameterizedTest
    @MethodSource("constructorArgs")
    void shouldConstructWithValidArgs(VfsNode<String> right, VfsNode<Integer> left) {
        // Given
        State state = State.ADDED;
        // When
        DiffResult<String, Integer> result = new DiffResult<>(right, left, state);
        // Then
        assertThat(result).hasRight(right).hasLeft(left).hasState(state);
    }

    static Stream<Arguments> constructorArgs() {
        return Stream.of(
                arguments(new VfsNode<String>(), new VfsNode<Integer>()),
                arguments(null, new VfsNode<Integer>()),
                arguments(null, null));
    }

    @Test
    void shouldNotConstructWithNullState() {
        // Given
        VfsNode<String> right = new VfsNode<>();
        VfsNode<Integer> left = new VfsNode<>();
        State state = null;
        // When / Then
        assertThrows(NullPointerException.class, () -> new DiffResult<>(right, left, state));
    }
}
