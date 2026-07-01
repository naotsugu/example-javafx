package com.mammb.tabs;

import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class LeafPane extends StackPane {

    private final TabPane tabPane = new TabPane();
    private final Rectangle marker = new Rectangle();
    private TreeNode parent;

    public LeafPane(TreeNode parent, ContentPane content) {
        getChildren().add(tabPane);
        this.parent = parent;
        var tab = new ContentTab(content);
        tabPane.getTabs().add(tab);
    }
}
