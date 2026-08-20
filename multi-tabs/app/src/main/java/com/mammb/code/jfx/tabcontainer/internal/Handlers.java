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

import com.mammb.code.jfx.tabcontainer.ContentPane;
import com.mammb.code.jfx.tabcontainer.TabContainer;
import com.mammb.code.jfx.tabcontainer.TabContainer.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.nio.file.Path;
import java.util.Objects;

/**
 * The Handlers.
 * @author Naotsugu Kobayashi
 */
public class Handlers {

    private final Handler<RequireContent> requireContentHandler;
    private final Handler<RequestContent> requestContentHandler;
    private final Handler<RequireStage> requireStageHandler;

    public Handlers(
        Handler<RequireContent> requireContentHandler,
        Handler<RequestContent> requestContentHandler,
        Handler<RequireStage> requireStageHandler) {
        this.requireContentHandler = Objects.requireNonNull(requireContentHandler);
        this.requestContentHandler = Objects.requireNonNull(requestContentHandler);
        this.requireStageHandler = Objects.requireNonNull(requireStageHandler);
    }

    public void handle(RequireContent requireContent) {
        requireContentHandler.handle(requireContent);
    }

    private void handle(RequestContent requestContent) {
        requestContentHandler.handle(requestContent);
    }

    private void handle(RequireStage requireStage) {
        requireStageHandler.handle(requireStage);
    }

    public ContentPane requireContent() {
        var e = new RequireContent() {
            ContentPane ret;
            @Override public void accept(ContentPane contentPane) { ret = contentPane; }
        };
        handle(e);
        return e.ret;
    }

    public void requestContent(Path path, TabContainer container) {
        record RequestContentRecord(Path argument, TabContainer container) implements RequestContent {}
        handle(new RequestContentRecord(path, container));
    }

    public Stage requireStage(Pane pane) {
        var e = new RequireStage() {
            Stage ret;
            @Override public void accept(Stage stage) { ret = stage; }
            @Override public Pane pane() { return pane; }
        };
        handle(e);
        return e.ret;
    }

//    public void requireStage(Pane pane, Require<Stage> requireStage) {
//        record RequireStageRecord(Pane pane, Require<Stage> requireStage) implements RequireStage {
//            public void accept(Stage stage) { requireStage.accept(stage);}
//        }
//        handle(new RequireStageRecord(pane, requireStage));
//    }

}
