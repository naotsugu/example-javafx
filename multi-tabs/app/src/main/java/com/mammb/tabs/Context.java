package com.mammb.tabs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public class Context {

    // last -> front
    private final ObservableList<Stage> stages = FXCollections.observableArrayList();
    private final AtomicReference<TabContent> dragged = new AtomicReference<>();

    public Scene createScene(Stage stage, ContentPane contentPane) {

        TreeNode treeNode = TreeNode.rootOf(this, contentPane);
        Scene scene = new Scene(treeNode);
        stage.setTitle("Tabs");
        stage.setScene(scene);

        stages.add(stage);
        stage.focusedProperty().addListener((_, _, focused) -> {
            if (focused && stages.remove(stage)) stages.add(stage);
        });
        stage.setOnCloseRequest(_ -> stages.remove(stage));
        return scene;
    }

    public void toFrontAll() {
        stages.stream()
            .filter(Predicate.not(Stage::isIconified))
            .filter(Predicate.not(Stage::isAlwaysOnTop))
            .forEach(Stage::toFront);
    }

    public void drag(TabContent tabContent) {
        dragged.set(tabContent);
    }

    public TabContent drag() {
        return dragged.get();
    }

    public void dragDone() {
        dragged.set(null);
    }

}
