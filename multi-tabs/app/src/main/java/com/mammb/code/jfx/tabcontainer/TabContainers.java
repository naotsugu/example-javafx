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
package com.mammb.code.jfx.tabcontainer;

import com.mammb.code.jfx.tabcontainer.internal.BranchNode;
import com.mammb.code.jfx.tabcontainer.internal.Context;
import com.mammb.code.jfx.tabcontainer.internal.LayoutStore;
import com.mammb.code.jfx.tabcontainer.internal.LeafNode;
import com.mammb.code.jfx.tabcontainer.internal.Tab;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * The TabContainers.
 * @author Naotsugu Kobayashi
 */
public interface TabContainers {

    static SceneBuilder builder() {
        return new SceneBuilder();
    }

    record SceneBuilder(
            Stage stage,
            Function<Object, ? extends ContentPane> toContent,
            BiFunction<Stage, Pane, Scene> toScene,
            BiPredicate<Object, Container> onOpenRequest,
            Path resumePath,
            Function<String, ? extends ContentPane> resumeToContent) {
        public SceneBuilder() {
            this(null, null, null, null, null, null);
        }
        public SceneBuilder stage(Stage stage) {
            return new SceneBuilder(Objects.requireNonNull(stage), toContent, toScene, onOpenRequest, resumePath, resumeToContent);
        }
        public SceneBuilder toContent(Function<Object, ? extends ContentPane> toContent) {
            return new SceneBuilder(stage, Objects.requireNonNull(toContent), toScene, onOpenRequest, resumePath, resumeToContent);
        }
        public SceneBuilder toScene(BiFunction<Stage, Pane, Scene> toScene) {
            return new SceneBuilder(stage, toContent, Objects.requireNonNull(toScene), onOpenRequest, resumePath, resumeToContent);
        }
        public SceneBuilder onOpenRequest(BiPredicate<Object, Container> onOpenRequest) {
            return new SceneBuilder(stage, Objects.requireNonNull(toContent), toScene, onOpenRequest, resumePath, resumeToContent);
        }
        public SceneBuilder resume(Path resumePath, Function<String, ? extends ContentPane> resumeToContent) {
            return new SceneBuilder(stage, toContent, Objects.requireNonNull(toScene), onOpenRequest, resumePath, resumeToContent);
        }
        public Scene build() {
            var ctx = context();
            var st = (stage == null) ? new Stage() : stage;
            return new LayoutStore(ctx, resumePath).load(stage, resumeToContent);
        }
        private Context context() {
            return new Context(
                Objects.requireNonNull(toContent),
                wrappedToScene(toScene, resumePath),
                (onOpenRequest == null) ? (_, _) -> false : onOpenRequest);
        }
    }


    private static BiFunction<Stage, Pane, Scene> wrappedToScene(
            BiFunction<Stage, Pane, Scene> toScene, Path resumePath) {

        BiFunction<Stage, Pane, Scene> toSceneFun = (toScene != null)
            ? toScene
            : (stage, pane) -> new Scene(pane);
        if (resumePath != null) {
            return (stage, pane) -> {
                stage.setOnCloseRequest(TabContainers::handleStageCloseRequest);
                stage.setOnHiding(e -> handleStageHiding(e, resumePath));
                return toSceneFun.apply(stage, pane);
            };
        } else {
            return toSceneFun;
        }
    }

    private static void handleStageCloseRequest(WindowEvent event) {
        if (event.getTarget() instanceof Stage stage) {
            Scene scene = stage.getScene();
            if (scene == null) {
                return;
            }

            if (scene.getRoot().lookup("." + BranchNode.STYLE_CLASS) instanceof BranchNode branchNode) {

                Predicate<ContentPane> predicate = (Stage.getWindows().stream().filter(Window::isShowing).count() > 1)
                    ? ContentPane::canCloseQuiet
                    : ContentPane::canExitQuiet;

                List<ContentPane> contentPanes = branchNode.leaves().stream()
                    .map(LeafNode::children)
                    .flatMap(Collection::stream)
                    .map(Tab::content)
                    .filter(Predicate.not(predicate))
                    .toList();

                for (ContentPane contentPane : contentPanes) {
                    if (!contentPane.closeRequest()) {
                        event.consume();
                        return;
                    }
                }
            }
        }
    }

    private static void handleStageHiding(WindowEvent event, Path resumePath) {
        if (Stage.getWindows().stream().noneMatch(Window::isShowing)) {
            if (event.getTarget() instanceof Stage stage) {
                new LayoutStore(null, resumePath).save(stage);
            }
        }
    }

}
