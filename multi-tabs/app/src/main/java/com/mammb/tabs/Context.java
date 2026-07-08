package com.mammb.tabs;

import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class Context {

    private final CopyOnWriteArrayList<TreeNode> roots = new CopyOnWriteArrayList<>();
    private final AtomicReference<TabContent> dragged = new AtomicReference<>();

    public Scene createScene(Stage stage, ContentPane contentPane) {
        TreeNode treeNode = TreeNode.rootOf(this, contentPane);
        Scene scene = new Scene(treeNode);
        stage.setTitle("Tabs");
        stage.setScene(scene);

        roots.add(treeNode);
        stage.setOnCloseRequest(e -> roots.remove(treeNode));
        return scene;
    }

    public void drag(TabContent tabContent) {
        dragged.set(tabContent);
    }

    public TabContent drag() {
        return dragged.get();
    }

    public void dragDone() {
        dragged.set(null);
    }

}
