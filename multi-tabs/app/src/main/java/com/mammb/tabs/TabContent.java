package com.mammb.tabs;

import javafx.event.Event;
import javafx.geometry.Point2D;
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

    static final DataFormat tabMoveFormat = new DataFormat(TabContent.class.getSimpleName() + ":tabMove");
    private final List<DropThroughPane> dropThroughPanes = new ArrayList<>();
    private final Context ctx;
    private final LeafPane parent;

    public TabContent(Context ctx, LeafPane parent, ContentPane content) {
        super();
        this.ctx = Objects.requireNonNull(ctx);
        this.parent = Objects.requireNonNull(parent);
        setContent(content);
        setOnClosed(this::handleClosed);
        var label = new Label(content.nameProperty().get());
        label.setOnDragDetected(this::handleTabDragDetected);
        label.setOnDragDone(this::handleDragDone);
        setGraphic(label);
    }

    ContentPane content() {
        return (ContentPane) getContent();
    }

    private void handleClosed(Event e) {
        parent.eject(this);
    }

    private void handleTabDragDetected(MouseEvent e) {
        if (e.getSource() instanceof Label label) {
            dropThroughPanes.addAll(DropThroughPane.create(ctx, parent.getScene().getWindow()));


            Dragboard db = label.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent cc = new ClipboardContent();
            cc.put(tabMoveFormat, String.valueOf(System.identityHashCode(label)));
            ctx.drag(this);
            Image image = tabImage();
            db.setDragView(image, image.getWidth() / 2, image.getHeight() / 2);
            db.setContent(cc);
            e.consume();
        }
    }

    private void handleDragDone(DragEvent e) {
        dropThroughPanes.forEach(DropThroughPane::close);
//        if (e.getTransferMode() == null && e.getGestureTarget() == null) {
//            double width = content().getWidth();
//            double height = content().getHeight();
//            Point2D pos = content().localToScreen(0, 0);
//            createNewWindow(
//                pos.getX() + width / 4,
//                pos.getY() + height / 4,
//                width, height);
//        }
        parent.eject(this);
        ctx.dragDone();
    }

    private void createNewWindow(double x, double y, double width, double height) {
        Stage newStage = new Stage();
        newStage.setWidth(width);
        newStage.setHeight(height);
        newStage.setX(x);
        newStage.setY(y);
        ctx.createScene(newStage, content());
        newStage.show();
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
