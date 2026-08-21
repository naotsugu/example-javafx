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
package com.mammb.code.jfx.tabcontainer.example;

import com.mammb.code.jfx.tabcontainer.ContainerHandle;
import com.mammb.code.jfx.tabcontainer.ContentPane;
import com.mammb.code.jfx.tabcontainer.TabContainer;
import com.mammb.code.jfx.tabcontainer.TabContainer.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.nio.file.Path;
import java.util.Objects;

public class ExampleApp extends Application {

    @Override
    public void start(Stage stage) {

        var tabContainer = TabContainer.of(
            this::handleRequireContent, this::handleRequestContent, this::handleRequireStage);
        var pane = tabContainer.resume(stage, Path.of("./build/tab-resume.conf"),
            LabelContent::new);

        intiStage(stage, pane);
        stage.show();

    }

    private ContentPane handleRequireContent() {
        return new LabelContent(null);
    }

    private Stage handleRequireStage(Pane pane) {
        return intiStage(new Stage(), pane);
    }

    private Stage intiStage(Stage stage, Pane pane) {
        stage.setTitle("example");
        stage.setScene(new Scene(new BorderPane(pane)));
        return stage;
    }

    private void handleRequestContent(Path path, ContainerHandle containerHandle) {
        if (path == null) {
            return;
        }
        var found = containerHandle.find(contentPane -> {
            if (contentPane instanceof LabelContent labelContent) {
                return Objects.equals(labelContent.shortNameProperty().get(), path.getFileName().toString());
            } else {
                return false;
            }
        });
        if (found.isPresent()) {
            containerHandle.select(found.get());
        } else {
            containerHandle.add(new LabelContent(path));
        }
    }

}
