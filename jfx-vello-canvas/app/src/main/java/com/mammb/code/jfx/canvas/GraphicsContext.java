package com.mammb.code.jfx.canvas;

import com.mammb.code.canvas.lib.lib_h;
import com.sun.javafx.geom.transform.Affine2D;
import javafx.geometry.VPos;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.Effect;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.TextAlignment;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;
import java.util.LinkedList;

public class GraphicsContext implements AutoCloseable {

    private final NativeGlobal nativeGlobal = NativeGlobal.instance();
    private final Cleaner.Cleanable cleanable;

    private final Arena arena = Arena.ofShared();
    private final MemorySegment ctxSegment;
    private final MemorySegment sceneSegment;

    private int sceneWidth;
    private int sceneHeight;
    private PixelBuffer<ByteBuffer> pixelBuffer;
    private ImageView imageView;
    private State curState;
    private LinkedList<State> stateStack;


    GraphicsContext(int width, int height) {

        sceneWidth = width;
        sceneHeight = height;

        ctxSegment = lib_h.create_render_context(sceneWidth, sceneHeight);
        cleanable = nativeGlobal.cleaner(this, ctxSegment, arena);
        sceneSegment = arena.allocate((long) sceneWidth * sceneHeight * 4); // 4bytes[BGRA]
        pixelBuffer = new PixelBuffer<>(
                sceneWidth, sceneHeight,
                sceneSegment.asByteBuffer(),
                PixelFormat.getByteBgraPreInstance());
        imageView = new ImageView(new WritableImage(pixelBuffer));
        this.curState = new State();
    }

    ImageView getImageView() {
        return imageView;
    }

    public void render() {
        lib_h.render(ctxSegment, sceneSegment);
        pixelBuffer.updateBuffer(_ -> null);
    }

    @Override
    public void close() {
        cleanable.clean();
    }

    public void setFill(Paint p) {
        if (p != null && curState.fill != p) {
            curState.fill = p;
            if (p instanceof Color c) {
                lib_h.set_fill(ctxSegment,
                        b(c.getRed()), b(c.getGreen()), b(c.getBlue()), b(c.getOpacity()));
            }
        }
    }

    public void setStroke(Paint p) {
        if (p != null && curState.stroke != p) {
            curState.stroke = p;
            if (p instanceof Color c) {
                lib_h.set_stroke(ctxSegment,
                        b(c.getRed()), b(c.getGreen()), b(c.getBlue()), b(c.getOpacity()));
            }
        }
    }

    public void fillRect(double x, double y, double w, double h) {
        if (w != 0 && h != 0) {
            lib_h.fill_rect(ctxSegment, x, y, w, h);
        }
    }
    public void strokeRect(double x, double y, double w, double h) {
        if (w != 0 || h != 0) {
            lib_h.stroke_rect(ctxSegment, x, y, w, h);
        }
    }
    public void fillRoundRect(double x, double y, double w, double h, double radius) {
        if (w != 0 && h != 0) {
            lib_h.fill_round_rect(ctxSegment, x, y, w, h, radius);
        }
    }
    public void strokeRoundRect(double x, double y, double w, double h, double radius) {
        if (w != 0 && h != 0) {
            lib_h.stroke_round_rect(ctxSegment, x, y, w, h, radius);
        }
    }
    public void fillText(String text, double x, double y) {
        lib_h.fill_text(ctxSegment, arena.allocateFrom(text), x, y);
    }

    // ------------------------------------------------------------------------

    private byte b(double v) {
        int val = (int) Math.round(v * 255.0);
        return (byte) val;
    }

    static class State {
        double globalAlpha;
        BlendMode blendop;
        Affine2D transform;
        Paint fill;
        Paint stroke;
        double linewidth;
        StrokeLineCap linecap;
        StrokeLineJoin linejoin;
        double miterlimit;
        double dashes[];
        double dashOffset;
        int numClipPaths;
        Font font;
        FontSmoothingType fontsmoothing;
        TextAlignment textalign;
        VPos textbaseline;
        Effect effect;
        FillRule fillRule;
        boolean imageSmoothing = true;

        State() {
            init();
        }

        final void init() {
            set(1.0, BlendMode.SRC_OVER,
                    null, //new Affine2D(),
                    Color.BLACK, Color.BLACK,
                    1.0, StrokeLineCap.SQUARE, StrokeLineJoin.MITER, 10.0,
                    null, 0.0,
                    0,
                    Font.getDefault(), FontSmoothingType.GRAY,
                    TextAlignment.LEFT, VPos.BASELINE,
                    null, FillRule.NON_ZERO, true);
        }

        State(GraphicsContext.State copy) {
            set(copy.globalAlpha, copy.blendop,
                    new Affine2D(copy.transform),
                    copy.fill, copy.stroke,
                    copy.linewidth, copy.linecap, copy.linejoin, copy.miterlimit,
                    copy.dashes, copy.dashOffset,
                    copy.numClipPaths,
                    copy.font, copy.fontsmoothing, copy.textalign, copy.textbaseline,
                    copy.effect, copy.fillRule, copy.imageSmoothing);
        }

        final void set(double globalAlpha, BlendMode blendop,
                       Affine2D transform, Paint fill, Paint stroke,
                       double linewidth, StrokeLineCap linecap,
                       StrokeLineJoin linejoin, double miterlimit,
                       double dashes[], double dashOffset,
                       int numClipPaths,
                       Font font, FontSmoothingType smoothing,
                       TextAlignment align, VPos baseline,
                       Effect effect, FillRule fillRule, boolean imageSmoothing)
        {
            this.globalAlpha = globalAlpha;
            this.blendop = blendop;
            this.transform = transform;
            this.fill = fill;
            this.stroke = stroke;
            this.linewidth = linewidth;
            this.linecap = linecap;
            this.linejoin = linejoin;
            this.miterlimit = miterlimit;
            this.dashes = dashes;
            this.dashOffset = dashOffset;
            this.numClipPaths = numClipPaths;
            this.font = font;
            this.fontsmoothing = smoothing;
            this.textalign = align;
            this.textbaseline = baseline;
            this.effect = effect;
            this.fillRule = fillRule;
            this.imageSmoothing = imageSmoothing;
        }

        GraphicsContext.State copy() {
            return new GraphicsContext.State(this);
        }

        void restore(GraphicsContext ctx) {
//            ctx.setGlobalAlpha(globalAlpha);
//            ctx.setGlobalBlendMode(blendop);
//            ctx.setTransform(transform.getMxx(), transform.getMyx(),
//                    transform.getMxy(), transform.getMyy(),
//                    transform.getMxt(), transform.getMyt());
//            ctx.setFill(fill);
//            ctx.setStroke(stroke);
//            ctx.setLineWidth(linewidth);
//            ctx.setLineCap(linecap);
//            ctx.setLineJoin(linejoin);
//            ctx.setMiterLimit(miterlimit);
//            ctx.setLineDashes(dashes);
//            ctx.setLineDashOffset(dashOffset);
//            GrowableDataBuffer buf = ctx.getBuffer();
//            while (ctx.curState.numClipPaths > numClipPaths) {
//                ctx.curState.numClipPaths--;
//                ctx.clipStack.removeLast();
//                buf.putByte(NGCanvas.POP_CLIP);
//            }
//            ctx.setFillRule(fillRule);
//            ctx.setFont(font);
//            ctx.setFontSmoothingType(fontsmoothing);
//            ctx.setTextAlign(textalign);
//            ctx.setTextBaseline(textbaseline);
//            ctx.setEffect(effect);
//            ctx.setImageSmoothing(imageSmoothing);
        }
    }

}
