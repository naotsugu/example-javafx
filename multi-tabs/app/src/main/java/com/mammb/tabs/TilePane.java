package com.mammb.tabs;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;

public class TilePane extends StackPane {
    private final SplitPane splitPane;
    private TilePane parent;

    private TilePane(TilePane parent, Node... contents) {
        this.splitPane = new SplitPane();
        this.parent = parent;
        this.splitPane.getItems().addAll(contents);
        getChildren().add(this.splitPane);
    }

    public static TilePane rootOf(Node node) {
        return new TilePane(null, node);
    }

    public void add(Node node, int index, Orientation orientation) {

    }

    // ------------------------------------------------------------------------

    public Orientation orientation() {
        return splitPane.getOrientation();
    }

    public void orientation(Orientation orientation) {
        splitPane.setOrientation(orientation);
    }

}
