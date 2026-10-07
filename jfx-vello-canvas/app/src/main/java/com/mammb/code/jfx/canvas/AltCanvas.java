package com.mammb.code.jfx.canvas;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;

public class AltCanvas extends StackPane {

    private final AltGraphicsContext ctx;

    public AltCanvas(int width, int height) {
        ctx = new AltGraphicsContext(this, width, height);
        var imageView = ctx.getImageView();
        StackPane.setAlignment(imageView, Pos.TOP_LEFT);
        getChildren().add(imageView);
    }

    public AltGraphicsContext getGraphicsContext() {
        return ctx;
    }

}
