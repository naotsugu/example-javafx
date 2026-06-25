package com.mammb.tabs;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class TilePane extends StackPane {

    private final TabPane tabPane = new TabPane();
    private final Rectangle marker = new Rectangle();
    private TileNode parent;

    public TilePane(TileNode parent, Content content) {
        var tab = new Tab("", content);
        tabPane.getTabs().add(tab);
        this.parent = parent;
    }

}
