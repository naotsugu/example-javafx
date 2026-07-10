package com.mammb.javafx.mosaic;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.StackPane;

public class ContentPane extends StackPane {

    private final SimpleObjectProperty<String> shortNameProperty = new SimpleObjectProperty<>("Untitled");
    private final SimpleObjectProperty<String> fullNameProperty = new SimpleObjectProperty<>("Untitled");

    public ContentPane() {
    }

    public ReadOnlyObjectProperty<String> shortNameProperty() {
        return shortNameProperty;
    }

    public ReadOnlyObjectProperty<String> fullNameProperty() {
        return fullNameProperty;
    }

}
