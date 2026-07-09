package com.mammb.tabs;

import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.SplitPane;
import java.util.Objects;

public class TreeNode extends SplitPane {

    private final Context ctx;
    private TreeNode parent;

    public TreeNode(Context ctx, TreeNode parent) {
        this.ctx = Objects.requireNonNull(ctx);
        this.parent = parent;
    }

    public TreeNode(Context ctx, TreeNode parent, ContentPane content) {
        this(ctx, parent);
        var leaf = new LeafPane(ctx, this, content);
        getItems().add(leaf);
    }

    public static TreeNode rootOf(Context ctx, ContentPane content) {
        return new TreeNode(ctx, null, content);
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
            TreeNode newChild = new TreeNode(ctx, this);
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

    private void remove(TreeNode treeNode) {
        getItems().remove(treeNode);
        if (getItems().isEmpty() && parent != null) {
            parent.remove(this);
        }
    }

    private void prune() {
        if (getItems().size() == 1 && getItems().getFirst() instanceof TreeNode child) {
            var items = child.getItems().stream().peek(node -> {
                switch (node) {
                    case TreeNode branch -> branch.parent = this;
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
        }
    }


    private Orientation orientation(Side side) {
        return switch (side) {
            case TOP, BOTTOM -> Orientation.VERTICAL;
            default ->  Orientation.HORIZONTAL;
        };
    }

}
