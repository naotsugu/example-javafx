package com.mammb.code.jfx.canvas;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;

public class Canvas extends StackPane {

    private final GraphicsContext ctx;
    public Canvas(int width, int height) {
        ctx = new GraphicsContext(width, height);
        var imageView = ctx.getImageView();
        StackPane.setAlignment(imageView, Pos.TOP_LEFT);
        getChildren().add(imageView);
    }

    public GraphicsContext getGraphicsContext() {
        return ctx;
    }

}
