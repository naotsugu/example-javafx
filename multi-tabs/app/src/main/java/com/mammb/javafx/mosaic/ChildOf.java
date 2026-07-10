package com.mammb.javafx.mosaic;

import javafx.scene.Node;

public interface ChildOf<P extends Node> {

    P parent();
    void parent(P parent);

    default boolean isRoot() {
        return parent() == null;
    }

}
