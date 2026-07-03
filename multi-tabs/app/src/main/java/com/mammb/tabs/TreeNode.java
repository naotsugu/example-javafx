package com.mammb.tabs;

import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;

public class TreeNode extends StackPane {

    private final Context context;
    private TreeNode parent;

    private LeafPane pane;

    private SplitPane children;
    private Orientation orientation;

    public TreeNode(Context context, TreeNode parent, ContentPane content) {
        this.context = context;
        this.parent = parent;
        this.pane = new LeafPane(context, this, content);
        getChildren().add(pane);
    }

    public static TreeNode rootOf(Context context, ContentPane content) {
        return new TreeNode(context, null, content);
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
