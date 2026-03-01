package com.mammb.code.gclog;

import atlantafx.base.theme.NordDark;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {

        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());

        var borderPane = new BorderPane();
        borderPane.setCenter(new ChatPane());

        var scene  = new Scene(borderPane, 800, 600);
        stage.setScene(scene);
        stage.setTitle("GC log chart");
        stage.show();
    }

}
