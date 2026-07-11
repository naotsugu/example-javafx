package com.mammb.javafx.mosaic;

import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class LeafNode extends TreeNode implements ParentOf<Tab> {

    private final TabPane tabPane = new TabPane();
    private final DropMarker dropMarker = new DropMarker();
    private final Context ctx;
    private BranchNode parent;

    public LeafNode(Context ctx, ContentPane content) {

        this.ctx = Objects.requireNonNull(ctx);
        initTabPane();
        initTabButton();
        getChildren().addAll(tabPane, dropMarker);
        addChildren(List.of(new Tab(ctx, content)));

        setOnDragOver(this::handleDragOver);
        setOnDragDropped(this::handleDragDropped);
        setOnDragExited(this::handleDragExited);
    }

    private void initTabPane() {
        tabPane.tabClosingPolicyProperty().set(TabPane.TabClosingPolicy.ALL_TABS);
        tabPane.getSelectionModel().selectedItemProperty().addListener(ctx::handleTabSelected);
        tabPane.getTabs().removeListener(ctx::handleTabRemoved);
    }

    private void handleDragOver(DragEvent e) {

        dropMarker.clear();
        boolean dragOnTabHeader = dragOnTabHeader(e);

        if (e.getDragboard().hasFiles() && dragOnTabHeader) {
            dropMarker.show(innerBounds(tabHeaderArea().getBoundsInLocal(), dropMarker.getStrokeWidth()));
            e.acceptTransferModes(TransferMode.COPY);
            e.consume();
            return;
        }

        Dragboard db = e.getDragboard();
        Tab dragged = ctx.draggedTab();
        if (!db.hasContent(Tab.TAB_MOVE_FORMAT) || dragged == null) return;

        if (dragOnTabHeader) {
            int insertionIndex = insertionIndex(e);
            List<Tab> tabs = children();
            int tabIndex = Math.min(tabs.size() - 1, insertionIndex);
            Node tabNode = tabs.get(tabIndex).tabNode();
            Bounds tabBounds = screenToLocal(tabNode.localToScreen(tabNode.getBoundsInLocal()));
            Bounds bounds = new BoundingBox(
                (insertionIndex > tabIndex) ? tabBounds.getMaxX() : tabBounds.getMinX(),
                tabBounds.getMinY(), // TODO
                dropMarker.getStrokeWidth(),
                tabBounds.getHeight()
            );
            dropMarker.show(bounds);
            e.acceptTransferModes(TransferMode.MOVE);
            e.consume();
        } else {
            dragOnSide(e).ifPresent(side -> {
                dropMarker.show(innerBounds(getLayoutBounds(), dropMarker.getStrokeWidth()), side);
                e.acceptTransferModes(TransferMode.MOVE);
                e.consume();
            });
        }
    }

    private void handleDragDropped(DragEvent e) {

        dropMarker.clear();
        Dragboard db = e.getDragboard();
        boolean dragOnTabHeader = dragOnTabHeader(e);

        if (e.getDragboard().hasFiles() && dragOnTabHeader) {
            List<Path> paths = db.getFiles().stream()
                .filter(File::exists).filter(File::canRead).map(File::toPath).toList();
            paths.stream()
                .map(ctx.contentSupplier())
                .map(contentPane -> new Tab(ctx, contentPane))
                .forEach(tab -> addChildren(List.of(tab)));
            if (paths.isEmpty()) {
                e.setDropCompleted(true);
                e.consume();
            }
            return;
        }

        Tab tab = ctx.draggedTab();
        if (!db.hasContent(Tab.TAB_MOVE_FORMAT) || tab == null) return;

        if (dragOnTabHeader) {
            int tabIndex = Math.min(tabPane.getTabs().size(), insertionIndex(e));
            addChild(tabIndex, new Tab(ctx, tab.content()));
            e.setDropCompleted(true);
            e.consume();
            return;
        }

        dragOnSide(e).ifPresent(side -> {
            parent.add(tab.content(), this, side);
            e.setDropCompleted(true);
            e.consume();
        });

    }

    private void handleDragExited(DragEvent e) {
        dropMarker.clear();
    }

    void eject(Tab node) {
        removeChild(node);
        if (children().isEmpty()) {
            parent.eject(this);
        }
    }

    @Override
    public BranchNode parent() {
        return parent;
    }

    @Override
    public void parent(BranchNode parent) {
        this.parent = parent;
    }

    private boolean dragOnTabHeader(DragEvent e) {
        double gap = 20;
        return tabHeaderArea().getLayoutBounds()
            .contains(Math.max(0, e.getX() - gap), Math.max(0, e.getY() - gap));
    }

    private Optional<Side> dragOnSide(DragEvent e) {
        Bounds bounds = localToScreen(getBoundsInLocal());
        double w = bounds.getWidth() / 3;
        double h = bounds.getHeight() / 3;
        if (new BoundingBox(
            bounds.getMaxX() - w,
            bounds.getMinY() + h,
            w,
            bounds.getHeight() - h * 2).contains(e.getScreenX(), e.getScreenY())) {
            return Optional.of(Side.RIGHT);
        } else if (new BoundingBox(
            bounds.getMinX() + w,
            bounds.getMaxY() - h,
            bounds.getWidth() - w * 2,
            h).contains(e.getScreenX(), e.getScreenY())) {
            return Optional.of(Side.BOTTOM);
        } else if (new BoundingBox(
            bounds.getMinX(),
            bounds.getMinY() + h,
            w,
            bounds.getHeight() - h * 2).contains(e.getScreenX(), e.getScreenY())) {
            return Optional.of(Side.LEFT);
        } else if (new BoundingBox(
            bounds.getMinX() + w,
            bounds.getMinY(),
            bounds.getWidth() - w * 2,
            h).contains(e.getScreenX(), e.getScreenY())) {
            return Optional.of(Side.TOP);
        }
        return Optional.empty();
    }

    private int insertionIndex(DragEvent e) {
        int insertion = 0;
        for (var tab : children()) {
            Node tabNode = tab.tabNode();
            Bounds bounds = tabNode.localToScreen(tabNode.getBoundsInLocal());
            if (e.getScreenX() < bounds.getCenterX()) {
                return insertion;
            }
            insertion++;
        }
        return insertion;
    }

    private Node tabHeaderArea() {
        return tabPane.lookup(".tab-header-area");
    }

    private Bounds innerBounds(Bounds bounds, double gap) {
        return new BoundingBox(
            bounds.getMinX() + gap,
            bounds.getMinY() + gap,
            bounds.getWidth()  - (gap * 2),
            bounds.getHeight() - (gap * 2)
        );
    }

    @Override
    public List<Tab> children() {
        return tabPane.getTabs().stream()
            .filter(Tab.class::isInstance)
            .map(Tab.class::cast)
            .toList();
    }

    @Override
    public void addChildren(List<Tab> tabs) {
        for (var tab : tabs) {
            addChild(children().size(), tab);
        }
    }

    @Override
    public void addChild(int index, Tab child) {
        child.parent(this);
        tabPane.getTabs().add(index, child);
        tabPane.getSelectionModel().select(child);
    }

    @Override
    public boolean removeChild(Tab child) {
        child.parent(null);
        return tabPane.getTabs().remove(child);
    }

    private void initTabButton() {
        TabButton tabButton = new TabButton();
        tabButton.setOnMouseClicked(_ -> {
            Tab newNormalTab = new Tab(ctx, this, ctx.emptyContentSupplier().get());
            int addTabIndex = tabPane.getTabs().indexOf(tabButton);
            tabPane.getTabs().add(addTabIndex, newNormalTab);
            tabPane.getSelectionModel().select(newNormalTab);
        });
        tabPane.getTabs().addLast(tabButton);

        tabPane.getSelectionModel().selectedItemProperty().addListener((_, old, selected) -> {
            if (selected == tabButton) {
                if (old != null && tabPane.getTabs().contains(old)) {
                    tabPane.getSelectionModel().select(old);
                } else {
                    int index = Math.max(0, tabPane.getTabs().size() - 2);
                    tabPane.getSelectionModel().select(index);
                }
            }
        });
    }

    static class TabButton extends javafx.scene.control.Tab {
        private final Label label;
        public TabButton() {
            label = new Label("+");
            var style = """
                -fx-font-weight: bold;
                -fx-font-size: 15px;
                -fx-padding: 0 5 0 5;
                -fx-background-radius: 15px;
                -fx-border-radius: 15px;
                """;
            label.setStyle(style);
            label.setOnMouseEntered(e -> label.setStyle(style + " -fx-background-color: rgba(128, 128, 128, 0.5);"));
            label.setOnMouseExited(e -> label.setStyle(style + " -fx-background-color: transparent;"));
            setGraphic(label);
            setClosable(false);
            getStyleClass().add("add-tab-button");
            setStyle("""
                -fx-background-color: transparent;
                """);
        }
        public final void setOnMouseClicked(EventHandler<? super MouseEvent> event) {
            label.setOnMouseClicked(event);
        }
    }

}
