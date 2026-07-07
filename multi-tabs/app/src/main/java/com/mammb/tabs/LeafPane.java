package com.mammb.tabs;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import java.io.File;
import java.util.Objects;
import java.util.Optional;

public class LeafPane extends StackPane {

    private final Context context;
    private final TabPane tabPane = new TabPane();
    private final DropMarker dropMarker = new DropMarker();
    private TreeNode parent;

    public LeafPane(Context context, TreeNode parent, ContentPane content) {

        this.context = Objects.requireNonNull(context);
        this.parent = Objects.requireNonNull(parent);

        getChildren().addAll(tabPane, dropMarker);
        var tab = new TabContent(context, this, content);
        tabPane.getTabs().add(tab);

        setOnDragOver(this::handleDragOver);
        setOnDragDropped(this::handleDragDropped);
        setOnDragExited(this::handleDragExited);
    }

    private void handleDragOver(DragEvent e) {

        dropMarker.clear();
        boolean dragOnTabHeader = dragOnTabHeader(e);

        if (e.getDragboard().hasFiles() && dragOnTabHeader) {
            Node tabHeaderArea = tabHeaderArea();
            Bounds bounds = innerBounds(screenToLocal(
                tabHeaderArea.localToScreen(tabHeaderArea.getBoundsInLocal())));
            dropMarker.show(bounds);
            e.acceptTransferModes(TransferMode.COPY);
            e.consume();
            return;
        }

        Dragboard db = e.getDragboard();
        TabContent dragged = context.drag();
        if (!db.hasContent(TabContent.tabMoveFormat) || dragged == null) return;
        e.acceptTransferModes(TransferMode.MOVE);

        if (dragOnTabHeader) {
            int insertionIndex = insertionIndex(e);
            int tabIndex = Math.min(tabPane.getTabs().size() - 1, insertionIndex);
            Node tabNode = ((TabContent) tabPane.getTabs().get(tabIndex)).tabNode();
            Bounds tabBounds = screenToLocal(tabNode.localToScreen(tabNode.getBoundsInLocal()));
            Bounds bounds = new BoundingBox(
                (insertionIndex > tabIndex) ? tabBounds.getMaxX() : tabBounds.getMinX(),
                tabBounds.getMinY(),
                dropMarker.getStrokeWidth(),
                tabBounds.getHeight()
            );
            dropMarker.show(bounds);
            e.acceptTransferModes(TransferMode.MOVE);
            e.consume();
            return;
        }

        Bounds bounds = innerBounds(getLayoutBounds());
        Side side = dragOnSide(e).orElse(null);
        dropMarker.show(bounds, side);
        e.acceptTransferModes(TransferMode.MOVE);
        e.consume();
    }

    private void handleDragDropped(DragEvent e) {
        dropMarker.clear();
        Dragboard db = e.getDragboard();
        boolean dragOnTabHeader = dragOnTabHeader(e);
        if (e.getDragboard().hasFiles() && dragOnTabHeader) {
            db.getFiles().stream().filter(File::canRead)
                .map(File::toPath)
                .map(path -> new ContentPane(new Label(path.toString()), path.getFileName().toString()))
                .map(contentPane -> new TabContent(context, this, contentPane))
                .forEach(tab -> tabPane.getTabs().add(tab));
            e.setDropCompleted(true);
            e.consume();
            return;
        }

        TabContent dragged = context.drag();
        if (!db.hasContent(TabContent.tabMoveFormat) || dragged == null) return;

        if (dragOnTabHeader) {
            int tabIndex = Math.min(tabPane.getTabs().size(), insertionIndex(e));
            var tabContent = new TabContent(context, this, dragged.content());
            tabPane.getTabs().add(tabIndex, tabContent);
            e.setDropCompleted(true);
            e.consume();
            return;
        }

        Side side = dragOnSide(e).orElse(null);
        parent.add(dragged.content(), this, side);
        e.setDropCompleted(true);
        e.consume();
    }

    public void eject(TabContent tab) {
        tabPane.getTabs().remove(tab);
        if (tabPane.getTabs().isEmpty()) {
            parent.remove(this);
        }
    }

    private void handleDragExited(DragEvent e) {
        dropMarker.clear();
    }

    public void parent(TreeNode parent) {
        this.parent = parent;
    }

    private Bounds innerBounds(Bounds bounds) {
        return new BoundingBox(
            bounds.getMinX() + dropMarker.getStrokeWidth(),
            bounds.getMinY() + dropMarker.getStrokeWidth(),
            bounds.getWidth()  - (dropMarker.getStrokeWidth() * 2),
            bounds.getHeight() - (dropMarker.getStrokeWidth() * 2)
        );
    }


    private boolean dragOnTabHeader(DragEvent e) {
        Node headerArea = tabHeaderArea();
        if (headerArea == null) return false;
        double gap = 20;
        return headerArea.getLayoutBounds()
            .contains(Math.max(0, e.getX() - gap), Math.max(0, e.getY() - gap));
    }

    private Node tabHeaderArea() {
        return tabPane.lookup(".tab-header-area");
    }

    private Optional<Side> dragOnSide(DragEvent e) {
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
