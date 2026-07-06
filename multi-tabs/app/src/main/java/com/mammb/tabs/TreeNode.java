package com.mammb.tabs;

import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;

public class TreeNode extends SplitPane {

    private final Context context;
    private TreeNode parent;

    public TreeNode(Context context, TreeNode parent) {
        this.context = context;
        this.parent = parent;
    }
    public TreeNode(Context context, TreeNode parent, ContentPane content) {
        this(context, parent);
        var leaf = new LeafPane(context, this, content);
        getItems().add(leaf);
    }

    public static TreeNode rootOf(Context context, ContentPane content) {
        return new TreeNode(context, null, content);
    }

    public void add(ContentPane content, LeafPane source, Side side) {

        int sourceIndex = getItems().indexOf(source);
        if (sourceIndex < 0) throw new IllegalArgumentException("No such node in content");

        Orientation orientation = orientation(side);

        if (getItems().size() <= 1 || getOrientation() == orientation) {
            var leaf = new LeafPane(context, this, content);
            int insIndex = (side == Side.RIGHT || side == Side.BOTTOM) ? sourceIndex + 1 : sourceIndex;
            getItems().add(insIndex, leaf);
            setOrientation(orientation);
        } else {
            LeafPane node = (LeafPane) getItems().remove(sourceIndex);
            TreeNode newChild = new TreeNode(context, this);
            newChild.setOrientation(orientation);
            newChild.getItems().add(node);
            node.parent(newChild);
            int insIndex = (side == Side.RIGHT || side == Side.BOTTOM) ? 1 : 0;
            var leaf = new LeafPane(context, newChild, content);
            newChild.getItems().add(insIndex, leaf);
            getItems().add(sourceIndex, newChild);
        }
    }

    public void eject(LeafPane leafPane) {
        getItems().remove(leafPane);
        if (getItems().isEmpty() && !isRoot()) {
            parent.eject(this);
        }
    }

    public void eject(TreeNode treeNode) {
        getItems().remove(treeNode);
        if (getItems().isEmpty() && !isRoot()) {
            parent.eject(this);
        }
    }

    public boolean isRoot() {
        return parent == null;
    }

    private Orientation orientation(Side side) {
        return switch (side) {
            case TOP, BOTTOM -> Orientation.VERTICAL;
            default ->  Orientation.HORIZONTAL;
        };
    }

}
