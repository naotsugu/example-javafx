package com.mammb.code.jfx.canvas.example;

import com.mammb.code.jfx.canvas.Canvas;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class App extends Application {

    private static final int WIDTH = 512;
    private static final int HEIGHT = 300;

    private Canvas canvas;

    @Override
    public void start(Stage stage) {

        canvas = new Canvas(WIDTH, HEIGHT);
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
        var c = canvas;
        if (c != null) {
            c.getGraphicsContext().close();
        }
    }
}
