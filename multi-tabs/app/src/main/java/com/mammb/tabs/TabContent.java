package com.mammb.tabs;

import javafx.scene.Node;
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
import java.util.Objects;

public class TabContent extends Tab {

    static final DataFormat tabMoveFormat = new DataFormat(TabContent.class.getSimpleName() + ":tabMove");

    private final Context ctx;

    public TabContent(Context ctx, ContentPane content) {
        super();
        this.ctx = ctx;
        setContent(content);
        var label = new Label(content.nameProperty().get());
        label.setOnDragDetected(this::handleTabDragDetected);
        label.setOnDragDone(this::handleDragDone);
        setGraphic(label);
    }

    ContentPane content() {
        return (ContentPane) getContent();
    }

    private void handleTabDragDetected(MouseEvent e) {
        if (e.getSource() instanceof Label label) {
            Dragboard db = label.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent cc = new ClipboardContent();
            cc.put(tabMoveFormat, String.valueOf(System.identityHashCode(label)));
            ctx.dragged(this);
            Image image = tabImage();
            db.setDragView(image, image.getWidth() / 2, image.getHeight() / 2);
            db.setContent(cc);
        }
    }

    private void handleDragDone(DragEvent e) {
        getTabPane().getTabs().remove(this);
        ctx.clear();
        if (getTabPane().getTabs().isEmpty()) {
            // TODO
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
