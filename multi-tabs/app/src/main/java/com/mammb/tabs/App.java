package com.mammb.tabs;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        var context = new Context();
        context.createScene(stage, context.contentPaneSupplier().get(), 600, 400);
        stage.show();
    }

}
