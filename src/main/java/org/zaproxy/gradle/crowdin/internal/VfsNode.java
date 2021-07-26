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

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A node of a tree hierarchy.
 *
 * @param <T> the type of data contained in the node.
 */
public class VfsNode<T> implements Comparable<VfsNode<?>> {

    public static final char PATH_SEPARATOR_CHAR = '/';
    public static final String SEPARATOR = String.valueOf(PATH_SEPARATOR_CHAR);

    private final String name;
    private final VfsNode<T> parent;
    private final String path;
    private SortedMap<String, VfsNode<T>> nodes;
    private T data;

    /** Constructs the root node. */
    public VfsNode() {
        this.name = "";
        this.path = "/";
        this.parent = null;
        this.nodes = new TreeMap<>();
        this.data = null;
    }

    private VfsNode(String name, VfsNode<T> parent) {
        this(name, parent, null);
    }

    private VfsNode(String name, VfsNode<T> parent, T data) {
        this(name, parent, data, new TreeMap<>());
    }

    private VfsNode(
            String name, VfsNode<T> parent, T data, SortedMap<String, VfsNode<T>> children) {
        this.name = validateNotEmpty("name", name);
        this.parent = Objects.requireNonNull(parent, "Non-root node must have a parent.");
        this.path = parent.isRoot() ? parent.path + name : parent.path + PATH_SEPARATOR_CHAR + name;
        this.nodes = children;
        this.data = data;
    }

