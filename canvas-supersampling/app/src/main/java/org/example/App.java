package org.example;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {

        double scale = 3.0;

        double fontSize = 16;
        double width = 300;
        double height = 150;

        Canvas canvas1 = new Canvas(width, height);
        GraphicsContext gc1 = canvas1.getGraphicsContext2D();
        gc1.setFontSmoothingType(FontSmoothingType.LCD);
        gc1.setFont(Font.font("Menlo", fontSize));
        gc1.setFill(Color.WHITE);
        gc1.fillText("【通常品質】JavaFXの日本語表示", 20, 30);

        Canvas canvas2 = new Canvas(width * 2, height * 2);
        canvas2.setStyle("-fx-width: " + width + "px; -fx-height: " + height + "px;");
        GraphicsContext gc2 = canvas2.getGraphicsContext2D();
        gc2.setFontSmoothingType(FontSmoothingType.LCD);
        gc2.scale(1.0 / scale, 1.0 / scale);
        gc2.setFont(Font.font("Menlo", fontSize * scale));
        gc2.setFill(Color.WHITE);
        gc2.fillText("【高品質】JavaFXの日本語表示", 20 * scale, 30 * scale);

        HBox box = new HBox();
        box.setAlignment(Pos.TOP_LEFT);
        box.setStyle("-fx-background-color: black");
        box.getChildren().addAll(canvas1, canvas2);
        Scene scene = new Scene(box, width * 2, height);
        stage.setTitle("Canvas Text Quality");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
