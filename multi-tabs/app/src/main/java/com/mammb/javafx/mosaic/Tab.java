package com.mammb.javafx.mosaic;

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

class Tab extends javafx.scene.control.Tab {

    static final DataFormat TAB_MOVE_FORMAT = new DataFormat(Tab.class.getSimpleName() + ":tabMove");

    private final List<DropThrough> dropThrough = new ArrayList<>();
    private final Context ctx;
    private final Label label;
    private LeafNode parent;


    Tab(Context ctx, LeafNode parent, ContentPane content) {

        this.ctx = Objects.requireNonNull(ctx);
        this.parent = Objects.requireNonNull(parent);
        this.label = new Label(content.shortNameProperty().getValue());

        setContent(content);
        setGraphic(label);

        setOnCloseRequest(this::handleCloseRequest);
        setOnClosed(this::handleClosed);
        label.setOnDragDetected(this::handleTabDragDetected);
        label.setOnDragDone(this::handleDragDone);
    }

    private void handleCloseRequest(Event e) {
//        content().handleCloseRequest(e);
//        if (e.isConsumed()) return;
    }

    private void handleClosed(Event e) {
//        parent.eject(this);
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
//            Scene preEject = parent.getScene();
//            parent.eject(this);
//            if (preEject.getRoot() instanceof BranchNode branchNode && branchNode.getItems().isEmpty()) {
//                ((Stage) preEject.getWindow()).close();
//            }
        }
        ctx.dragDone();
    }

    ContentPane content() {
        return (ContentPane) getContent();
    }


    private Image tabImage() {
        var snapshotParams = new SnapshotParameters();
        snapshotParams.setFill(Color.TRANSPARENT);
        return tabNode().snapshot(snapshotParams, null);
    }

    private Node tabNode() {
        for (Node n = getGraphic(); n != null; n = n.getParent()) {
            if (Objects.equals(n.getClass().getSimpleName(), "TabHeaderSkin"))
                return n;
        }
        return getGraphic();
    }

}
