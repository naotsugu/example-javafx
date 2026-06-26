package com.mammb.tabs;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;

public class TreeNode extends StackPane {

    private TreeNode parent;

    private SplitPane children;
    private Orientation orientation;

    public TreeNode(TreeNode parent, Node content) {
        this.parent = parent;
        getChildren().add(new LeafPane(this, content));
    }

    public static TreeNode rootOf(Node content) {
        return new TreeNode(null, content);
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isBranch() {
        return !isLeaf();
    }

    public boolean isLeaf() {
        return children == null;
    }

}
