package com.mammb.tabs;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TabContent extends Tab {

    static final DataFormat TAB_MOVE_FORMAT = new DataFormat(TabContent.class.getSimpleName() + ":tabMove");
    private final List<DropThrough> dropThrough = new ArrayList<>();
    private final Context ctx;
    private final LeafPane parent;

    public TabContent(Context ctx, LeafPane parent, ContentPane content) {
        this.ctx = Objects.requireNonNull(ctx);
        this.parent = Objects.requireNonNull(parent);
        setContent(content);
        setOnCloseRequest(this::handleCloseRequest);
        setOnClosed(this::handleClosed);
        var label = new Label(content.nameProperty().get());
        label.setOnDragDetected(this::handleTabDragDetected);
        label.setOnDragDone(this::handleDragDone);
        setGraphic(label);
    }

    ContentPane content() {
        return (ContentPane) getContent();
    }

    private void handleCloseRequest(Event e) {
        content().handleCloseRequest(e);
        if (e.isConsumed()) return;
    }

    private void handleClosed(Event e) {
        parent.eject(this);
    }

    private void handleTabDragDetected(MouseEvent e) {
        if (e.getSource() instanceof Label label) {
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
    }

    private void handleDragDone(DragEvent e) {
        dropThrough.forEach(DropThrough::close);
        Scene scene = parent.getScene();
        parent.eject(this);
        ctx.dragDone();
        if (scene.getRoot() instanceof BranchNode branchNode && branchNode.getItems().isEmpty()) {
            ((Stage) scene.getWindow()).close();
        }
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
}
