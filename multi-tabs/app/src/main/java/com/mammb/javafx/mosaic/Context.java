package com.mammb.javafx.mosaic;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Context {

    // last -> front
    private final ObservableList<Stage> stages = FXCollections.observableArrayList();
    private final AtomicReference<Tab> dragged = new AtomicReference<>();
    private final ConcurrentHashMap<Stage, Tab> latestTab = new ConcurrentHashMap<>();
    private Supplier<? extends ContentPane> emptyContentSupplier = ContentPane::new;
    private Function<Path, ? extends ContentPane> contentSupplier = ContentPane::new;

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

    public Supplier<? extends ContentPane> emptyContentSupplier() {
        return emptyContentSupplier;
    }

    public void emptyContentSupplier(Supplier<? extends ContentPane> emptyContentSupplier) {
        this.emptyContentSupplier = emptyContentSupplier;
    }

    public Function<Path, ? extends ContentPane> contentSupplier() {
        return contentSupplier;
    }

    public void contentSupplier(Function<Path, ? extends ContentPane> contentSupplier) {
        this.contentSupplier = contentSupplier;
    }
}
