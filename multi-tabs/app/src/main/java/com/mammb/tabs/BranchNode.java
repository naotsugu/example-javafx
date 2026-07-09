package com.mammb.tabs;

import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import java.util.Objects;

public class BranchNode extends SplitPane {

    private final Context ctx;
    private BranchNode parent;

    public BranchNode(Context ctx, BranchNode parent) {
        this.ctx = Objects.requireNonNull(ctx);
        this.parent = parent;
    }

    public BranchNode(Context ctx, BranchNode parent, ContentPane content) {
        this(ctx, parent);
        var leaf = new LeafPane(ctx, this, content);
        getItems().add(leaf);
    }

    public static BranchNode rootOf(Context ctx, ContentPane content) {
        return new BranchNode(ctx, null, content);
    }

    public void add(ContentPane content, LeafPane source, Side side) {

        int sourceIndex = getItems().indexOf(source);
        if (sourceIndex < 0) throw new IllegalArgumentException("No such node in content");

        Orientation orientation = orientation(side);

        if (getItems().size() <= 1) {
            // add new leaf pane
            var leaf = new LeafPane(ctx, this, content);
            int insIndex = (side == Side.RIGHT || side == Side.BOTTOM) ? sourceIndex + 1 : sourceIndex;
            getItems().add(insIndex, leaf);
            setOrientation(orientation);
        } else {
            LeafPane node = (LeafPane) getItems().remove(sourceIndex);
            BranchNode newChild = new BranchNode(ctx, this);
            newChild.setOrientation(orientation);
            newChild.getItems().add(node);
            node.parent(newChild);
            int insIndex = (side == Side.RIGHT || side == Side.BOTTOM) ? 1 : 0;
            var leaf = new LeafPane(ctx, newChild, content);
            newChild.getItems().add(insIndex, leaf);
            getItems().add(sourceIndex, newChild);
        }
    }

    void remove(LeafPane leafPane) {
        getItems().remove(leafPane);
        if (getItems().isEmpty() && parent != null) {
            parent.remove(this);
        }
        prune();
    }

    private void remove(BranchNode branchNode) {
        getItems().remove(branchNode);
        if (getItems().isEmpty() && parent != null) {
            parent.remove(this);
        }
    }

    private void prune() {
        if (parent != null && getItems().size() == 1) {
            var child = getItems().getFirst();
            int index = parent.getItems().indexOf(this);
            if (index >= 0) {
                parent.getItems().set(index, child);
                switch (child) {
                    case BranchNode branch -> branch.parent = parent;
                    case LeafPane leaf -> leaf.parent(parent);
                    default -> { }
                }
            }
        } else if (getItems().size() == 1 && getItems().getFirst() instanceof BranchNode child) {
            var items = child.getItems().stream().peek(node -> {
                switch (node) {
                    case BranchNode branch -> branch.parent = this;
                    case LeafPane leaf -> leaf.parent(this);
                    default -> { }
                }
            }).toList();
            getItems().clear();
            setOrientation(child.getOrientation());
            getItems().addAll(items);
        }
        if (parent != null) {
            parent.prune();
        } else if (getItems().isEmpty()) {
            if (ctx.stages().size() > 1) {
                ((Stage) getScene().getWindow()).close();
            } else {
                var leaf = new LeafPane(ctx, this, ctx.contentPaneSupplier().get());
                getItems().add(leaf);
            }
        }
    }


    private Orientation orientation(Side side) {
        return switch (side) {
            case TOP, BOTTOM -> Orientation.VERTICAL;
            default ->  Orientation.HORIZONTAL;
        };
    }

}
