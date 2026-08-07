package com.mammb.code.jfx.multitab;

import javafx.geometry.Side;

public interface Container {

    void add(ContentPane contentPane);

    void add(Side side, ContentPane contentPane);

}
