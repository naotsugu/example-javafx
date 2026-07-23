package com.mammb.code.jfx.multitab;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import java.nio.file.Path;

public class LabelContent extends ContentPane {

    private final SimpleObjectProperty<String> shortNameProperty = new SimpleObjectProperty<>("");
    private final SimpleObjectProperty<String> fullNameProperty = new SimpleObjectProperty<>("");

    private LabelContent(String shortName, String fullName) {
        shortNameProperty.set(shortName);
        fullNameProperty.set(fullName);
    }

    public LabelContent(Path path) {
        this(path.getFileName().toString(), path.toString());
    }

    public LabelContent(String string) {
        if (string == null || string.isBlank()) {
            shortNameProperty.set("Untitled");
        } else {
            String[] strip = string.split(System.getProperty("path.separator", ";"), 2);
            shortNameProperty.set(strip[0]);
            if (strip.length > 1) {
                fullNameProperty.set(strip[1]);
            }
        }
    }

    @Override
    public void focus() {

    }

    @Override
    public boolean canCloseQuiet() {
        return true;
    }

    @Override
    public boolean closeRequest() {
        return true;
    }

    @Override
    public void close() {

    }

    @Override
    public String asString() {
        return shortNameProperty.get() + System.getProperty("path.separator", ";") + fullNameProperty.get();
    }

    @Override
    public ReadOnlyObjectProperty<String> shortNameProperty() {
        return shortNameProperty;
    }

    @Override
    public ReadOnlyObjectProperty<String> fullNameProperty() {
        return fullNameProperty;
    }

}
