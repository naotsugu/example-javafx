package com.mammb.javafx.mosaic;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.StackPane;
import java.util.Objects;

public class LeafNode extends StackPane implements ChildOf<BranchNode> {

    private final TabPane tabPane = new TabPane();
    private final DropMarker dropMarker = new DropMarker();
    private final Context ctx;
    private BranchNode parent;

    public LeafNode(Context ctx, BranchNode parent, ContentPane content) {

        this.ctx = Objects.requireNonNull(ctx);
        this.parent = Objects.requireNonNull(parent);

        getChildren().addAll(tabPane, dropMarker);
        tabPane.getTabs().add(new Tab(ctx, this, content));

        setOnDragOver(this::handleDragOver);
        setOnDragDropped(this::handleDragDropped);
        setOnDragExited(this::handleDragExited);
    }

    private void handleDragOver(DragEvent e) {

    }

    private void handleDragDropped(DragEvent e) {

    }

    private void handleDragExited(DragEvent e) {
        dropMarker.clear();
    }


    @Override
    public BranchNode parent() {
        return parent;
    }

    @Override
    public void parent(BranchNode parent) {
        this.parent = Objects.requireNonNull(parent);
    }

    private Node tabHeaderArea() {
        return tabPane.lookup(".tab-header-area");
    }

    private Bounds innerBounds(Bounds bounds, double gap) {
        return new BoundingBox(
            bounds.getMinX() + gap,
            bounds.getMinY() + gap,
            bounds.getWidth()  - (gap * 2),
            bounds.getHeight() - (gap * 2)
        );
    }

}
