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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import com.mammb.code.jfx.tabcontainer.ContentPane;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * The Resume.
 * @author Naotsugu Kobayashi
 */
public class Resume {

    /** The logger. */
    private static final System.Logger log = System.getLogger(Resume.class.getName());

    private final Context ctx;
    private final Function<String, ? extends ContentPane> resumeToContent;

    private Resume(Context ctx, Function<String, ? extends ContentPane> resumeToContent) {
        this.ctx = ctx;
        this.resumeToContent = resumeToContent;
    }

    public static Resume of(Context ctx, Function<String, ? extends ContentPane> resumeToContent) {
        return new Resume(ctx, resumeToContent);
    }

    public BranchNode load(Path path, Stage stage) {
        try {
            var lines = Files.readAllLines(path);
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
            Pane pane = fromString(lines.get(1));
            if (pane instanceof BranchNode branchNode) {
                return branchNode;
            }
        } catch (Exception e) {
            log.log(System.Logger.Level.ERROR, "Failed to read resume file", e);
        }

        stage.setWidth(600);
        stage.setHeight(400);
        return new BranchNode(ctx, resumeToContent.apply(""));
    }


    private Pane fromString(String str) {

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
                .map(this::fromString)
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
                .map(Resume::unescape)
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


    private static String unescape(String str) {
        if (str == null || str.isBlank()) return null;
        for (int i = Suspend.ESCAPES.length - 1; i >= 0; i--) {
            str = str.replace(Suspend.ESCAPES[i][1], Suspend.ESCAPES[i][0]);
        }
        return str;
    }

}
