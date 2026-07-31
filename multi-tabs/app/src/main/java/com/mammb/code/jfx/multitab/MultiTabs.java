/*
 * Copyright 2026- the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mammb.code.jfx.multitab;

import com.mammb.code.jfx.multitab.internal.BranchNode;
import com.mammb.code.jfx.multitab.internal.Context;
import com.mammb.code.jfx.multitab.internal.LeafNode;
import com.mammb.code.jfx.multitab.internal.ParentOf;
import com.mammb.code.jfx.multitab.internal.Tab;
import com.mammb.code.jfx.multitab.internal.TreeNode;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public interface MultiTabs {

    static Builder builder() {
        return new Builder();
    }

    static String asString(Pane pane) {
        return null;
    }


    record Builder(
            Supplier<? extends ContentPane> toContent,
            Function<Path, ? extends ContentPane> pathToContent,
            BiFunction<Stage, Pane, Stage> initStage) {
        public Builder() {
            this(null, null, (nextStage, pane) -> {
                nextStage.setScene(new Scene(pane));
                return nextStage;
            });
        }
        public Builder toContent(Supplier<? extends ContentPane> toContent) {
            return new Builder(Objects.requireNonNull(toContent), pathToContent, initStage);
        }
        public Builder pathToContent(Function<Path, ? extends ContentPane> pathToContent) {
            return new Builder(toContent, Objects.requireNonNull(pathToContent), initStage);
        }
        public Builder initStage(BiFunction<Stage, Pane, Stage> initStage) {
            return new Builder(toContent, pathToContent, Objects.requireNonNull(initStage));
        }
        public void buildOn(Stage stage) {
            Objects.requireNonNull(stage);
            var ctx = context();
            ctx.initStage(stage, new BranchNode(ctx, ctx.create()));
        }
        public Pane build(Stage stage) {
            var ctx = context();
            ctx.addStage(stage);
            return new BranchNode(ctx, ctx.create());
        }
        private Context context() {
            Objects.requireNonNull(toContent);
            return new Context(
                toContent,
                (pathToContent != null) ? pathToContent : _ -> toContent.get(),
                (initStage != null) ? initStage : (nextStage, pane) -> {
                    nextStage.setScene(new Scene(pane));
                    return nextStage;
                });
        }
    }

    private static String asString(ParentOf<?> parentOf) {

        return switch (parentOf) {

            case BranchNode branchNode -> "{" + String.join(",",
                // orientation
                branchNode.orientation().toString().substring(0, 1),
                // dividerPositions
                Arrays.stream(branchNode.dividerPositions())
                    .mapToObj(String::valueOf).findFirst().orElse("0.5"),
                // children
                branchNode.children().stream()
                    .filter(ParentOf.class::isInstance)
                    .map(e -> (ParentOf<?>) e)
                    .map(MultiTabs::asString)
                    .collect(Collectors.joining(","))
            ) + "}";

            case LeafNode leafNode -> leafNode.children().stream()
                .map(Tab::content)
                .map(ContentPane::asString)
                .map(MultiTabs::escape)
                .collect(Collectors.joining(",", "[", "]"));

            default -> "";
        };
    }

    private static Pane fromString(Context ctx, String str, Function<String, ? extends ContentPane> stringToContent) {

        if (str.startsWith("{") && str.endsWith("}")) {
            str = str.substring(1, str.length() - 1); // remove '{' '}'
            // orientation
            Orientation orientation = Objects.equals(str.charAt(0), 'H')
                ? Orientation.HORIZONTAL
                : Orientation.VERTICAL;
            // dividerPositions
            int divClose = str.indexOf(',', 2, str.length());
            String div = str.substring(2, divClose);
            double[] dividerPositions = new double[] { div.isBlank() ? 0.5 : Double.parseDouble(div) };
            // children
            List<TreeNode> children = splitBranch(str.substring(divClose + 1)).stream()
                .map(s -> fromString(ctx, s, stringToContent))
                .filter(TreeNode.class::isInstance)
                .map(TreeNode.class::cast)
                .toList();
            // create BranchNode
            var branchNode = new BranchNode(ctx);
            branchNode.orientation(orientation);
            branchNode.dividerPositions(dividerPositions);
            branchNode.addChildren(children);
            return branchNode;

        } else if (str.startsWith("[") && str.endsWith("]")) {
            str = str.substring(1, str.length() - 1); // remove '[' ']'
            // children
            String[] split = str.split(",");
            List<Tab> children = Arrays.stream(split)
                .map(MultiTabs::unescape)
                .map(stringToContent)
                .map(c -> new Tab(ctx, c))
                .toList();
            // create LeafNode
            var leafNode = new LeafNode(ctx);
            leafNode.addChildren(children);
            return leafNode;
        }
        return null;
    }

    private static List<String> splitBranch(String str) {
        Deque<Character> deque = new ArrayDeque<>();
        char p = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '[' || c == '{') {
                deque.push(c);
            } else if (c == ']' && !deque.isEmpty() && deque.peek() == '[') {
                deque.pop();
            } else if (c == '}' && !deque.isEmpty() && deque.peek() == '{') {
                deque.pop();
            } else if ((p == ']' || p == '}') && c == ',' && deque.isEmpty()) {
                return List.of(
                    str.substring(0, i),
                    str.substring(i + 1));
            }
            p = c;
        }
        return List.of(str);
    }

    String[][] ESCAPES = { {"%", "%25"}, {"[", "%5B"}, {"]", "%5D"}, {"{", "%7B"}, {"}", "%7D"}, {"\"", "%22"}, {",", "%2C"} };

    private static String escape(String str) {
        if (str == null || str.isBlank()) return null;
        for (String[] rule : ESCAPES) {
            str = str.replace(rule[0], rule[1]);
        }
        return str;
    }

    private static String unescape(String str) {
        if (str == null || str.isBlank()) return null;
        for (int i = ESCAPES.length - 1; i >= 0; i--) {
            str = str.replace(ESCAPES[i][1], ESCAPES[i][0]);
        }
        return str;
    }

}
