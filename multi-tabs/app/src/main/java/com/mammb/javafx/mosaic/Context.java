package com.mammb.javafx.mosaic;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Context {

    // last -> front
    private final ObservableList<Stage> stages = FXCollections.observableArrayList();
    private final AtomicReference<Tab> dragged = new AtomicReference<>();
    private Supplier<ContentPane> emptyContentSupplier = () -> new ContentPane();

    Scene installOn(Stage stage, ContentPane contentPane,
            double x, double y, double width, double height) {

        Scene scene = new Scene(new BranchNode(this, contentPane));
        stage.setScene(scene);
        stage.setWidth(width);
        stage.setHeight(height);
        stage.setX(x);
        stage.setX(y);

        stages.add(stage);
        stage.focusedProperty().addListener((_, _, focused) -> {
            // sort by z-order
            if (focused && stages.remove(stage)) stages.add(stage);
        });
        stage.setOnHidden(_ -> stages.remove(stage));

        return scene;
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

}
