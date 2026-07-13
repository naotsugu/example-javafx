package com.mammb.javafx.mosaic;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;

public class Context {

    // last -> front
    private final ObservableList<Stage> stages = FXCollections.observableArrayList();
    private final ObservableMap<Scene, Tab> latestTab = FXCollections.observableHashMap();
    private final AtomicReference<Tab> dragged = new AtomicReference<>();
    private Function<String, ? extends ContentPane> contentSupplier = ContentPane::new;
    private Function<Path, ? extends ContentPane> pathContentSupplier = ContentPane::new;

    public Context() {

    }

    public Context(Stage stage) {
        addStage(stage);
    }

    void addStage(Stage stage) {
        stages.add(stage);
        stage.focusedProperty().addListener((_, _, focused) -> {
            // sort by z-order
            if (focused && stages.remove(stage)) stages.add(stage);
        });
        stage.setOnHidden(_ -> stages.remove(stage));
    }

    void toFrontAll() {
        stages().stream()
            .filter(Predicate.not(Stage::isIconified))
            .filter(Predicate.not(Stage::isAlwaysOnTop))
            .forEach(Stage::toFront);
    }

    List<Stage> stages() {
        return stages.stream().toList();
    }

    void dragStart(Tab tab) {
        dragged.set(tab);
    }

    Tab draggedTab() {
        return dragged.get();
    }

    public void dragDone() {
        dragged.set(null);
    }

    public Function<String, ? extends ContentPane> contentSupplier() {
        return contentSupplier;
    }

    public void contentSupplier(Function<String, ? extends ContentPane> function) {
        this.contentSupplier = Objects.requireNonNull(function);
    }

    public Function<Path, ? extends ContentPane> pathContentSupplier() {
        return pathContentSupplier;
    }

    public void pathContentSupplier(Function<Path, ? extends ContentPane> function) {
        this.pathContentSupplier = Objects.requireNonNull(function);
    }

    public void handleTabSelected(ObservableValue<? extends javafx.scene.control.Tab> observable, javafx.scene.control.Tab oldValue, javafx.scene.control.Tab newValue) {
        if (newValue instanceof Tab selected && selected.parent() != null && selected.parent().getScene() != null) {
            latestTab.put(selected.parent().getScene(), selected);
        }
    }

    public void handleTabRemoved(ListChangeListener.Change<? extends javafx.scene.control.Tab> change) {
        while (change.next()) {
            for (var removed : change.getRemoved()) {
                if (removed instanceof Tab tab && tab.parent() != null && tab.parent().getScene() != null) {
                    latestTab.put(tab.parent().getScene(), tab);
                }
            }
        }
    }

}
