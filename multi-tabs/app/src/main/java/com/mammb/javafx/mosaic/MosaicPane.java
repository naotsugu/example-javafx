package com.mammb.javafx.mosaic;

import javafx.geometry.Orientation;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MosaicPane extends StackPane {

    private final Context ctx;

    public MosaicPane(Stage stage) {
        this.ctx = new Context(stage);
        getChildren().add(new BranchNode(ctx, ctx.contentSupplier().apply("")));
    }

    public MosaicPane(Stage stage,
            Function<String, ? extends ContentPane> contentSupplier,
            Function<Path, ? extends ContentPane> pathContentSupplier) {
        this.ctx = new Context(stage);
        if  (contentSupplier != null)
            ctx.contentSupplier(contentSupplier);
        if (pathContentSupplier != null)
            ctx.pathContentSupplier(pathContentSupplier);
        getChildren().add(new BranchNode(ctx, ctx.contentSupplier().apply("")));
    }


    public String asString() {
        return asStringRecursive((BranchNode) getChildren().getFirst());
    }

    private String asStringRecursive(ParentOf<?> parentOf) {
        return switch (parentOf) {
            case BranchNode branchNode -> "{" +
                branchNode.orientation().toString().charAt(0) + "," +
                Arrays.stream(branchNode.dividerPositions())
                    .mapToObj(String::valueOf).findFirst().orElse("") + "," +
                branchNode.children().stream()
                    .filter(ParentOf.class::isInstance)
                    .map(e -> (ParentOf<?>) e)
                    .map(this::asStringRecursive)
                    .collect(Collectors.joining(",")) + "}";
            case LeafNode leafNode -> leafNode.children().stream()
                .map(Tab::content)
                .map(ContentPane::asString)
                .map(e -> "\"" + e + "\"")
                .collect(Collectors.joining(",", "[", "]"));
            default -> "";
        };
    }

    public MosaicPane fromString(String string) {
        return null;
    }

    public Object fromStringRecursive(String string) {
        return null;
    }

}
