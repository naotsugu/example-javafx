package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HeaderBar;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class App extends Application {

    @Override
    public void start(Stage stage) {

        var textField = new TextField();
        textField.setPromptText("Search...");
        textField.setMaxWidth(300);
        HeaderBar.setMargin(textField, new Insets(5, 10, 5, 10));
        HeaderBar.setAlignment(textField, Pos.TOP_CENTER);

        var headerBar = new HeaderBar();
        headerBar.setCenter(textField);

        var root = new BorderPane();
        root.setTop(headerBar);
        var text = new Label("Hello, JavaFX");
        text.setFocusTraversable(true);
        root.setCenter(text);

        Scene scene = new Scene(root, 700, 300);
        stage.setScene(scene);
        stage.initStyle(StageStyle.EXTENDED);
        stage.show();
        text.requestFocus();
    }

    public static void main(String[] args) {
        launch();
    }
}
