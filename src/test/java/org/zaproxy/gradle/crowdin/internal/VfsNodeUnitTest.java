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
import static org.zaproxy.gradle.crowdin.internal.DiffResultAssert.assertThat;
import static org.zaproxy.gradle.crowdin.internal.VfsNodeAssert.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Unit test for {@link VfsNode} */
class VfsNodeUnitTest {

    @Test
    void shouldHaveEmptyNameIfRoot() {
        // Given / When
        VfsNode<String> root = new VfsNode<>();
        // Then
        assertThat(root).hasName("");
    }

    @Test
    void shouldHaveRootPathIfRoot() {
        // Given / When
        VfsNode<String> root = new VfsNode<>();
        // Then
        assertThat(root).hasPath("/");
    }

    @Test
    void shouldBeRootIfRoot() {
        // Given / When
        VfsNode<String> root = new VfsNode<>();
        // Then
        assertThat(root).isRoot();
    }

    @Test
    void shouldHaveNoParentIfRoot() {
        // Given / When
        VfsNode<String> root = new VfsNode<>();
        // Then
        assertThat(root).hasParent(null);
    }

    @Test
    void shouldHaveNoDataIfRoot() {
        // Given / When
        VfsNode<String> root = new VfsNode<>();
        // Then
        assertThat(root).hasNoData();
    }

