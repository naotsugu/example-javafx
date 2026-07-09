package com.mammb.tabs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Context {

    // last -> front
    private final ObservableList<Stage> stages = FXCollections.observableArrayList();
    private final AtomicReference<TabContent> dragged = new AtomicReference<>();
    private Supplier<ContentPane> contentPaneSupplier = () -> new ContentPane(new Label("empty"), "empty");

    public Scene createScene(Stage stage, ContentPane contentPane, double width, double height) {

        BranchNode branchNode = BranchNode.rootOf(this, contentPane);
        Scene scene = new Scene(branchNode);
        stage.setScene(scene);
        stage.setWidth(width);
        stage.setHeight(height);

        stages.add(stage);
        stage.focusedProperty().addListener((_, _, focused) -> {
            if (focused && stages.remove(stage)) stages.add(stage);
        });
        stage.setOnCloseRequest(_ -> stages.remove(stage));
        return scene;
    }

    public void toFrontAll() {
        stages().stream()
            .filter(Predicate.not(Stage::isIconified))
            .filter(Predicate.not(Stage::isAlwaysOnTop))
            .forEach(Stage::toFront);
    }

    public List<Stage> stages() {
        return stages.stream().toList();
    }

    public void dragStart(TabContent tabContent) {
        dragged.set(tabContent);
    }

    public TabContent dragElement() {
        return dragged.get();
    }

    public void dragDone() {
        dragged.set(null);
    }
    public Supplier<ContentPane> contentPaneSupplier() {
        return contentPaneSupplier;
    }
    public void setContentPaneSupplier(Supplier<ContentPane> contentPaneSupplier) {
        this.contentPaneSupplier = contentPaneSupplier;
    }

}
