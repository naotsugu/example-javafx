package com.mammb.javafx.pane.tabblock.internal;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

class TabXButton extends javafx.scene.control.Tab {

    private final Label label;

    public TabXButton() {
        label = new Label();
        label.setGraphic(plus());
        var style = """
            -fx-padding: 3 5 3 5;
            -fx-background-radius: 15px;
            -fx-border-radius: 15px;
            """;
        label.setStyle(style);
        label.setOnMouseEntered(_ -> label.setStyle(style + " -fx-background-color: rgba(128, 128, 128, 0.5);"));
        label.setOnMouseExited(_ -> label.setStyle(style + " -fx-background-color: transparent;"));
        setGraphic(label);
        setClosable(false);
        getStyleClass().add("add-tab-button");
        setStyle("""
            -fx-background-color: transparent;
            """);
    }

    public final void setOnMouseClicked(EventHandler<? super MouseEvent> event) {
        label.setOnMouseClicked(event);
    }

    /**
     * Get the plus icon.
     * @return the plus icon
     */
    public static SVGPath plus() {
        var svg = new SVGPath();
        svg.setContent("""
            M8 4a.5.5 0 0 1 .5.5v3h3a.5.5 0 0 1 0 1h-3v3a.5.5 0 0 1-1 0v-3h-3a.5.5 0 0 1 0-1h3v-3A.5.5 0 0 1 8 4
            """);
        svg.getStyleClass().add("icon");
        svg.setStroke(Color.GRAY);
        return svg;
    }

}
