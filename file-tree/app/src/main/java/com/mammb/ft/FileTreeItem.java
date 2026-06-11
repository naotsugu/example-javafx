package com.mammb.ft;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public class FileTreeItem extends TreeItem<Path> {

    private boolean loaded = false;
    private final boolean isDirectory;

    public FileTreeItem(Path value) {
        super(value);
        this.isDirectory = Files.isDirectory(value);
    }

    @Override
    public boolean isLeaf() {
        return !isDirectory;
    }

    @Override
    public ObservableList<TreeItem<Path>> getChildren() {
        if (!loaded) {
            loaded = true;
            if (isDirectory) {
                try (Stream<Path> stream = Files.list(getValue())) {
                    stream.sorted(Comparator.comparing(p -> p.getFileName().toString()))
                          .forEach(path -> super.getChildren().add(new FileTreeItem(path)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return super.getChildren();
    }
}
