package com.mammb.tabs;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        TreeNode treeNode = TreeNode.rootOf(new Label("label1"));
        Scene scene = new Scene(treeNode, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Tabs");
        stage.show();
    }

}
