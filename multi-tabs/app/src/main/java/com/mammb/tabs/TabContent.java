package com.mammb.tabs;

import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import java.util.Objects;

public class TabContent extends Tab {

    static final DataFormat tabMoveFormat = new DataFormat(TabContent.class.getSimpleName() + ":tabMove");

    private final Context context;

    public TabContent(Context context, ContentPane content) {
        super();
        this.context = context;
        setContent(content);
        var label = new Label(content.nameProperty().get());
        label.setOnDragDetected(this::handleTabDragDetected);
        setGraphic(label);
    }

    private void handleTabDragDetected(MouseEvent e) {
        if (e.getSource() instanceof Label label) {
            Dragboard db = label.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent cc = new ClipboardContent();
            cc.put(tabMoveFormat, String.valueOf(System.identityHashCode(label)));
            context.dragged(this);
            Image image = tabImage(label);
            db.setDragView(image, image.getWidth() / 2, image.getHeight() / 2);
            db.setContent(cc);
        }
    }

    private static Image tabImage(Node node) {
        node = tabNode(node);
        var snapshotParams = new SnapshotParameters();
        snapshotParams.setFill(Color.TRANSPARENT);
        return node.snapshot(snapshotParams, null);
    }

    private static Node tabNode(Node node) {
        for (Node n = node; n != null; n = n.getParent()) {
            if (Objects.equals(n.getClass().getSimpleName(), "TabHeaderSkin"))
                return n;
        }
        return node;
    }
}
