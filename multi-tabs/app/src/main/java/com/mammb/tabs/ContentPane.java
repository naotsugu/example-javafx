package com.mammb.tabs;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class ContentPane extends StackPane implements Content {
    private String name;
    public ContentPane(Node... children) {
        super();
        getChildren().addAll(children);
    }

    public static ContentPane of(Node... children) {
        return new ContentPane(children);
    }

    @Override
    public String name() {
        return name;
    }
}

