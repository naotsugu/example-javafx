package com.mammb.tabs;

import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;

public class TreeNode extends StackPane {

    private TreeNode parent;

    private LeafPane pane;

    private SplitPane children;
    private Orientation orientation;

    public TreeNode(TreeNode parent, ContentPane content) {
        this.parent = parent;
        this.pane = new LeafPane(this, content);
        getChildren().add(pane);
    }

    public static TreeNode rootOf(ContentPane content) {
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
