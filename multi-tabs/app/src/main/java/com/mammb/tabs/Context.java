package com.mammb.tabs;

import java.util.concurrent.atomic.AtomicReference;

public class Context {
    private final AtomicReference<TabContent> dragged = new AtomicReference<>();

    public void dragged(TabContent tabContent) {
        dragged.set(tabContent);
    }

    public TabContent dragged() {
        return dragged.get();
    }

    public void clear() {
        dragged.set(null);
    }

}
