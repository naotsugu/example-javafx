package com.mammb.javafx.mosaic;

import javafx.scene.layout.StackPane;
import java.util.List;
import java.util.Objects;

public class BranchNode extends StackPane implements ChildOf<BranchNode>, ParentOf<ChildOf<BranchNode>> {

    private final Context ctx;
    private BranchNode parent;

    public BranchNode(Context ctx, BranchNode parent) {
        this.ctx = Objects.requireNonNull(ctx);
        this.parent = parent;
    }

    @Override
    public BranchNode parent() {
        return parent;
    }

    @Override
    public List<ChildOf<BranchNode>> children() {
        return List.of();
    }
}
