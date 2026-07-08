package com.mammb.tabs;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        var context = new Context();
        context.createScene(stage, new ContentPane(new Label("initial"), "initial"));
        stage.setWidth(600);
        stage.setHeight(400);
        stage.show();
    }

}
