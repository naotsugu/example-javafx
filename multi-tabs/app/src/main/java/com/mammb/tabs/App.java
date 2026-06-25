package com.mammb.tabs;

import javafx.application.Application;
import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;

import java.nio.file.Paths;
import java.util.Objects;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        //TilePane tilePane = new TilePane(new Label("label1"));
        Scene scene = new Scene(new Label("label1"), 800, 600);
        stage.setScene(scene);
        stage.setTitle("Tabs");
        stage.show();
    }
}
