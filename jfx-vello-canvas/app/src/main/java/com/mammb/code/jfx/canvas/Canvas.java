package com.mammb.code.jfx.canvas;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;

public class Canvas extends StackPane {

    private final GraphicsContext ctx;
    public Canvas() {
        ctx = new GraphicsContext(300, 600);
        var imageView = ctx.getImageView();
        StackPane.setAlignment(imageView, Pos.TOP_LEFT);
        getChildren().add(imageView);
    }

}
