package com.mammb.javafx.mosaic;

public interface ChildOf<E> {

    E parent();

    default boolean isRoot() {
        return parent() == null;
    }

}
