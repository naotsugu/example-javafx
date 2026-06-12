package com.mammb.ft;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.shape.SVGPath;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

public class PathTreeView extends TreeView<Path> {

    public PathTreeView() {
        super(new PathTreeItem(Paths.get(System.getProperty("user.dir"))));
        setCellFactory(_ -> new PathTreeCell());
        getRoot().setExpanded(true);
        getRoot().expandedProperty().addListener((_, _, expanded) -> {
            if (expanded && getRoot() instanceof PathTreeItem item) {
                item.getChildren();
            }
        });
    }

    static class PathTreeItem extends TreeItem<Path> {

        private boolean loaded = false;
        private final boolean isDirectory;

        public PathTreeItem(Path value) {
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
                            .forEach(path -> super.getChildren().add(new PathTreeItem(path)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return super.getChildren();
        }
    }


    static class PathTreeCell extends TreeCell<Path> {
        @Override
        protected void updateItem(Path item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item.getFileName().toString());
                setGraphic(Files.isDirectory(item) ? folder() : file());
            }
        }
    }

    static SVGPath folder() {
        return svg("""
            M1 3.5A1.5 1.5 0 0 1 2.5 2h2.764c.958 0 1.76.56 2.311 1.184C7.985 3.648 8.48 4 9 4h4.5A1.5 1.5 0 0 1 15 5.5v7a1.5 1.5 0 0 1-1.5 1.5h-11A1.5 1.5 0 0 1 1 12.5zM2.5 3a.5.5 0 0 0-.5.5V6h12v-.5a.5.5 0 0 0-.5-.5H9c-.964 0-1.71-.629-2.174-1.154C6.374 3.334 5.82 3 5.264 3zM14 7H2v5.5a.5.5 0 0 0 .5.5h11a.5.5 0 0 0 .5-.5z
            """);
    }
    static SVGPath file() {
        return svg("""
            M14 4.5V14a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V2a2 2 0 0 1 2-2h5.5zm-3 0A1.5 1.5 0 0 1 9.5 3V1H4a1 1 0 0 0-1 1v12a1 1 0 0 0 1 1h8a1 1 0 0 0 1-1V4.5z
            """);
    }

    private static SVGPath svg(String d) {
        var svg = new SVGPath();
        svg.setContent(d);
        svg.getStyleClass().add("icon");
        return svg;
    }

}
