package com.mammb.code.jfx.canvas;

import com.mammb.code.canvas.lib.Point;
import com.mammb.code.canvas.lib.lib_h;
import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.ref.Cleaner;
import java.lang.ref.Reference;
import java.nio.ByteBuffer;

public class RenderContext implements AutoCloseable {

    private final NativeGlobal nativeGlobal = NativeGlobal.instance();
    private final Cleaner.Cleanable cleanable;

    private final Arena arena = Arena.ofShared();
    private final MemorySegment ctxSegment;
    private final MemorySegment sceneSegment;
    private final RenderCanvas theCanvas;
    private volatile boolean closed = false;
    private int viewWidth;
    private int sceneWidth;
    private int sceneHeight;
    private PixelBuffer<ByteBuffer> pixelBuffer;
    private ImageView imageView;


    RenderContext(RenderCanvas canvas, int width, int height) {

        theCanvas = canvas;
        viewWidth = width;
        sceneWidth = alignWidth(width);
        sceneHeight = height;

        ctxSegment = lib_h.create_render_context(sceneWidth, sceneHeight);
        cleanable = nativeGlobal.cleaner(this, ctxSegment, arena);
        sceneSegment = arena.allocate((long) sceneWidth * sceneHeight * 4); // 4bytes[BGRA]
        pixelBuffer = new PixelBuffer<>(
                sceneWidth, sceneHeight,
                sceneSegment.asByteBuffer(),
                PixelFormat.getByteBgraPreInstance());
        imageView = new ImageView(new WritableImage(pixelBuffer));
    }

    ImageView getImageView() {
        return imageView;
    }

    public RenderCanvas getCanvas() {
        return theCanvas;
    }

    public Font getFont() {
        return null; //this.curState.font;
    }
    public void setFontSmoothingType(FontSmoothingType var1) {
        // TODO
    }

    public void render() {
        if (!closed) {
            try {
                lib_h.render(ctxSegment, sceneSegment);
                pixelBuffer.updateBuffer(_ -> null);
            } finally {
                Reference.reachabilityFence(this);
            }
        }
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            cleanable.clean();
        }
    }

    public void clearRect(double x, double y, double w, double h) {
        if (w != 0 && h != 0) {
            // TODO
        }
    }
    public void setFill(Paint p) {
        if (p != null && !closed) {
            if (p instanceof Color c) {
                lib_h.set_fill(ctxSegment,
                        b(c.getRed()), b(c.getGreen()), b(c.getBlue()), b(c.getOpacity()));
            }
        }
    }

    public void setStroke(Paint p) {
        if (p != null && !closed) {
            if (p instanceof Color c) {
                lib_h.set_stroke(ctxSegment,
                        b(c.getRed()), b(c.getGreen()), b(c.getBlue()), b(c.getOpacity()));
            }
        }
    }
    public void setLineWidth(double lw) {
        if (lw > 0 && lw < Double.POSITIVE_INFINITY && !closed) {
            lib_h.set_line_width(ctxSegment, lw);
        }
    }
    public void setLineCap(StrokeLineCap cap) {
        if (cap != null && !closed) {
            lib_h.set_line_cap(ctxSegment, switch (cap) {
                case SQUARE -> 2; case ROUND -> 1; case BUTT -> 0;
            });
        }
    }
    public void setLineJoin(StrokeLineJoin join) {
        // TODO
    }
    public void setFont(Font f) {
        if (f != null  && !closed) {
            try (var localArena = Arena.ofConfined()) {
                lib_h.set_font_family(ctxSegment, localArena.allocateFrom(f.getFamily()));
            }
            lib_h.set_font_size(ctxSegment, f.getSize());
        }
    }

    // -- draw ----------------------------------------------------------------

    public void fillRect(double x, double y, double w, double h) {
        if (w != 0 && h != 0 && !closed) {
            lib_h.fill_rect(ctxSegment, x, y, w, h);
        }
    }
    public void strokeRect(double x, double y, double w, double h) {
        if (w != 0 || h != 0 && !closed) {
            lib_h.stroke_rect(ctxSegment, x, y, w, h);
        }
    }
    public void fillRoundRect(double x, double y, double w, double h, double radius) {
        if (w != 0 && h != 0 && !closed) {
            lib_h.fill_round_rect(ctxSegment, x, y, w, h, radius);
        }
    }
    public void strokeRoundRect(double x, double y, double w, double h, double radius) {
        if (w != 0 && h != 0 && !closed) {
            lib_h.stroke_round_rect(ctxSegment, x, y, w, h, radius);
        }
    }
    public void fillOval(double x, double y, double w, double h) {
        if (w != 0 || h != 0 && !closed) {
            lib_h.fill_oval(ctxSegment, x, y, w, h);
        }
    }
    public void strokeOval(double x, double y, double w, double h) {
        if (w != 0 || h != 0 && !closed) {
            lib_h.stroke_oval(ctxSegment, x, y, w, h);
        }
    }
    public void strokeLine(double x1, double y1, double x2, double y2) {
        if (!closed) {
            lib_h.stroke_oval(ctxSegment, x1, y1, x2, y2);
        }
    }
    public void fillPolygon(double[] xPoints, double[] yPoints, int nPoints) {
        if (nPoints >= 3 && !closed) {
            try (var localArena = Arena.ofConfined()) {
                lib_h.fill_polygon(ctxSegment,
                        localArena.allocateFrom(ValueLayout.JAVA_DOUBLE, xPoints),
                        localArena.allocateFrom(ValueLayout.JAVA_DOUBLE, yPoints),
                        nPoints);
            }
        }
    }
    public void strokePolygon(double[] xPoints, double[] yPoints, int nPoints) {
        if (nPoints >= 2 && !closed) {
            try (var localArena = Arena.ofConfined()) {
                lib_h.stroke_polygon(ctxSegment,
                        localArena.allocateFrom(ValueLayout.JAVA_DOUBLE, xPoints),
                        localArena.allocateFrom(ValueLayout.JAVA_DOUBLE, yPoints),
                        nPoints);
            }
        }
    }
    public void strokePolyline(double[] xPoints, double[] yPoints, int nPoints) {
        if (nPoints >= 2 && !closed) {
            try (var localArena = Arena.ofConfined()) {
                lib_h.stroke_polyline(ctxSegment,
                        localArena.allocateFrom(ValueLayout.JAVA_DOUBLE, xPoints),
                        localArena.allocateFrom(ValueLayout.JAVA_DOUBLE, yPoints),
                        nPoints);
            }
        }
    }

    public Point2D fillText(String text, double x, double y) {
        if (text == null || text.isEmpty() || closed) return new Point2D(x, y);
        try (var localArena = Arena.ofConfined()) {
            MemorySegment end = lib_h.fill_text(localArena, ctxSegment, localArena.allocateFrom(text), x, y);
            return new Point2D(Point.x(end), Point.y(end));
        }
    }

    // -- helper --------------------------------------------------------------

    private byte b(double v) {
        int val = (int) Math.round(v * 255.0);
        return (byte) val;
    }

    /**
     * Rounds the width up to the next multiple of 64 pixels (= 256 bytes per row).
     * e.g. 1000 -> 1024, 1024 -> 1024.
     */
    public static int alignWidth(int width) {
        int widthAlignment = 256 / 4;
        return (width + widthAlignment - 1) & -widthAlignment;
    }
}
