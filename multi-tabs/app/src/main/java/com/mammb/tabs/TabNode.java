package com.mammb.tabs;

import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;

public class TabNode extends StackPane {
    private final TabPane tabPane = new TabPane();

    public TabNode() {
        getChildren().add(tabPane);
    }
}
