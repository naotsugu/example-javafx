package com.mammb.javafx.mosaic;

import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;
import java.util.List;
import java.util.Objects;

public class BranchNode extends StackPane implements ChildOf<BranchNode>, ParentOf<ChildOf<BranchNode>> {

    private final SplitPane splitPane = new SplitPane();
    private final Context ctx;
    private BranchNode parent;

    BranchNode(Context ctx, BranchNode parent) {
        this.ctx = Objects.requireNonNull(ctx);
        this.parent = parent;
    }

    BranchNode(Context ctx, ContentPane content) {
        this(ctx, (BranchNode) null);
        splitPane.getItems().add(new LeafNode(ctx, this, content));
    }

    @Override
    public BranchNode parent() {
        return parent;
    }

    @Override
    public void parent(BranchNode parent) {
        this.parent = parent;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ChildOf<BranchNode>> children() {
        return splitPane.getItems().stream()
            .filter(ChildOf.class::isInstance)
            .map(item -> (ChildOf<BranchNode>) item)
            .toList();
    }


    @Override
    public void addChildren(List<ChildOf<BranchNode>> children) {
        for (ChildOf<BranchNode> child : children) {
            child.parent(this);
            if (child instanceof Node node) {
                splitPane.getItems().add(node);
            }
        }
    }

}
