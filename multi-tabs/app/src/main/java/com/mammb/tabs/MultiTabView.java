package com.mammb.tabs;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class MultiTabView extends StackPane {
    private TilePane root;

    public MultiTabView(Node note) {
        this.root = TilePane.rootOf(note);
        getChildren().add(root);
    }

}
