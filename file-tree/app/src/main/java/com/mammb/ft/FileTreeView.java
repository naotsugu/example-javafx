package com.mammb.ft;

import javafx.scene.control.TreeView;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileTreeView extends TreeView<Path> {

    public FileTreeView() {
        super(new FileTreeItem(Paths.get(System.getProperty("user.dir"))));
        setCellFactory(p -> new PathTreeCell());
        getRoot().setExpanded(true);
        getRoot().expandedProperty().addListener((_, _, newValue) -> {
            if (newValue && getRoot() instanceof FileTreeItem item) {
                item.getChildren();
            }
        });
    }
}
