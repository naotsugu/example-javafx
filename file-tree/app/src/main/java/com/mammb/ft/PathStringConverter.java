package com.mammb.ft;

import javafx.util.StringConverter;
import java.nio.file.Path;

public class PathStringConverter extends StringConverter<Path> {
    @Override
    public String toString(Path path) {
        if (path == null) {
            return "";
        }
        Path fileName = path.getFileName();
        return (fileName != null) ? fileName.toString() : path.toString();
    }

    @Override
    public Path fromString(String string) {
        return null;
    }
}
