package com.mammb.code.jfx.canvas;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.image.ImageView;

public class Canvas extends ImageView {

    private DoubleProperty width = new SimpleDoubleProperty(0);
    private DoubleProperty height = new SimpleDoubleProperty(0);

    private final GraphicsContext ctx;
    private final RenderPulse renderPulse;

    public Canvas(int width, int height) {
        ctx = new GraphicsContext(this, width, height);
        renderPulse = new RenderPulse(ctx::render);
        this.width.set(width);
        this.height.set(height);
        renderPulse.start();
    }

    public GraphicsContext getGraphicsContext() {
        return ctx;
    }

    RenderPulse getRenderPulse() {
        return renderPulse;
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
