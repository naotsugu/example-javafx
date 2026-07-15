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
package com.mammb.javafx.pane.tabblock.internal;

import com.mammb.javafx.pane.tabblock.ContentPane;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Tab extends javafx.scene.control.Tab implements ChildOf<LeafNode> {

    static final DataFormat TAB_MOVE_FORMAT = new DataFormat(Tab.class.getSimpleName() + ":tabMove");

    private final List<DropThrough> dropThrough = new ArrayList<>();
    private final Context ctx;
    private final Label label;
    private LeafNode parent;

    public Tab(Context ctx, ContentPane content) {
        this(ctx, null, content);
    }

    Tab(Context ctx, LeafNode parent, ContentPane content) {

        this.ctx = Objects.requireNonNull(ctx);
        this.parent = parent;
        this.label = new Label(content.shortNameProperty().getValue());

        setContent(content);
        setGraphic(label);

        setOnCloseRequest(this::handleCloseRequest);
        setOnClosed(this::handleClosed);
        label.setOnDragDetected(this::handleTabDragDetected);
        label.setOnDragDone(this::handleDragDone);
    }

    private void handleCloseRequest(Event e) {
        if (!content().closeRequest()) {
            e.consume();
        }
    }

    private void handleClosed(Event e) {
        parent.eject(this);
    }

    private void handleTabDragDetected(MouseEvent e) {
        dropThrough.addAll(DropThrough.create(ctx));
        Dragboard db = label.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent cc = new ClipboardContent();
        cc.put(TAB_MOVE_FORMAT, String.valueOf(System.identityHashCode(label)));
        ctx.dragStart(this);
        Image image = tabImage();
        db.setDragView(image, image.getWidth() / 2, image.getHeight() / 2);
        db.setContent(cc);
        e.consume();
    }

    private void handleDragDone(DragEvent e) {
        dropThrough.forEach(DropThrough::close);
        if (e.getTransferMode() == TransferMode.MOVE) {
            parent.eject(this);
        }
        ctx.dragDone();
    }

    public ContentPane content() {
        return (ContentPane) getContent();
    }


    private Image tabImage() {
        var snapshotParams = new SnapshotParameters();
        snapshotParams.setFill(Color.TRANSPARENT);
        return tabNode().snapshot(snapshotParams, null);
    }

    Node tabNode() {
        for (Node n = getGraphic(); n != null; n = n.getParent()) {
            if (Objects.equals(n.getClass().getSimpleName(), "TabHeaderSkin"))
                return n;
        }
        return getGraphic();
    }

    @Override
    public LeafNode parent() {
        return parent;
    }

    @Override
    public void parent(LeafNode parent) {
        this.parent = parent;
    }
}
