package com.mammb.ft;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.SVGPath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PathTreeView extends TreeView<Path> {

    private final List<Consumer<Path>> selectActions = new ArrayList<>();
    private final BooleanProperty compactFolders =
            new SimpleBooleanProperty(this, "compactFolders", true);

    public PathTreeView() {
        this(Paths.get(System.getProperty("user.dir")));
    }

    public PathTreeView(Path... roots) {
        super(new TreeItem<>());
        setShowRoot(false);

        for (Path root : roots) {
            addRoot(root);
        }

        setCellFactory(param -> new PathTreeCell());
        getSelectionModel().selectedItemProperty().addListener(
            (_, _, item) -> {
                if (item != null && item.getValue() != null && !selectActions.isEmpty()) {
                    selectActions.forEach(action -> action.accept(item.getValue()));
                }
            });

        compactFolders.addListener((_, _, newValue) -> {
            List<Path> currentRoots = getRoot().getChildren().stream()
                    .map(TreeItem::getValue).toList();
            getRoot().getChildren().clear();
            for (Path rootPath : currentRoots) {
                PathTreeItem newRoot = new PathTreeItem(rootPath, newValue);
                newRoot.setExpanded(true);
                getRoot().getChildren().add(newRoot);
            }
        });

        // Drag and Drop to add new roots
        setOnDragOver(event -> {
            if (event.getGestureSource() != this && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                List<Path> existingRoots = getRoot().getChildren().stream()
                        .map(TreeItem::getValue)
                        .toList();

                db.getFiles().stream()
                        .map(File::toPath)
                        .filter(Files::isDirectory)
                        .filter(p -> !existingRoots.contains(p)) // Avoid duplicates
                        .forEach(this::addRoot);
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    public void addRoot(Path path) {
        PathTreeItem item = new PathTreeItem(path, isCompactFolders());
        item.setExpanded(true);
        getRoot().getChildren().add(item);
    }

    public boolean isCompactFolders() {
        return compactFolders.get();
    }

    public BooleanProperty compactFoldersProperty() {
        return compactFolders;
    }

    public void setCompactFolders(boolean compactFolders) {
        this.compactFolders.set(compactFolders);
    }

    public void addSelectAction(Consumer<Path> action) {
        selectActions.add(action);
    }

    static class PathTreeItem extends TreeItem<Path> {

        private boolean loaded = false;
        private final boolean isDirectory;
        private final boolean compact;

        public PathTreeItem(Path value, boolean compact) {
            super(value);
            this.isDirectory = Files.isDirectory(value);
            this.compact = compact;
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
                            .forEach(path -> {
                                if (Files.isDirectory(path) && compact) {
                                    super.getChildren().add(buildCompactTreeItem(path));
                                } else {
                                    super.getChildren().add(new PathTreeItem(path, compact));
                                }
                            });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return super.getChildren();
        }

        private TreeItem<Path> buildCompactTreeItem(Path path) {
            List<Path> chain = new ArrayList<>();
            chain.add(path);

            Path current = path;
            while (true) {
                try (Stream<Path> stream = Files.list(current)) {
                    List<Path> children = stream.toList();
                    if (children.size() == 1 && Files.isDirectory(children.getFirst())) {
                        current = children.getFirst();
                        chain.add(current);
                    } else {
                        break;
                    }
                } catch (IOException e) {
                    break;
                }
            }

            if (chain.size() > 1) {
                String displayPath = chain.stream()
                        .map(p -> p.getFileName().toString())
                        .collect(Collectors.joining("/"));
                return new CompactPathTreeItem(current, displayPath, compact);
            } else {
                return new PathTreeItem(path, compact);
            }
        }
    }

    static class CompactPathTreeItem extends PathTreeItem {
        private final String displayPath;

        public CompactPathTreeItem(Path value, String displayPath, boolean compact) {
            super(value, compact);
            this.displayPath = displayPath;
        }

        public String getDisplayPath() {
            return displayPath;
        }
    }


    static class PathTreeCell extends TreeCell<Path> {

        private final ContextMenu rootContextMenu = new ContextMenu();
        private final ContextMenu dirContextMenu = new ContextMenu();
        private final ContextMenu fileContextMenu = new ContextMenu();

        public PathTreeCell() {
            // Root context menu
            MenuItem removeRootItem = new MenuItem("Remove");
            removeRootItem.setOnAction(event -> getTreeView().getRoot().getChildren().remove(getTreeItem()));
            rootContextMenu.getItems().add(removeRootItem);

            // Common items
            MenuItem copyItem = new MenuItem("Copy");
            copyItem.setOnAction(event -> copyPath());
            MenuItem pasteItem = new MenuItem("Paste");
            pasteItem.setOnAction(event -> pastePath());
            MenuItem renameItem = new MenuItem("Rename");
            renameItem.setOnAction(event -> renamePath());
            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(event -> deletePath());

            // Directory context menu
            MenuItem newFileItem = new MenuItem("New File");
            newFileItem.setOnAction(event -> createNew("file"));
            MenuItem newDirItem = new MenuItem("New Directory");
            newDirItem.setOnAction(event -> createNew("dir"));
            dirContextMenu.getItems().addAll(copyItem, pasteItem, new SeparatorMenuItem(), newFileItem, newDirItem, new SeparatorMenuItem(), renameItem, deleteItem);

            // File context menu
            fileContextMenu.getItems().addAll(copyItem, new SeparatorMenuItem(), renameItem, deleteItem);

            // Drag and Drop handlers
            setOnDragDetected(event -> {
                if (getItem() == null) return;
                Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.put(DataFormat.FILES, List.of(getItem().toFile()));
                dragboard.setContent(content);
                event.consume();
            });

            setOnDragOver(event -> {
                if (event.getGestureSource() != this && event.getDragboard().hasFiles()) {
                    Path targetPath = getItem();
                    if (targetPath != null && Files.isDirectory(targetPath)) {
                        event.acceptTransferModes(TransferMode.MOVE);
                    }
                }
                event.consume();
            });

            setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    Path targetDir = getItem();
                    Path sourcePath = db.getFiles().getFirst().toPath();
                    try {
                        Path newPath = targetDir.resolve(sourcePath.getFileName());
                        Files.move(sourcePath, newPath);
                        success = true;
                    } catch (IOException e) {
                        showError("Move Failed", "Could not move " + sourcePath.getFileName() + ": " + e.getMessage());
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            });

            setOnDragDone(event -> {
                if (event.getTransferMode() == TransferMode.MOVE) {
                    getTreeItem().getParent().getChildren().remove(getTreeItem());
                }
                event.consume();
            });
        }


        @Override
        protected void updateItem(Path item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                setContextMenu(null);
            } else {
                TreeItem<Path> treeItem = getTreeItem();
                if (treeItem instanceof CompactPathTreeItem compact) {
                    setText(compact.getDisplayPath());
                } else {
                    setText(item.getFileName().toString());
                }
                setGraphic(Files.isDirectory(item) ? folder() : file());

                // Set context menu
                if (treeItem.getParent() == getTreeView().getRoot()) {
                    setContextMenu(rootContextMenu);
                } else if (Files.isDirectory(item)) {
                    setContextMenu(dirContextMenu);
                } else {
                    setContextMenu(fileContextMenu);
                }
            }
        }

        private void copyPath() {
            ClipboardContent content = new ClipboardContent();
            content.put(DataFormat.FILES, List.of(getItem().toFile()));
            Clipboard.getSystemClipboard().setContent(content);
        }

        private void pastePath() {
            Path targetDir = getItem();
            if (!Files.isDirectory(targetDir)) return;

            Clipboard clipboard = Clipboard.getSystemClipboard();
            if (!clipboard.hasFiles()) return;

            for (File file : clipboard.getFiles()) {
                try {
                    Path sourcePath = file.toPath();
                    Path destPath = targetDir.resolve(sourcePath.getFileName());
                    Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);

                    // Add to tree
                    TreeItem<Path> newItem = new PathTreeItem(destPath, ((PathTreeView) getTreeView()).isCompactFolders());
                    getTreeItem().getChildren().add(newItem);

                } catch (IOException e) {
                    showError("Paste Failed", "Could not paste " + file.getName() + ": " + e.getMessage());
                }
            }
            getTreeItem().getChildren().sort(Comparator.comparing(t -> t.getValue().getFileName().toString()));
        }


        private void createNew(String type) {
            Path parentPath = getItem();
            if (!Files.isDirectory(parentPath)) return;

            TextInputDialog dialog = new TextInputDialog("Untitled");
            dialog.setTitle("Create New " + (type.equals("file") ? "File" : "Directory"));
            dialog.setHeaderText("Enter the name:");
            dialog.setContentText("Name:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                try {
                    Path newPath = parentPath.resolve(name);
                    if (type.equals("file")) {
                        Files.createFile(newPath);
                    } else {
                        Files.createDirectory(newPath);
                    }
                    // Add to tree
                    TreeItem<Path> newItem = new PathTreeItem(newPath, ((PathTreeView) getTreeView()).isCompactFolders());
                    getTreeItem().getChildren().add(newItem);
                    getTreeItem().getChildren().sort(Comparator.comparing(t -> t.getValue().getFileName().toString()));
                } catch (IOException e) {
                    showError("Creation failed", "Could not create " + name + ": " + e.getMessage());
                }
            });
        }

        private void renamePath() {
            Path path = getItem();
            TextInputDialog dialog = new TextInputDialog(path.getFileName().toString());
            dialog.setTitle("Rename");
            dialog.setHeaderText("Enter new name for " + path.getFileName());
            dialog.setContentText("New Name:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(newName -> {
                try {
                    Path newPath = path.resolveSibling(newName);
                    Files.move(path, newPath);
                    getTreeItem().setValue(newPath);
                } catch (IOException e) {
                    showError("Rename failed", "Could not rename " + path.getFileName() + ": " + e.getMessage());
                }
            });
        }

        private void deletePath() {
            Path path = getItem();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Confirmation");
            alert.setHeaderText("Delete " + path.getFileName() + "?");
            alert.setContentText("Are you sure you want to delete this " + (Files.isDirectory(path) ? "directory and its contents?" : "file?"));

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    if (Files.isDirectory(path)) {
                        try (Stream<Path> walk = Files.walk(path)) {
                            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                                try { Files.delete(p); } catch (IOException e) { /* ignore */ }
                            });
                        }
                    } else {
                        Files.delete(path);
                    }
                    getTreeItem().getParent().getChildren().remove(getTreeItem());
                } catch (IOException e) {
                    showError("Delete failed", "Could not delete " + path.getFileName() + ": " + e.getMessage());
                }
            }
        }

        private void showError(String title, String message) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
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
