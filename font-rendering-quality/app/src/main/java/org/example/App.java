package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        var vbox = new VBox(canvas1(), canvas2(), stackPaneText(), stackPaneLabel(), canvasRecoat(), canvasRecoatGray());
        vbox.setStyle("-fx-background-color: #000;");
        vbox.setSnapToPixel(true);
        var scene = new Scene(vbox, 600, 200);//, true, SceneAntialiasing.DISABLED);
        scene.setFill(Color.TRANSPARENT);
//        scene.getStylesheets().add(String.join(",",
//                "data:text/css;base64",
//                Base64.getEncoder().encodeToString("""
//                        .root {
//                            -fx-font-family: "Hiragino Kaku Gothic ProN", "sans-serif";
//                        }
//                        """.getBytes(StandardCharsets.UTF_8))));
        stage.setScene(scene);
        stage.setTitle("font-rendering-quality");
        stage.show();
    }

    private Canvas canvas1() {
        var canvas = new Canvas(600, 25);
        var gc = canvas.getGraphicsContext2D();
        gc.setFontSmoothingType(FontSmoothingType.LCD);
        gc.setFill(Color.WHITE);
        gc.fillText("Font Rendering Quality : フォントレンダリング品質(Canvas LCD)", 13, 20);
        return canvas;
    }

    private Canvas canvas2() {
        var canvas = new Canvas(600, 25);
        var gc = canvas.getGraphicsContext2D();
        gc.setFontSmoothingType(FontSmoothingType.GRAY);
        gc.setFill(Color.WHITE);
        gc.fillText("Font Rendering Quality : フォントレンダリング品質(Canvas GRAY)", 13, 20);
        return canvas;
    }

    private Pane stackPaneText() {
        var pane = new StackPane();
        Text text = new Text("Font Rendering Quality : フォントレンダリング品質(StackPane + Text)");
        text.setFill(Color.WHITE);
        StackPane.setAlignment(text, Pos.CENTER_LEFT);
        StackPane.setMargin(text, new Insets(10, 0, 0, 13));
        pane.getChildren().add(text);
        return pane;
    }

    private Pane stackPaneLabel() {
        var pane = new StackPane();
        Label label = new Label("Font Rendering Quality : フォントレンダリング品質(StackPane + Label)");
        label.setTextFill(Color.WHITE);
        label.setPadding(new Insets(10, 0, 0, 13));
        StackPane.setAlignment(label, Pos.CENTER_LEFT);
        pane.getChildren().add(label);
        return pane;
    }

    private Canvas canvasRecoat() {
        var canvas = new Canvas(600, 25);
        var gc = canvas.getGraphicsContext2D();
        gc.setFontSmoothingType(FontSmoothingType.LCD);
        gc.setFill(Color.WHITE);
        gc.fillText("Font Rendering Quality : フォントレンダリング品質(Canvas LCD 重ね塗り)", 13, 20);
        gc.fillText("Font Rendering Quality : フォントレンダリング品質(Canvas LCD 重ね塗り)", 13, 20);
        return canvas;
    }

    private Canvas canvasRecoatGray() {
        var canvas = new Canvas(600, 25);
        var gc = canvas.getGraphicsContext2D();
        gc.setFontSmoothingType(FontSmoothingType.GRAY);
        gc.setFill(Color.WHITE);
        gc.fillText("Font Rendering Quality : フォントレンダリング品質(Canvas GRAY 重ね塗り)", 13, 20);
        gc.fillText("Font Rendering Quality : フォントレンダリング品質(Canvas GRAY 重ね塗り)", 13, 20);
        return canvas;
    }

}
