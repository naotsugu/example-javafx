package com.mammb.tabs;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class LeafPane extends StackPane {

    private final Context context;
    private final TabPane tabPane = new TabPane();
    private final Rectangle marker;
    private TreeNode parent;

    public LeafPane(Context context, TreeNode parent, ContentPane content) {

        this.context = context;
        this.parent = parent;

        marker = new Rectangle();
        marker.setFill(Color.TRANSPARENT);
        marker.setStroke(Color.DARKORANGE);
        marker.setStrokeWidth(1.5);
        marker.setManaged(false);

        getChildren().addAll(tabPane, marker);
        var tab = new ContentTab(context, content);
        tabPane.getTabs().add(tab);

        setOnDragOver(this::handleDragOver);
//        setOnDragDropped(this::handleDragDropped);
       setOnDragExited(this::handleDragExited);
//        setOnDragDone(this::handleDragDone);
    }

    private void handleDragOver(DragEvent e) {
        marker.setVisible(false);
        Dragboard db = e.getDragboard();
        ContentTab dragged = context.dragged();
        if (!db.hasContent(ContentTab.tabMove) || dragged == null) return;
        e.acceptTransferModes(TransferMode.MOVE);

        Bounds bounds = innerBounds();
        marker.setX(bounds.getMinX());
        marker.setY(bounds.getMinY());
        marker.setWidth(bounds.getWidth());
        marker.setHeight(bounds.getHeight());
        marker.setVisible(true);
        var dropPoint = dropPoint(this, e);
        switch (dropPoint) {
            case LEFT -> marker.setWidth(bounds.getWidth() / 2);
            case TOP -> marker.setHeight(bounds.getHeight() / 2);
            case ANY -> e.acceptTransferModes(TransferMode.NONE);
            case RIGHT -> {
                marker.setX(bounds.getCenterX());
                marker.setWidth(bounds.getWidth() / 2);
            }
            case BOTTOM -> {
                marker.setY(bounds.getCenterY());
                marker.setHeight(bounds.getHeight() / 2);
            }
        }
        e.consume();
    }

    private void handleDragExited(DragEvent e) {
        marker.setVisible(false);
    }

    private Bounds innerBounds() {
        Bounds bounds = getLayoutBounds();
        return new BoundingBox(
            bounds.getMinX() + marker.getStrokeWidth(),
            bounds.getMinY() + marker.getStrokeWidth(),
            bounds.getWidth()  - (marker.getStrokeWidth() * 2),
            bounds.getHeight() - (marker.getStrokeWidth() * 2)
        );
    }

    private enum DropPoint { HEADER, TOP, RIGHT, BOTTOM, LEFT, ANY }

    private static DropPoint dropPoint(Node node, DragEvent e) {

        Bounds paneBounds = node.localToScreen(node.getBoundsInLocal());
        double w = paneBounds.getWidth() / 4;
        double h = paneBounds.getHeight() / 4;
        if (new BoundingBox(
            paneBounds.getMinX(),
            paneBounds.getMinY(),
            paneBounds.getWidth(),
            25 * 2).contains(e.getScreenX(), e.getScreenY())) {
            return DropPoint.HEADER;
        } else if (new BoundingBox(
            paneBounds.getMaxX() - w,
            paneBounds.getMinY() + h,
            w,
            paneBounds.getHeight() - h * 2).contains(e.getScreenX(), e.getScreenY())) {
            return DropPoint.RIGHT;
        } else if (new BoundingBox(
            paneBounds.getMinX() + w,
            paneBounds.getMaxY() - h,
            paneBounds.getWidth() - w * 2,
            h).contains(e.getScreenX(), e.getScreenY())) {
            return DropPoint.BOTTOM;
        } else if (new BoundingBox(
            paneBounds.getMinX(),
            paneBounds.getMinY() + h,
            w,
            paneBounds.getHeight() - h * 2).contains(e.getScreenX(), e.getScreenY())) {
            return DropPoint.LEFT;
        } else if (new BoundingBox(
            paneBounds.getMinX() + w,
            paneBounds.getMinY(),
            paneBounds.getWidth() - w * 2,
            h).contains(e.getScreenX(), e.getScreenY())) {
            return DropPoint.TOP;
        } else {
            return DropPoint.ANY;
        }
    }

}
