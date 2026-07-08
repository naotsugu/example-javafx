package com.mammb.tabs;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class DropThroughPane extends Pane {

    private final Stage stage;

    private DropThroughPane(Window owner, Rectangle2D bounds) {

        setStyle("-fx-background-color: transparent;");

        var scene = new Scene(this);
        scene.setFill(Color.TRANSPARENT);

        stage = new Stage();
        stage.initModality(Modality.NONE);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        stage.show();
    }

    public static List<DropThroughPane> create(Context context, Window owner) {
        List<Stage> stages = Window.getWindows().stream()
            .filter(Stage.class::isInstance)
            .map(Stage.class::cast)
            .toList();
        List<DropThroughPane> list = Screen.getScreens().stream()
            .map(screen -> new DropThroughPane(owner, screen.getVisualBounds()))
            .toList();
        Platform.runLater(() -> stages.stream()
            .filter(Predicate.not(Stage::isIconified))
            .sorted(Comparator
                .comparing(Stage::isFocused, Comparator.naturalOrder()))
            .forEach(Stage::toFront));
        return list;
    }

    public void close() {
        stage.close();
    }

}
