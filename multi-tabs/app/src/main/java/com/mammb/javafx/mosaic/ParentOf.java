package com.mammb.javafx.mosaic;

import java.util.List;

public interface ParentOf<C> {

    List<C> children();
    void addChildren(List<C> children);

}
