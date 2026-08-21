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

import com.mammb.code.jfx.tabcontainer.internal.Context;
import com.mammb.code.jfx.tabcontainer.internal.Handlers;
import com.mammb.code.jfx.tabcontainer.internal.Resume;
import com.mammb.code.jfx.tabcontainer.internal.TabContainerImpl;
import javafx.geometry.Side;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javax.swing.text.AbstractDocument;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * The TabContainer.
 * @author Naotsugu Kobayashi
 */
public interface TabContainer {

    interface RequireContent { ContentPane content(); }
    interface RequireStage { Stage stage(Pane pane); }
    interface RequestContent { void accept(Path path, ContainerHandle containerHandle); }

    static TabContainer of(
        RequireContent requireContent,
        RequestContent requestContent,
        RequireStage requireStage) {

        var handlers = new Handlers(
            requireContent,
            requestContent,
            requireStage);

        return new TabContainerImpl(new Context(handlers));
    }

    Pane resume(Stage stage, Path path, Function<String, ? extends ContentPane> resumeToContent);

    Pane create(Stage stage);

    void add(ContentPane contentPane);


}
