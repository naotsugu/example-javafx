package com.mammb.javafx.mosaic;

import javafx.geometry.Bounds;
import javafx.geometry.Side;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class DropMarker extends Rectangle {

    public DropMarker() {
        setFill(Color.TRANSPARENT);
        setStroke(Color.DARKORANGE);
        setStrokeWidth(1.5);
        setManaged(false);
        setVisible(false);
    }

    public void show(Bounds bounds) {
        show(bounds, null);
    }

    public void show(Bounds bounds, Side side) {
        setX(bounds.getMinX());
        setY(bounds.getMinY());
        setWidth(bounds.getWidth());
        setHeight(bounds.getHeight());
        switch (side) {
            case TOP    -> top();
            case RIGHT  -> right();
            case BOTTOM -> bottom();
            case LEFT   -> left();
            case null   -> {}
        }
        setVisible(true);
    }

    public void clear() {
        setVisible(false);
    }

    public void top() {
        setHeight(getHeight() / 2);
    }

    public void right() {
        setWidth(getWidth() / 2);
        setX(getX() + getWidth());
    }

    public void bottom() {
        setHeight(getHeight() / 2);
        setY(getY() + getHeight());
    }

    public void left() {
        setWidth(getWidth() / 2);
    }

}
