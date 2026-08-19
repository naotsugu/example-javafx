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
package com.mammb.code.jfx.tabcontainer.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.mammb.code.jfx.tabcontainer.ContentPane;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * The LayoutStore.
 * @author Naotsugu Kobayashi
 */
public class LayoutStore {

    /** The logger. */
    private static final System.Logger log = System.getLogger(LayoutStore.class.getName());

    /** The Context. */
    private final Context ctx;
    /** The store path. */
    private final Path path;

    /**
     * Constructor.
     * @param ctx the Context
     * @param path the store path
     */
    public LayoutStore(final Context ctx, final Path path) {
        this.ctx = ctx; // TODO Objects.requireNonNull(ctx);
        this.path = Objects.requireNonNull(path);
    }

    public void save(Stage stage) {
        try {
            Files.write(path, asString(stage));
        } catch (IOException e) {
            log.log(System.Logger.Level.ERROR, "failed to write resume file", e);
        }
    }

    public Scene load(Stage stage, Function<String, ? extends ContentPane> resumeToContent) {
        try {
            var lines = Files.readAllLines(path);
            return ctx.toScene(stage, replicate(lines, stage, resumeToContent));
        } catch (IOException e) {
            log.log(System.Logger.Level.ERROR, "failed to read resume file", e);
        }
        stage.setWidth(600);
        stage.setHeight(400);
        return ctx.toScene(stage, new BranchNode(ctx, ctx.createEmptyContent()));
    }


    // -- suspend -------------------------------------------------------------

    private static List<String> asString(Stage stage) {

        Scene scene = stage.getScene();

        String loc = String.join(",",
            Double.toString(stage.getX()),
            Double.toString(stage.getY()),
            Double.toString(scene.getWidth()),
            Double.toString(scene.getHeight()));

        Node node = scene.lookup("." + BranchNode.STYLE_CLASS);
        String str = (node instanceof BranchNode branchNode)
            ? asStringRecursive(branchNode.root())
            : "";

        return List.of(loc, str);
    }


    private static String asStringRecursive(ParentOf<?> parentOf) {

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
                    .map(LayoutStore::asStringRecursive)
                    .collect(Collectors.joining(","))
            ) + "}";

            case LeafNode leafNode -> leafNode.children().stream()
                .map(Tab::content)
                .map(ContentPane::asString)
                .map(LayoutStore::escape)
                .collect(Collectors.joining(",", "[", "]"));

            default -> "";
        };
    }

    // -- resume --------------------------------------------------------------

    public BranchNode replicate(List<String> lines, Stage stage, Function<String, ? extends ContentPane> resumeToContent) {
        try {
            String[] split = lines.getFirst().split(",");

            double x = Double.parseDouble(split[0]);
            double y = Double.parseDouble(split[1]);
            if (Screen.getScreens().stream().anyMatch(screen ->
                screen.getVisualBounds().contains(x, y))) {
                stage.setX(x);
                stage.setY(y);
            }

            double w = Math.max(Double.parseDouble(split[2]), 90);
            double h = Math.max(Double.parseDouble(split[3]), 30);
            stage.setWidth(w);
            stage.setHeight(h);
            Pane pane = fromString(lines.get(1), resumeToContent);
            if (pane instanceof BranchNode branchNode) {
                return branchNode;
            }
        } catch (Exception e) {
            log.log(System.Logger.Level.ERROR, "failed to resume", e);
        }

        stage.setWidth(600);
        stage.setHeight(400);
        return new BranchNode(ctx, ctx.createEmptyContent());
    }


    private Pane fromString(String str, Function<String, ? extends ContentPane> resumeToContent) {

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
                .map(s -> fromString(s, resumeToContent))
                .filter(TreeNode.class::isInstance)
                .map(TreeNode.class::cast)
                .toList();
            // create BranchNode
            var branchNode = new BranchNode(ctx);
            branchNode.orientation(orientation);
            branchNode.addChildren(children);
            Platform.runLater(() -> branchNode.dividerPositions(dividerPositions));
            return branchNode;

        } else if (str.startsWith("[") && str.endsWith("]")) {
            str = str.substring(1, str.length() - 1); // remove '[' ']'
            // children
            String[] split = str.split(",");
            List<Tab> children = Arrays.stream(split)
                .map(LayoutStore::unescape)
                .map(resumeToContent)
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

    // -- utilities -----------------------------------------------------------

    private static final String[][] ESCAPES = { {"%", "%25"}, {"[", "%5B"}, {"]", "%5D"}, {"{", "%7B"}, {"}", "%7D"}, {"\"", "%22"}, {",", "%2C"} };

    private static String escape(String str) {
        if (str == null) return null;
        if (str.isBlank()) return "";
        for (String[] rule : ESCAPES) {
            str = str.replace(rule[0], rule[1]);
        }
        return str;
    }

    private static String unescape(String str) {
        if (str == null) return null;
        if (str.isBlank()) return "";
        for (int i = ESCAPES.length - 1; i >= 0; i--) {
            str = str.replace(ESCAPES[i][1], ESCAPES[i][0]);
        }
        return str;
    }

}
