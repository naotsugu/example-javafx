package com.mammb.code.jfx.canvas.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import com.mammb.code.canvas.lib.lib_h;

public class App extends Application {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private Arena arena;
    private MemorySegment ctxPtr;

    @Override
    public void start(Stage stage) {

        arena = Arena.ofShared();

        ctxPtr = lib_h.create_ctx(WIDTH, HEIGHT);
        if (ctxPtr.equals(MemorySegment.NULL)) {
            throw new RuntimeException("Failed to initialize context");
        }

        long bufferSize = (long) WIDTH * HEIGHT * 4; // 4bytes[BGRA]
        MemorySegment segment = arena.allocate(bufferSize);

        ByteBuffer byteBuffer = segment.asByteBuffer();
        PixelFormat<ByteBuffer> format = PixelFormat.getByteBgraPreInstance();
        PixelBuffer<ByteBuffer> pixelBuffer = new PixelBuffer<>(WIDTH, HEIGHT, byteBuffer, format);
        WritableImage image = new WritableImage(pixelBuffer);

        ImageView imageView = new ImageView(image);
        StackPane root = new StackPane(imageView);
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        stage.setScene(scene);
        stage.show();

        lib_h.render(ctxPtr, segment);
        pixelBuffer.updateBuffer(_ -> null);

    }

    @Override
    public void stop() {
        if (ctxPtr != null && !ctxPtr.equals(MemorySegment.NULL)) {
            lib_h.destroy_ctx(ctxPtr);
        }
        if (arena != null) arena.close();
    }
}
