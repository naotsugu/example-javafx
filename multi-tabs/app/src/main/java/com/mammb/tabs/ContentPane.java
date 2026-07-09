package com.mammb.tabs;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class ContentPane extends StackPane {

    private final SimpleObjectProperty<String> nameProperty = new SimpleObjectProperty<>();

    public ContentPane(Node child, String name) {
        super();
        nameProperty.set(name);
        getChildren().add(child);
    }

    public static ContentPane of(Node child, String name) {
        return new ContentPane(child, name);
    }

    public ReadOnlyObjectProperty<String> nameProperty() { return nameProperty; }

    public void handleCloseRequest(Event e) {
    }
}

