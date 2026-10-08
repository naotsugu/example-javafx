package com.mammb.code.jfx.canvas;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;

class ContextState {

    Paint fill;
    Paint stroke;
    double linewidth;
    StrokeLineCap linecap;
    StrokeLineJoin linejoin;
    Font font;

    ContextState() {
        init();
    }

    final void init() {
        set(Color.BLACK,
            Color.BLACK,
            1.0,
            StrokeLineCap.SQUARE,
            StrokeLineJoin.MITER,
            Font.getDefault());
    }

    ContextState(ContextState copy) {
        set(copy.fill,
            copy.stroke,
            copy.linewidth,
            copy.linecap,
            copy.linejoin,
            copy.font);
    }

    final void set(
            Paint fill,
            Paint stroke,
            double linewidth,
            StrokeLineCap linecap,
            StrokeLineJoin linejoin,
            Font font) {
        this.fill = fill;
        this.stroke = stroke;
        this.linewidth = linewidth;
        this.linecap = linecap;
        this.linejoin = linejoin;
        this.font = font;
    }

    ContextState copy() {
        return new ContextState(this);
    }

    void restore(GraphicsContext ctx) {
        ctx.setFill(fill);
        ctx.setStroke(stroke);
        ctx.setLineWidth(linewidth);
        ctx.setLineCap(linecap);
        ctx.setLineJoin(linejoin);
        ctx.setFont(font);
    }

}
