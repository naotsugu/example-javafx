package com.mammb.javafx.mosaic;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.StackPane;
import java.nio.file.Path;

public class ContentPane extends StackPane {

    private final SimpleObjectProperty<String> shortNameProperty = new SimpleObjectProperty<>("");
    private final SimpleObjectProperty<String> fullNameProperty = new SimpleObjectProperty<>("");

    private ContentPane(String shortName, String fullName) {
        shortNameProperty.set(shortName);
        fullNameProperty.set(fullName);
    }

    public ContentPane(Path path) {
        this(path.getFileName().toString(), path.toString());
    }

    public ContentPane(String string) {
        if (string == null || string.isBlank()) {
            shortNameProperty.set("Untitled");
        } else {
            String[] strip = string.split(";", 2);
            shortNameProperty.set(strip[0]);
            if (strip.length > 1) {
                fullNameProperty.set(strip[1]);
            }
        }
    }

    public String asString() {
        return shortNameProperty.get() + ";" + fullNameProperty.get();
    }

    public boolean canClose() {
        return true;
    }

    public boolean closeRequest() {
        return true;
    }

    public ReadOnlyObjectProperty<String> shortNameProperty() {
        return shortNameProperty;
    }

    public ReadOnlyObjectProperty<String> fullNameProperty() {
        return fullNameProperty;
    }

}
