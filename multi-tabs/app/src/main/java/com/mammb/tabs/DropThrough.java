package com.mammb.tabs;

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

public class DropThrough extends Pane {

    private final Stage stage;
    private final Context ctx;

    private DropThrough(Context ctx, Rectangle2D bounds) {
        this.ctx = ctx;
        setStyle("-fx-background-color: transparent;");
        setOnDragOver(this::handleDragOver);
        setOnDragDropped(this::handleDragDropped);

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

    public static List<DropThrough> create(Context ctx) {
        List<DropThrough> list = Screen.getScreens().stream()
            .map(screen -> new DropThrough(ctx, screen.getVisualBounds()))
            .toList();
        ctx.toFrontAll();
        return list;
    }

    private void handleDragOver(DragEvent e) {
        Dragboard db = e.getDragboard();
        TabContent dragged = ctx.dragElement();
        if (!db.hasContent(TabContent.TAB_MOVE_FORMAT) || dragged == null) return;
        e.acceptTransferModes(TransferMode.MOVE);
        e.consume();
    }

    private void handleDragDropped(DragEvent e) {
        Dragboard db = e.getDragboard();
        TabContent dragged = ctx.dragElement();
        if (!db.hasContent(TabContent.TAB_MOVE_FORMAT) || dragged == null) return;

        Stage newStage = new Stage();
        ctx.createScene(newStage, dragged.content(), dragged.content().getWidth(), dragged.content().getHeight());
        newStage.setX(e.getScreenX() - newStage.getWidth() / 2);
        newStage.setY(e.getScreenY() - newStage.getHeight() / 2);
        newStage.show();
        e.setDropCompleted(true);
        e.consume();
    }

    public void close() {
        stage.close();
    }

}
