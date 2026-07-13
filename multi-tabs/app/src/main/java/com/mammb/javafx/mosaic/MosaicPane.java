package com.mammb.javafx.mosaic;

import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MosaicPane extends StackPane {

    private final Context ctx;

    public MosaicPane(Stage stage) {
        this.ctx = new Context(stage);
        getChildren().add(new BranchNode(ctx, ctx.contentSupplier().apply("")));
    }

    public String asString() {
        return "";
    }

}
