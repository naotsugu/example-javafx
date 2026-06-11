package com.mammb.ft;

import javafx.scene.control.TextArea;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ContentArea extends TextArea {

    public void display(Path path) {
        if (path == null || !Files.isRegularFile(path)) {
            return;
        }
        try {
            setText(Files.readString(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
