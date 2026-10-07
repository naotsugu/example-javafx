package com.mammb.code.jfx.canvas;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.image.ImageView;

public class AltCanvas extends ImageView {

    private DoubleProperty width = new SimpleDoubleProperty(0);
    private DoubleProperty height = new SimpleDoubleProperty(0);

    private final AltGraphicsContext ctx;

    public AltCanvas(int width, int height) {
        ctx = new AltGraphicsContext(this, width, height);
        this.width.set(width);
        this.height.set(height);
    }

    public AltGraphicsContext getGraphicsContext() {
        return ctx;
    }

    public void setSize(int width, int height) {
        this.width.set(width);
        this.height.set(height);
        ctx.resize(width, height);
    }

    public final void setWidth(double value) {
        width.set(value);
    }

    public final double getWidth() {
        return width.get();
    }

    public final void setHeight(double value) {
        height.set(value);
    }

    public final double getHeight() {
        return height.get();
    }

}
