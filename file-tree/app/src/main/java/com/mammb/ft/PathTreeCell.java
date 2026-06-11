package com.mammb.ft;

import javafx.scene.control.TreeCell;
import java.nio.file.Files;
import java.nio.file.Path;

public class PathTreeCell extends TreeCell<Path> {

    private final PathStringConverter converter = new PathStringConverter();

    @Override
    protected void updateItem(Path item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            setText(converter.toString(item));
            setGraphic(Files.isDirectory(item) ? Icon.folder() : Icon.file());
        }
    }
}
