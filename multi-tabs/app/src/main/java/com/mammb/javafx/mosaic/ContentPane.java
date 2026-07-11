package com.mammb.javafx.mosaic;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.StackPane;
import java.nio.file.Path;

public class ContentPane extends StackPane {

    private final SimpleObjectProperty<String> shortNameProperty = new SimpleObjectProperty<>("");
    private final SimpleObjectProperty<String> fullNameProperty = new SimpleObjectProperty<>("");

    public ContentPane(String shortName, String fullName) {
        shortNameProperty.set(shortName);
        fullNameProperty.set(fullName);
    }
    public ContentPane() {
        this("Untitled", "");
    }
    public ContentPane(Path path) {
        this(path.getFileName().toString(), path.toString());
    }

    public ReadOnlyObjectProperty<String> shortNameProperty() {
        return shortNameProperty;
    }

    public ReadOnlyObjectProperty<String> fullNameProperty() {
        return fullNameProperty;
    }

}