    private static String validateNotEmpty(String property, String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("The " + property + " must not be null nor empty.");
        }
        return value;
    }

    /**
     * Gets the name.
     *
     * @return the name, never {@code null}.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the (full) path.
     *
     * @return the path, never {@code null}.
     */
    public String getPath() {
        return path;
    }

    /**
     * Tells whether or not this node is the root node.
     *
     * <p>The root node has no parent.
     *
     * @return {@code true} if root, {@code false} otherwise,
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * Gets the parent.
     *
     * @return the parent, or {@code null} if root node.
     */
    public VfsNode<T> getParent() {
        return parent;
    }

    /**
     * Gets the data of the node.
     *
     * @return the data, or {@code null} if none.
     */
    public T getData() {
        return data;
    }

    /**
     * Tells whether or not this node has data.
     *
     * @return {@code true} if it has data, {@code false} otherwise,
     */
    public boolean hasData() {
        return data != null;
    }

    /**
     * Tells whether or not this node has nodes.
     *
     * @return {@code true} if it has nodes, {@code false} otherwise,
     */
    public boolean hasNodes() {
        return !nodes.isEmpty();
    }

    public VfsNode<T> get(String path) {
        String[] pathSegments = getSegments(path);
        return get(pathSegments, 0, pathSegments.length - 1);
    }

    private VfsNode<T> get(String[] pathSegments, int start, int end) {
        if (pathSegments.length == 0 || (pathSegments.length == 1 && pathSegments[0].isEmpty())) {
            return this;
        }

        VfsNode<T> node = nodes.get(pathSegments[start]);
        if (start < end && node != null) {
            return node.get(pathSegments, start + 1, end);
        }
        return node;
    }

    public VfsNode<T> add(String path) {
        validateNotEmpty("path", path);

        String[] pathSegments = getSegments(path);
        if (get(pathSegments, 0, pathSegments.length - 1) != null) {
            return null;
        }

        return add(pathSegments, 0, pathSegments.length - 1);
    }

    private VfsNode<T> add(String[] pathSegments, int start, int end) {
        VfsNode<T> node =
                nodes.computeIfAbsent(
                        pathSegments[start], nodeName -> new VfsNode<>(nodeName, this));
        if (start < end) {
            return node.add(pathSegments, start + 1, end);
        }
        return node;
    }

    public VfsNode<T> add(String path, T data) {
        validateNotEmpty("path", path);
        Objects.requireNonNull(data, "The data must not be null.");

        String[] pathSegments = getSegments(path);
        VfsNode<T> parent = this;
        if (pathSegments.length > 1) {
            parent = add(pathSegments, 0, pathSegments.length - 2);
        }

        String name = pathSegments[pathSegments.length - 1];
        VfsNode<T> child = parent.nodes.get(name);
        if (child != null) {
            if (!child.hasData()) {
                child.data = data;
                return child;
            }
            return null;
        }
        VfsNode<T> node = new VfsNode<>(name, parent, data);
        parent.nodes.put(name, node);
        return node;
    }

    public <T2> VfsNode<DiffResult<T, T2>> diff(VfsNode<T2> left) {
        Objects.requireNonNull(left);

        VfsNode<DiffResult<T, T2>> result = new VfsNode<>();
        diffInto(left, result);
        return result;
    }

    public <T2> VfsNode<DiffResult<T, T2>> diff(VfsNode<T2> left, String needle) {
        Objects.requireNonNull(left);

        VfsNode<DiffResult<T, T2>> result = new VfsNode<>();

        VfsNode<T> start = get(needle);
        if (start == null) {
            throw new IllegalArgumentException("Provided needle not found: " + needle);
        }

        start.walk(
                (path, node) -> {
                    VfsNode<T2> otherNode = left.get(path);
                    if (otherNode == null) {
                        result.add(path, new DiffResult<>(node, null, DiffResult.State.ADDED));
                        return;
                    }
                    result.add(path, new DiffResult<>(node, otherNode, DiffResult.State.SAME));
                });

        start.diffInto(left.get(needle), result);
        return result;
    }

    public void walk(BiConsumer<String, VfsNode<T>> consumer) {
        Objects.requireNonNull(consumer);

        if (parent != null) {
            parent.walk(consumer);
            consumer.accept(getPath(), this);
        }
    }

    private <T2> void diffInto(VfsNode<T2> left, VfsNode<DiffResult<T, T2>> result) {
        if (left == null) {
            traverse(
                    (path, node) -> {
                        result.add(path, new DiffResult<>(node, null, DiffResult.State.ADDED));
                    });
            return;
        }

        traverse(
                left,
                (a, b) -> {
                    String path = a.getPath();
                    if (b == null) {
                        result.add(path, new DiffResult<>(a, null, DiffResult.State.ADDED));
                        return;
                    }
                    result.add(path, new DiffResult<>(a, b, DiffResult.State.SAME));
                });

        left.traverse(
                this,
                (a, b) -> {
                    if (b == null) {
                        result.add(
                                a.getPath(), new DiffResult<>(null, a, DiffResult.State.REMOVED));
                    }
                });
    }

    public void traverse(BiConsumer<String, VfsNode<T>> consumer) {
        Objects.requireNonNull(consumer);

        nodes.forEach(
                (k, e) -> {
                    consumer.accept(e.getPath(), e);
                    e.traverse(consumer);
                });
    }

    private <T2> void traverse(VfsNode<T2> left, BiConsumer<VfsNode<T>, VfsNode<T2>> consumer) {
        nodes.forEach(
                (k, e) -> {
                    VfsNode<T2> b = left != null ? left.get(k) : null;
                    consumer.accept(e, b);
                    e.traverse(b, consumer);
                });
    }

    public void print(PrintStream out) {
        Objects.requireNonNull(out);

        print(out, node -> null);
    }

    public void print(PrintStream out, Function<VfsNode<T>, String> action) {
        Objects.requireNonNull(out);
        Objects.requireNonNull(action);

        String prefix = "";
        if (parent != null) {
            prefix = new ParentsPrinter<>(this).print(out, action);
        }
        printTree(out, this, prefix, action);
    }

    private static class ParentsPrinter<T> {

        private final VfsNode<T> node;
        private final StringBuilder prefix;

        ParentsPrinter(VfsNode<T> node) {
            this.node = node;
            this.prefix = new StringBuilder();
        }

        String print(PrintStream out, Function<VfsNode<T>, String> action) {
            node.walk(
                    (path, node) -> {
                        out.print(prefix);
                        out.print("└─\u00A0");
                        out.print(node.name);
                        String result = action.apply(node);
                        if (result != null) {
                            out.print(result);
                        }
                        out.println();
                        prefix.append("\u00A0\u00A0\u00A0");
                    });
            return prefix.toString();
        }
    }

    private static <T> void printTree(
            PrintStream out, VfsNode<T> node, String prefix, Function<VfsNode<T>, String> action) {
        int i = 1;
        int size = node.nodes.size();
        for (Iterator<VfsNode<T>> it = node.nodes.values().iterator(); it.hasNext(); i++) {
            VfsNode<T> subNode = it.next();
            boolean last = i == size;
            out.print(prefix);
            out.print(last ? '└' : '├');
            out.print("─\u00A0");
            out.print(subNode.name);
            String result = action.apply(subNode);
            if (result != null) {
                out.print(result);
            }
            out.println();
            printTree(out, subNode, prefix + (last ? "\u00A0" : '│') + "\u00A0\u00A0", action);
        }
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof VfsNode)) {
            return false;
        }
        VfsNode<?> other = (VfsNode<?>) obj;
        if (!Objects.equals(parent, other.parent)) {
            return false;
        }
        return Objects.equals(name, other.name);
    }

    @Override
    public int compareTo(VfsNode<?> other) {
        if (other == null) {
            return 1;
        }
        if (this == other) {
            return 0;
        }
        if (parent == null) {
            if (other.parent == null) {
                return 0;
            }
            return -1;
        } else if (other.parent == null) {
            return 1;
        }
        int result = parent.compareTo(other.parent);
        if (result != 0) {
            return result;
        }
        return name.compareTo(other.name);
    }

    private static String[] getSegments(String path) {
        String[] segments = path.split(SEPARATOR, -1);

        if (segments[0].length() == 0) {
            segments = Arrays.copyOfRange(segments, 1, segments.length);
        }

        return segments;
    }
}
