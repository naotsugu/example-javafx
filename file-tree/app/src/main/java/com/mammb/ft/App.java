package com.mammb.ft;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {

        FileTreeView treeView = new FileTreeView();
        ContentArea contentArea = new ContentArea();

        treeView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    contentArea.display(newValue.getValue());
                }
            });

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(treeView, contentArea);
        splitPane.setDividerPositions(0.3f);

        Scene scene = new Scene(splitPane, 800, 600);
        stage.setScene(scene);
        stage.setTitle("File Tree");
        stage.show();
    }
}
