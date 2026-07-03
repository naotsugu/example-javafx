package com.mammb.tabs;

import java.util.concurrent.atomic.AtomicReference;

public class Context {
    private final AtomicReference<ContentTab> dragged = new AtomicReference<>();

    public void dragged(ContentTab contentTab) {
        dragged.set(contentTab);
    }

    public ContentTab dragged() {
        return dragged.get();
    }

    public void clear() {
        dragged.set(null);
    }

}
