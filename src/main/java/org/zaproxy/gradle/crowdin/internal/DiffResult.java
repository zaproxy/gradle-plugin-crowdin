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

import java.util.Objects;

/**
 * The result of comparing two nodes between two node hierarchies.
 *
 * @param <T1> the type of the data of the right node.
 * @param <T2> the type of the data of the left node.
 */
public class DiffResult<T1, T2> {

    /** The resulting state of the diff. */
    public enum State {
        /**
         * If the right node was added, that is, absent in the left node hierarchy.
         *
         * <p>In this state the left node is {@code null}.
         */
        ADDED,
        /**
         * If both node hierarchies have the node.
         *
         * <p>In this state both nodes are non-{@code null}.
         */
        SAME,
        /**
         * If the left node was removed, that is, absent in the right node hierarchy.
         *
         * <p>In this state the right node is {@code null}.
         */
        REMOVED
    }

    private final VfsNode<T1> right;
    private final VfsNode<T2> left;
    private final State state;

    public DiffResult(VfsNode<T1> right, VfsNode<T2> left, State state) {
        this.right = right;
        this.left = left;
        this.state = Objects.requireNonNull(state);
    }

    /**
     * Gets the right node.
     *
     * @return the right node, might be {@code null}.
     */
    public VfsNode<T1> getRight() {
        return right;
    }

    /**
     * Gets the left node.
     *
     * @return the left node, might be {@code null}.
     */
    public VfsNode<T2> getLeft() {
        return left;
    }

    /**
     * Gets the state.
     *
     * @return the state, never {@code null}.
     */
    public State getState() {
        return state;
    }
}
