package com.mammb.code.jfx.canvas.example;

import com.mammb.code.jfx.canvas.Canvas;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import com.mammb.code.canvas.lib.lib_h;

public class App extends Application {

    private static final int WIDTH = 512;
    private static final int HEIGHT = 300;

    @Override
    public void start(Stage stage) {

        var canvas = new Canvas(WIDTH, HEIGHT);
        Scene scene = new Scene(canvas, WIDTH, HEIGHT, Color.TRANSPARENT);
        stage.setScene(scene);
        stage.show();

        var gc = canvas.getGraphicsContext();
        gc.setFill(Color.AQUA);
        gc.fillRect(50, 50, 100, 100);

        gc.setFill(Color.BLACK);
        gc.fillText("Hello", 100, 100);
        gc.render();

    }

    @Override
    public void stop() {

    }
}
