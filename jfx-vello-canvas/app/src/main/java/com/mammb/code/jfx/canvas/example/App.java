package com.mammb.code.jfx.canvas.example;

import com.mammb.code.jfx.canvas.AltCanvas;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;


public class App extends Application {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;

    @Override
    public void start(Stage stage) {

        var renderCanvas = new AltCanvas(WIDTH, HEIGHT);
        var canvas = new Canvas(WIDTH, HEIGHT);

        HBox hbox = new HBox();
        hbox.getChildren().addAll(canvas, renderCanvas);
        Scene scene = new Scene(hbox, WIDTH * 2, HEIGHT, Color.TRANSPARENT);
        stage.setScene(scene);
        stage.show();

        {
            var gc = canvas.getGraphicsContext2D();
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, WIDTH, HEIGHT);

            gc.setFill(Color.DARKBLUE);
            gc.fillRect(50, 50, 100, 100);

            gc.setFont(Font.font(14));
            gc.setFill(Color.WHITE);
            gc.fillText("Hello JavaFX Canvas フォント描画品質", 100, 100);
        }

        {
            var gc = renderCanvas.getGraphicsContext();
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, WIDTH, HEIGHT);

            gc.setFill(Color.DARKBLUE);
            gc.fillRect(50, 50, 100, 100);

            gc.setFont(Font.font(14));
            gc.setFill(Color.WHITE);
            gc.fillText("Hello Vello Canvas フォント描画品質", 100, 100);
            gc.render();
        }

    }

    @Override
    public void stop() {
    }
}
