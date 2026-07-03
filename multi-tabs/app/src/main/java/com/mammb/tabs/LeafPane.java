package com.mammb.tabs;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.Optional;

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
        var tab = new TabContent(context, content);
        tabPane.getTabs().add(tab);

        setOnDragOver(this::handleDragOver);
//        setOnDragDropped(this::handleDragDropped);
       setOnDragExited(this::handleDragExited);
//        setOnDragDone(this::handleDragDone);
    }

    private void handleDragOver(DragEvent e) {

        marker.setVisible(false);
        Dragboard db = e.getDragboard();
        TabContent dragged = context.dragged();
        if (!db.hasContent(TabContent.tabMoveFormat) || dragged == null) return;
        e.acceptTransferModes(TransferMode.MOVE);

        if (dragOnHeader(e)) {
            int insertionIndex = insertionIndex(e);
            int tabIndex = Math.min(tabPane.getTabs().size() - 1, insertionIndex);
            Node tabNode = ((TabContent) tabPane.getTabs().get(tabIndex)).tabNode();
            Bounds ins = screenToLocal(tabNode.localToScreen(tabNode.getBoundsInLocal()));
            marker.setX((insertionIndex > tabIndex) ? ins.getMaxX() : ins.getMinX());
            marker.setY(ins.getMinY());
            marker.setHeight(ins.getHeight());
            marker.setWidth(2);
            marker.setVisible(true);
            e.consume();
            return;
        }

        Bounds bounds = innerBounds();
        marker.setX(bounds.getMinX());
        marker.setY(bounds.getMinY());
        marker.setWidth(bounds.getWidth());
        marker.setHeight(bounds.getHeight());
        marker.setVisible(true);
        switch (dragSide(e).orElse(null)) {
            case LEFT -> marker.setWidth(bounds.getWidth() / 2);
            case TOP -> marker.setHeight(bounds.getHeight() / 2);
            case RIGHT -> {
                marker.setX(bounds.getCenterX());
                marker.setWidth(bounds.getWidth() / 2);
            }
            case BOTTOM -> {
                marker.setY(bounds.getCenterY());
                marker.setHeight(bounds.getHeight() / 2);
            }
            case null -> {}
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


    private boolean dragOnHeader(DragEvent e) {
        Node headerArea = tabPane.lookup(".tab-header-area");
        if (headerArea == null) return false;
        return headerArea.localToScreen(headerArea.getBoundsInLocal())
            .contains(e.getScreenX(), e.getScreenY());
    }

    private Optional<Side> dragSide(DragEvent e) {
        Bounds paneBounds = localToScreen(getBoundsInLocal());
        double w = paneBounds.getWidth() / 3;
        double h = paneBounds.getHeight() / 3;
        if (new BoundingBox(
            paneBounds.getMaxX() - w,
            paneBounds.getMinY() + h,
            w,
            paneBounds.getHeight() - h * 2).contains(e.getScreenX(), e.getScreenY())) {
            return Optional.of(Side.RIGHT);
        } else if (new BoundingBox(
            paneBounds.getMinX() + w,
            paneBounds.getMaxY() - h,
            paneBounds.getWidth() - w * 2,
            h).contains(e.getScreenX(), e.getScreenY())) {
            return Optional.of(Side.BOTTOM);
        } else if (new BoundingBox(
            paneBounds.getMinX(),
            paneBounds.getMinY() + h,
            w,
            paneBounds.getHeight() - h * 2).contains(e.getScreenX(), e.getScreenY())) {
            return Optional.of(Side.LEFT);
        } else if (new BoundingBox(
            paneBounds.getMinX() + w,
            paneBounds.getMinY(),
            paneBounds.getWidth() - w * 2,
            h).contains(e.getScreenX(), e.getScreenY())) {
            return Optional.of(Side.TOP);
        }
        return Optional.empty();
    }

    private int insertionIndex(DragEvent e) {
        int insertion = 0;
        for (Tab tab : tabPane.getTabs()) {
            Node tabNode = ((TabContent) tab).tabNode();
            Bounds bounds = tabNode.localToScreen(tabNode.getBoundsInLocal());
            if (e.getScreenX() < bounds.getCenterX()) {
                return insertion;
            }
            insertion++;
        }
        return insertion;
    }


}
