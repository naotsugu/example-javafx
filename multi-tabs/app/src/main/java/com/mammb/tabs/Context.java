package com.mammb.tabs;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class Context {
    private final CopyOnWriteArrayList<TreeNode> roots = new CopyOnWriteArrayList<>();
    private final AtomicReference<TabContent> dragged = new AtomicReference<>();


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
