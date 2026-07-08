package com.mammb.tabs;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.util.List;

public class DropThroughPane extends Pane {

    private final Stage stage;
    private final Context ctx;

    private DropThroughPane(Context ctx, Rectangle2D bounds) {
        this.ctx = ctx;
        setStyle("-fx-background-color: transparent;");
        setOnDragOver(this::handleDragOver);
        setOnDragDropped(this::handleDragDropped);
        setOnDragExited(this::handleDragExited);

        var scene = new Scene(this);
        scene.setFill(Color.TRANSPARENT);

        ctx.stages().stream()
            .map(s -> new Rectangle(
                    s.getX() - bounds.getMinX(),
                    s.getY() - bounds.getMinY(),
                    s.getWidth(), s.getHeight()))
            .peek(shape -> shape.setFill(Color.TRANSPARENT))
            .peek(shape -> shape.setOnDragEntered(_ -> ctx.toFrontAll()))
            .forEach(shape -> getChildren().add(shape));

        stage = new Stage();
        // stage.initOwner(owner);
        stage.initModality(Modality.NONE);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        stage.show();
    }

    public static List<DropThroughPane> create(Context ctx) {
        List<DropThroughPane> list = Screen.getScreens().stream()
            .map(screen -> new DropThroughPane(ctx, screen.getVisualBounds()))
            .toList();
        ctx.toFrontAll();
        return list;
    }

    private void handleDragOver(DragEvent e) {
        Dragboard db = e.getDragboard();
        TabContent dragged = ctx.drag();
        if (!db.hasContent(TabContent.tabMoveFormat) || dragged == null) return;
        e.acceptTransferModes(TransferMode.MOVE);
        e.consume();
    }

    private void handleDragDropped(DragEvent e) {
        Dragboard db = e.getDragboard();
        TabContent dragged = ctx.drag();
        if (!db.hasContent(TabContent.tabMoveFormat) || dragged == null) return;
//        if (e.getTransferMode() == null && e.getGestureTarget() == null) {
//            double width = content().getWidth();
//            double height = content().getHeight();
//            Point2D pos = content().localToScreen(0, 0);
//            createNewWindow(
//                pos.getX() + width / 4,
//                pos.getY() + height / 4,
//                width, height);
//        }

    }

    private void handleDragExited(DragEvent e) {
    }


    public void close() {
        stage.close();
    }

}
