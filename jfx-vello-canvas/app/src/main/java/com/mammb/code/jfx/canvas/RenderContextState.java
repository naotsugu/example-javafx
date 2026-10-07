package com.mammb.code.jfx.canvas;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;

class RenderContextState {

    Paint fill;
    Paint stroke;
    double linewidth;
    StrokeLineCap linecap;
    StrokeLineJoin linejoin;
    Font font;

    RenderContextState() {
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

    RenderContextState(RenderContextState copy) {
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

    RenderContextState copy() {
        return new RenderContextState(this);
    }

    void restore(RenderContext ctx) {
        ctx.setFill(fill);
        ctx.setStroke(stroke);
        ctx.setLineWidth(linewidth);
        ctx.setLineCap(linecap);
        ctx.setLineJoin(linejoin);
        ctx.setFont(font);
    }

}
