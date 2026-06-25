package com.mammb.tabs;

import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;

public class TileNode extends StackPane {

    private TileNode parent;

    private SplitPane children;
    private Orientation orientation;

    public TileNode(TileNode parent, Content content) {
        this.parent = parent;
        getChildren().setAll(new TilePane(this, content));
    }

    public static TileNode rootOf(Content content) {
        return new TileNode(null, content);
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
