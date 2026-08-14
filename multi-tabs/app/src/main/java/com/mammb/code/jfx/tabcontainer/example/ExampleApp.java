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

import com.mammb.code.jfx.tabcontainer.Container;
import com.mammb.code.jfx.tabcontainer.TabContainers;
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

        Scene scene = TabContainers.builder()
            .stage(stage)
            .toContent(LabelContent::new)
            .toScene(this::toScene)
            .onOpenRequest(this::onOpenRequest)
            .resume(Path.of("./build/tab-resume.conf"), LabelContent::new)
            .build();

        stage.setScene(scene);
        stage.show();

    }

    private boolean onOpenRequest(Object arg, Container container) {
        if (arg instanceof Path path) {
            var found = container.find(contentPane -> {
                if (contentPane instanceof LabelContent labelContent) {
                    return Objects.equals(labelContent.shortNameProperty().get(), path.getFileName().toString());
                } else {
                    return false;
                }
            });
            if (found.isPresent()) {
                container.select(found.get());
                return true;
            }
        }
        return false;
    }

    private Scene toScene(Stage stage, Pane pane) {
        stage.setTitle("example");
        return new Scene(new BorderPane(pane));
    }

}