    @Test
    void shouldHaveNoNodesByDefault() {
        // Given / When
        VfsNode<String> node = new VfsNode<>();
        // Then
        assertThat(node).hasNoNodes();
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "/a", "/1/a", "/1/2/a"})
    void shouldAddNode(String path) {
        // Given
        VfsNode<String> node = new VfsNode<>();
        // When
        VfsNode<String> nodeAdded = node.add(path);
        // Then
        assertThat(node).hasNodes();
        assertThat(nodeAdded).hasName("a").hasNoNodes().hasNoData();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldNotAddNodeWithNullOrEmptyPath(String path) {
        // Given
        VfsNode<String> node = new VfsNode<>();
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> node.add(path));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldNotAddNodeWithDataAndNullOrEmptyPath(String path) {
        // Given
        VfsNode<String> node = new VfsNode<>();
        String data = "A";
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> node.add(path, data));
    }

    @Test
    void shouldNotAddNodeWithEmptyName() {
        // Given
        VfsNode<String> node = new VfsNode<>();
        String path = "/a/";
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> node.add(path));
    }

    @Test
    void shouldNotAddNodeWithEmptyNameWithData() {
        // Given
        VfsNode<String> node = new VfsNode<>();
        String path = "/a/";
        String data = "A";
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> node.add(path, data));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "/a", "/1/a", "/1/2/a"})
    void shouldNotAddNodeIfAlreadyAdded(String path) {
        // Given
        VfsNode<String> node = new VfsNode<>();
        node.add(path);
        // When
        VfsNode<String> nodeAdded = node.add(path);
        // Then
        assertThat(node).hasNodes();
        assertThat(nodeAdded).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "/a", "/1/a", "/1/2/a"})
    void shouldAddNodeWithData(String path) {
        // Given
        VfsNode<String> node = new VfsNode<>();
        String data = "A";
        // When
        VfsNode<String> nodeAdded = node.add(path, data);
        // Then
        assertThat(node).hasNodes();
        assertThat(nodeAdded).hasName("a").hasNoNodes().hasData(data);
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "/a", "/1/a", "/1/2/a"})
    void shouldNotAddNodeWithDataIfAlreadyAdded(String path) {
        // Given
        VfsNode<String> node = new VfsNode<>();
        String data = "A";
        node.add(path, data);
        // When
        VfsNode<String> nodeAdded = node.add(path, data);
        // Then
        assertThat(node).hasNodes();
        assertThat(nodeAdded).isNull();
    }

    @Test
    void shouldAddSubNodesIfNotPresent() {
        // Given
        VfsNode<String> node = new VfsNode<>();
        // When
        VfsNode<String> nodeAdded = node.add("/1/2/a");
        // Then
        assertThat(node).hasNodes();
        assertThat(nodeAdded).isNotNull();
        assertThat(node.get("/1")).hasName("1").hasNodes().hasNoData();
        assertThat(node.get("/1/2")).hasName("2").hasNodes().hasNoData();
    }

    @Test
    void shouldAddSubNodesIfNotPresentWhenAddingWithData() {
        // Given
        VfsNode<String> node = new VfsNode<>();
        // When
        VfsNode<String> nodeAdded = node.add("/1/2/a", "Data");
        // Then
        assertThat(node).hasNodes();
        assertThat(nodeAdded).isNotNull();
        assertThat(node.get("/1")).hasName("1").hasNodes().hasNoData();
        assertThat(node.get("/1/2")).hasName("2").hasNodes().hasNoData();
    }

    @Test
    void shouldSetDataToCreatedSubNode() {
        // Given
        VfsNode<String> node = new VfsNode<>();
        node.add("/1/2/a", "Data");
        // When
        VfsNode<String> nodeAdded = node.add("/1/2", "Data 2");
        // Then
        assertThat(node).hasNodes();
        assertThat(nodeAdded).isNotNull();
        assertThat(node.get("/1")).hasNoData();
        assertThat(node.get("/1/2")).hasData("Data 2");
    }

    @Test
    void shouldNotSetDataToCreatedSubNodeIfAlreadySet() {
        // Given
        VfsNode<String> node = new VfsNode<>();
        node.add("/1/2/a", "Data");
        node.add("/1/2", "Data 2");
        // When
        VfsNode<String> nodeAdded = node.add("/1/2", "Data 3");
        // Then
        assertThat(node).hasNodes();
        assertThat(nodeAdded).isNull();
        assertThat(node.get("/1")).hasNoData();
        assertThat(node.get("/1/2")).hasData("Data 2");
    }

    @Test
    void shouldNotBeRootIfSubNodes() {
        // Given
        VfsNode<String> node = new VfsNode<>();
        // When
        VfsNode<String> nodeAdded = node.add("/1/2/a");
        // Then
        assertThat(node).hasNodes();
        assertThat(nodeAdded).isNotRoot();
        assertThat(node.get("/1")).isNotRoot();
        assertThat(node.get("/1/2")).isNotRoot();
    }

    @Test
    void shouldAddWithExpectedParentsAndPaths() {
        // Given / When
        VfsNode<String> node = new VfsNode<>();
        VfsNode<String> node1 = node.add("/1");
        VfsNode<String> node2 = node.add("/1/2");
        VfsNode<String> node3 = node2.add("3");
        VfsNode<String> node4 = node.add("/1/2/4");
        VfsNode<String> nodeX = node4.add("/x");
        VfsNode<String> nodeY = nodeX.add("y");
        VfsNode<String> nodeZ = nodeX.add("/y/z");
        // Then
        assertThat(node1).hasParent(node);
        assertThat(node2).hasParent(node1);
        assertThat(node3).hasParent(node2);
        assertThat(node4).hasParent(node2);
        assertThat(nodeX).hasParent(node4).hasPath("/1/2/4/x");
        assertThat(nodeY).hasParent(nodeX).hasPath("/1/2/4/x/y");
        assertThat(nodeZ).hasParent(nodeY).hasPath("/1/2/4/x/y/z");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    void shouldGetSelf(String path) {
        // Given
        VfsNode<String> node = new VfsNode<>();
        // When
        VfsNode<String> gotNode = node.get(path);
        // Then
        assertThat(gotNode).isSameAs(node);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/a", "a"})
    void shouldGetDirectNode(String path) {
        // Given
        VfsNode<String> node = new VfsNode<>();
        String value = "A";
        node.add(path, value);
        // When
        VfsNode<String> gotNode = node.get(path);
        // Then
        assertThat(gotNode).hasData(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"a/b", "/a/b"})
    void shouldGetDescendentNode(String path) {
        // Given
        VfsNode<String> node = new VfsNode<>();
        String value = "B";
        node.add(path, value);
        // When
        VfsNode<String> gotNode = node.get(path);
        // Then
        assertThat(gotNode).hasData(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"b", "/b"})
    void shouldGetDirectNodeOfSubNode(String path) {
        // Given
        VfsNode<String> root = new VfsNode<>();
        String value = "B";
        VfsNode<String> nodeAdded = root.add("/a/b", value);
        VfsNode<String> subNode = root.get("/a");
        // When
        VfsNode<String> gotNode = subNode.get(path);
        // Then
        assertThat(gotNode).isSameAs(nodeAdded);
    }

    @Test
    void shouldCreateDiff_1() {
        // Given
        VfsNode<String> right = new VfsNode<>();
        VfsNode<String> left = new VfsNode<>();
        // When
        VfsNode<DiffResult<String, String>> result = right.diff(left);
        // Then
        assertThat(result.hasNodes()).isFalse();
    }

    @Nested
    class Diff {

        @Test
        void shouldResultInSameNode() {
            // Given
            VfsNode<String> right = new VfsNode<>();
            right.add("/a", "Right A");
            VfsNode<String> left = new VfsNode<>();
            left.add("/a", "Left A");
            // When
            VfsNode<DiffResult<String, String>> result = right.diff(left);
            // Then
            assertThat(result).hasNodes();
            assertThat(result.get("/a").getData())
                    .hasRightData("Right A")
                    .hasLeftData("Left A")
                    .hasState(DiffResult.State.SAME);
        }

        @Test
        void shouldResultInSameNodes() {
            // Given
            VfsNode<String> right = new VfsNode<>();
            right.add("/a/b", "B");
            VfsNode<String> left = new VfsNode<>();
            left.add("/a/b", "B");
            // When
            VfsNode<DiffResult<String, String>> result = right.diff(left);
            // Then
            assertThat(result).hasNodes();
            assertThat(result.get("/a").getData())
                    .hasRightData(null)
                    .hasLeftData(null)
                    .hasState(DiffResult.State.SAME);
            assertThat(result.get("/a/b").getData())
                    .hasRightData("B")
                    .hasLeftData("B")
                    .hasState(DiffResult.State.SAME);
        }

        @Test
        void shouldResultInSameNodesWithValues() {
            // Given
            VfsNode<String> right = new VfsNode<>();
            right.add("/a", "A");
            right.add("/a/b", "B");
            VfsNode<String> left = new VfsNode<>();
            left.add("/a", "A");
            left.add("/a/b", "B");
            // When
            VfsNode<DiffResult<String, String>> result = right.diff(left);
            // Then
            assertThat(result).hasNodes();
            assertThat(result.get("/a").getData())
                    .hasRightData("A")
                    .hasLeftData("A")
                    .hasState(DiffResult.State.SAME);
            assertThat(result.get("/a/b").getData())
                    .hasRightData("B")
                    .hasLeftData("B")
                    .hasState(DiffResult.State.SAME);
        }

        @Test
        void shouldResultInRemovedNode() {
            // Given
            VfsNode<String> right = new VfsNode<>();
            VfsNode<String> left = new VfsNode<>();
            left.add("/a", "A");
            // When
            VfsNode<DiffResult<String, String>> result = right.diff(left);
            // Then
            assertThat(result).hasNodes();
            assertThat(result.get("/a").getData())
                    .hasRight(null)
                    .hasLeftData("A")
                    .hasState(DiffResult.State.REMOVED);
        }

        @Test
        void shouldResultInRemovedNodes() {
            // Given
            VfsNode<String> right = new VfsNode<>();
            VfsNode<String> left = new VfsNode<>();
            left.add("/a/b", "B");
            // When
            VfsNode<DiffResult<String, String>> result = right.diff(left);
            // Then
            assertThat(result).hasNodes();
            assertThat(result.get("/a").getData())
                    .hasRight(null)
                    .hasLeftData(null)
                    .hasState(DiffResult.State.REMOVED);
            assertThat(result.get("/a/b").getData())
                    .hasRight(null)
                    .hasLeftData("B")
                    .hasState(DiffResult.State.REMOVED);
        }

        @Test
        void shouldResultInAddedNode() {
            // Given
            VfsNode<String> right = new VfsNode<>();
            right.add("/a", "A");
            VfsNode<String> left = new VfsNode<>();
            // When
            VfsNode<DiffResult<String, String>> result = right.diff(left);
            // Then
            assertThat(result).hasNodes();
            assertThat(result.get("/a").getData())
                    .hasRightData("A")
                    .hasLeft(null)
                    .hasState(DiffResult.State.ADDED);
        }

        @Test
        void shouldResultInAddedNodes() {
            // Given
            VfsNode<String> right = new VfsNode<>();
            right.add("/a/b", "B");
            VfsNode<String> left = new VfsNode<>();
            // When
            VfsNode<DiffResult<String, String>> result = right.diff(left);
            // Then
            assertThat(result).hasNodes();
            assertThat(result.get("/a").getData())
                    .hasRightData(null)
                    .hasLeft(null)
                    .hasState(DiffResult.State.ADDED);
            assertThat(result.get("/a/b").getData())
                    .hasRightData("B")
                    .hasLeft(null)
                    .hasState(DiffResult.State.ADDED);
        }

        @Test
        void shouldResultInSameAddedAndRemoved() {
            // Given
            VfsNode<String> right = new VfsNode<>();
            right.add("/a", "A");
            right.add("/b", "B");
            VfsNode<String> left = new VfsNode<>();
            left.add("/a", "A");
            left.add("/c", "C");
            // When
            VfsNode<DiffResult<String, String>> result = right.diff(left);
            // Then
            assertThat(result).hasNodes();
            assertThat(result.get("/a").getData())
                    .hasRightData("A")
                    .hasLeftData("A")
                    .hasState(DiffResult.State.SAME);
            assertThat(result.get("/b").getData())
                    .hasRightData("B")
                    .hasLeft(null)
                    .hasState(DiffResult.State.ADDED);
            assertThat(result.get("/c").getData())
                    .hasRight(null)
                    .hasLeftData("C")
                    .hasState(DiffResult.State.REMOVED);
        }

        @Test
        void shouldHaveOppositeResultIfNodesSwapped() {
            // Given
            VfsNode<String> right = new VfsNode<>();
            right.add("/a", "A");
            right.add("/b", "B");
            VfsNode<String> left = new VfsNode<>();
            left.add("/a", "A");
            left.add("/c", "C");
            // When
            VfsNode<DiffResult<String, String>> result = left.diff(right);
            // Then
            assertThat(result).hasNodes();
            assertThat(result.get("/a").getData())
                    .hasRightData("A")
                    .hasLeftData("A")
                    .hasState(DiffResult.State.SAME);
            assertThat(result.get("/b").getData())
                    .hasRight(null)
                    .hasLeftData("B")
                    .hasState(DiffResult.State.REMOVED);
            assertThat(result.get("/c").getData())
                    .hasRightData("C")
                    .hasLeft(null)
                    .hasState(DiffResult.State.ADDED);
        }
    }

    @Nested
    class DiffWithNeedle {

        @Test
        void shouldResultInExceptionIfNeedleNotFound() {
            // Given
            VfsNode<String> right = new VfsNode<>();
            right.add("/a", "A");
            VfsNode<String> left = new VfsNode<>();
            left.add("/a", "A");
            // When / Then
            assertThrows(IllegalArgumentException.class, () -> right.diff(left, "/w"));
        }

        @Test
        void shouldResultInAddedNodesAboveNeedle() {
            // Given
            VfsNode<String> right = new VfsNode<>();
            right.add("/x/a/b", "B");
            VfsNode<String> left = new VfsNode<>();
            right.add("/y", "Y");
            // When
            VfsNode<DiffResult<String, String>> result = right.diff(left, "/x");
            // Then
            assertThat(result).hasNodes();
            assertThat(result.get("/y")).isNull();
            assertThat(result.get("/x").getData())
                    .hasRightData(null)
                    .hasLeft(null)
                    .hasState(DiffResult.State.ADDED);
            assertThat(result.get("/x/a").getData())
                    .hasRightData(null)
                    .hasLeft(null)
                    .hasState(DiffResult.State.ADDED);
            assertThat(result.get("/x/a/b").getData())
                    .hasRightData("B")
                    .hasLeft(null)
                    .hasState(DiffResult.State.ADDED);
        }

        @Test
        void shouldResultInSameAddedAndRemoved() {
            // Given
            VfsNode<String> right = new VfsNode<>();
            right.add("/x/a", "A");
            right.add("/x/b", "B");
            right.add("/x/b/c", "B/C");
            right.add("/y/a", "A");
            VfsNode<String> left = new VfsNode<>();
            left.add("/x/a", "A");
            left.add("/x/c", "C");
            left.add("/z/a", "A");
            // When
            VfsNode<DiffResult<String, String>> result = right.diff(left, "/x");
            // Then
            assertThat(result).hasNodes();
            assertThat(result.get("/y")).isNull();
            assertThat(result.get("/y/a")).isNull();
            assertThat(result.get("/z")).isNull();
            assertThat(result.get("/z/a")).isNull();
            assertThat(result.get("/x").getData())
                    .hasRightData(null)
                    .hasLeftData(null)
                    .hasState(DiffResult.State.SAME);
            assertThat(result.get("/x/a").getData())
                    .hasRightData("A")
                    .hasLeftData("A")
                    .hasState(DiffResult.State.SAME);
            assertThat(result.get("/x/b").getData())
                    .hasRightData("B")
                    .hasLeft(null)
                    .hasState(DiffResult.State.ADDED);
            assertThat(result.get("/x/b/c").getData())
                    .hasRightData("B/C")
                    .hasLeft(null)
                    .hasState(DiffResult.State.ADDED);
            assertThat(result.get("/x/c").getData())
                    .hasRight(null)
                    .hasLeftData("C")
                    .hasState(DiffResult.State.REMOVED);
        }

        @Test
        void shouldHaveOppositeResultIfNodesSwapped() {
            // Given
            VfsNode<String> right = new VfsNode<>();
            right.add("/x/a", "A");
            right.add("/x/b", "B");
            right.add("/x/b/c", "B/C");
            right.add("/y/a", "A");
            VfsNode<String> left = new VfsNode<>();
            left.add("/x/a", "A");
            left.add("/x/c", "C");
            left.add("/z/a", "A");
            // When
            VfsNode<DiffResult<String, String>> result = left.diff(right, "/x");
            // Then
            assertThat(result).hasNodes();
            assertThat(result.get("/y")).isNull();
            assertThat(result.get("/y/a")).isNull();
            assertThat(result.get("/z")).isNull();
            assertThat(result.get("/z/a")).isNull();
            assertThat(result.get("/x").getData())
                    .hasRightData(null)
                    .hasLeftData(null)
                    .hasState(DiffResult.State.SAME);
            assertThat(result.get("/x/a").getData())
                    .hasRightData("A")
                    .hasLeftData("A")
                    .hasState(DiffResult.State.SAME);
            assertThat(result.get("/x/b").getData())
                    .hasRight(null)
                    .hasLeftData("B")
                    .hasState(DiffResult.State.REMOVED);
            assertThat(result.get("/x/b/c").getData())
                    .hasRight(null)
                    .hasLeftData("B/C")
                    .hasState(DiffResult.State.REMOVED);
            assertThat(result.get("/x/c").getData())
                    .hasRightData("C")
                    .hasLeft(null)
                    .hasState(DiffResult.State.ADDED);
        }
    }

    @Nested
    class HashCode {
        @Test
        void shouldHaveSameHashCodeIfRoots() {
            // Given
            VfsNode<String> node = new VfsNode<>();
            VfsNode<String> other = new VfsNode<>();
            // When / Then
            assertThat(node).hasSameHashCodeAs(other);
        }

        @Test
        void shouldHaveSameHashCodeIfSameNameAndParent() {
            // Given
            VfsNode<String> node = new VfsNode<>();
            node.add("/a");
            VfsNode<String> other = new VfsNode<>();
            other.add("/a");
            // When / Then
            assertThat(node.get("/a")).hasSameHashCodeAs(node.get("/a"));
        }

        @Test
        void shouldNotHaveSameHashCodeIfDifferentName() {
            // Given
            VfsNode<String> node = new VfsNode<>();
            node.add("/a");
            node.add("/b");
            // When / Then
            assertThat(node.get("/a")).doesNotHaveSameHashCodeAs(node.get("/b"));
        }

        @Test
        void shouldNotHaveSameHashCodeIfDifferentParent() {
            // Given
            VfsNode<String> node = new VfsNode<>();
            node.add("/a/1");
            node.add("/b/1");
            // When / Then
            assertThat(node.get("/a/1")).doesNotHaveSameHashCodeAs(node.get("/b/1"));
        }
    }

    @Nested
    class Equality {

        @Test
        void shouldBeEqualToItself() {
            // Given
            VfsNode<String> node = new VfsNode<>();
            // When / Then
            assertThat(node.equals(node)).isTrue();
        }

        @Test
        void shouldBeEqualEvenIfSubclass() {
            // Given
            VfsNode<String> node = new VfsNode<>();
            VfsNode<String> nodeSubclass = new VfsNode<String>() {};
            // When / Then
            assertThat(node).isEqualTo(nodeSubclass);
        }

        @Test
        void shouldBeEqualEvenIfDifferentDataType() {
            // Given
            VfsNode<String> node = new VfsNode<>();
            VfsNode<Integer> nodeWithDifferentDataType = new VfsNode<>();
            // When / Then
            assertThat(node).isEqualTo(nodeWithDifferentDataType);
        }

        @Test
        void shouldBeEqualEvenWithDifferentData() {
            // Given
            VfsNode<String> node = new VfsNode<>();
            node.add("/a", "A");
            VfsNode<String> other = new VfsNode<>();
            other.add("/a", "B");
            // When / Then
            assertThat(node.get("/a")).isEqualTo(other.get("/a"));
        }

        @Test
        void shouldBeEqualEvenWithDifferentSubnodes() {
            // Given
            VfsNode<String> node = new VfsNode<>();
            node.add("/a", "A");
            VfsNode<String> other = new VfsNode<>();
            other.add("/b", "B");
            // When / Then
            assertThat(node).isEqualTo(other);
        }

        @Test
        void shouldNotBeEqualToNull() {
            // Given
            VfsNode<String> node = new VfsNode<>();
            // When / Then
            assertThat(node.equals(null)).isFalse();
        }

        @Test
        void shouldNotBeEqualToDifferentType() {
            // Given
            VfsNode<String> node = new VfsNode<>();
            String other = "A";
            // When / Then
            assertThat(node).isNotEqualTo(other);
        }

        @Test
        void shouldNotBeEqualWithDifferentParent() {
            // Given
            VfsNode<String> node = new VfsNode<>();
            node.add("/a");
            node.add("/a/a");
            // When / Then
            assertThat(node.get("/a")).isNotEqualTo(node.get("/a/a"));
        }
    }

    @Nested
    class Comparison {

        @Test
        void shouldBeGreaterThanNull() {
            // Given
            VfsNode<String> node = new VfsNode<>();
            VfsNode<String> other = null;
            // When / Then
            assertThat(node).isGreaterThan(other);
        }

        @Test
        void shouldBeEqualToItself() {
            // Given
            VfsNode<String> node = new VfsNode<>();
            // When / Then
            assertThat(node).isEqualByComparingTo(node);
        }

        @Test
        void shouldBeEqualIfRoots() {
            // Given
            VfsNode<String> node = new VfsNode<>();
            VfsNode<String> other = new VfsNode<>();
            // When / Then
            assertThat(node).isEqualByComparingTo(other);
        }

        @Test
        void shouldBeEqualIfSameParentAndName() {
            // Given
            VfsNode<String> node = new VfsNode<>();
            node.add("/a");
            VfsNode<String> other = new VfsNode<>();
            other.add("/a");
            // When / Then
            assertThat(node.get("/")).isEqualByComparingTo(other);
        }

        @Test
        void shouldBeLessThanIfNoParent() {
            // Given
            VfsNode<String> node = new VfsNode<>();
            node.add("/a");
            // When / Then
            assertThat(node).isLessThan(node.get("/a"));
        }

        @Test
        void shouldBeGreaterThanIfParent() {
            // Given
            VfsNode<String> node = new VfsNode<>();
            node.add("/a");
            // When / Then
            assertThat(node.get("/a")).isGreaterThan(node);
        }

        @Test
        void shouldBeLessThanIfLessParent() {
            // Given
            VfsNode<String> node = new VfsNode<>();
            node.add("/a/1");
            node.add("/b/1");
            // When / Then
            assertThat(node.get("/a/1")).isLessThan(node.get("/b/1"));
        }

        @Test
        void shouldBeGreaterThanIfGreaterParent() {
            // Given
            VfsNode<String> node = new VfsNode<>();
            node.add("/a/1");
            node.add("/b/1");
            // When / Then
            assertThat(node.get("/b/1")).isGreaterThan(node.get("/a/1"));
        }

        @Test
        void shouldBeLessThanIfLessName() {
            // Given
            VfsNode<String> node = new VfsNode<>();
            node.add("/a");
            node.add("/b");
            // When / Then
            assertThat(node.get("/a")).isLessThan(node.get("/b"));
        }

        @Test
        void shouldBeGreaterThanIfGreaterName() {
            // Given
            VfsNode<String> node = new VfsNode<>();
            node.add("/a");
            node.add("/b");
            // When / Then
            assertThat(node.get("/b")).isGreaterThan(node.get("/a"));
        }
    }
}
