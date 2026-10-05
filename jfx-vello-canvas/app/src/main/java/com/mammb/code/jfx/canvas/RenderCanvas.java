package com.mammb.code.jfx.canvas;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;

public class RenderCanvas extends StackPane {

    private final RenderContext ctx;

    public RenderCanvas(int width, int height) {
        ctx = new RenderContext(width, height);
        var imageView = ctx.getImageView();
        StackPane.setAlignment(imageView, Pos.TOP_LEFT);
        getChildren().add(imageView);
    }

    public RenderContext getGraphicsContext() {
        return ctx;
    }

}
