package com.mammb.javafx.mosaic;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {

        var pane = new MosaicPane(stage);
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.setWidth(600);
        stage.setHeight(400);
        stage.show();

        stage.setOnCloseRequest(_ -> System.out.println(pane.asString()));

    }
}
