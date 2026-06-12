package com.mammb.ft;

import javafx.application.Application;
import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;

import java.nio.file.Paths;
import java.util.Objects;

@SuppressWarnings("restriction")
public class App extends Application {

    @Override
    public void start(Stage stage) {

        ContentArea contentArea = new ContentArea();
        PathTreeView treeView = new PathTreeView(
                Paths.get(System.getProperty("user.home")),
                Paths.get(System.getProperty("user.dir")));
        treeView.addSelectAction(contentArea::display);

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(treeView, contentArea);
        splitPane.setDividerPositions(0.3f);

        Scene scene = new Scene(splitPane, 800, 600);

        if (Platform.getPreferences().getColorScheme() == ColorScheme.DARK) {
            scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("dark-theme.css")).toExternalForm());
        }

        stage.setScene(scene);
        stage.setTitle("File Tree");
        stage.show();
    }
}
