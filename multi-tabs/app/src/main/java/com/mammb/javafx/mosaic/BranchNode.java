package com.mammb.javafx.mosaic;

import com.mammb.tabs.LeafPane;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.SplitPane;
import java.util.List;
import java.util.Objects;

public class BranchNode extends TreeNode implements ParentOf<TreeNode> {

    private final SplitPane splitPane = new SplitPane();
    private final Context ctx;
    private BranchNode parent;

    BranchNode(Context ctx, BranchNode parent) {
        this.ctx = Objects.requireNonNull(ctx);
        this.parent = parent;
        getChildren().add(splitPane);
    }

    BranchNode(Context ctx, ContentPane content) {
        this(ctx, (BranchNode) null);
        splitPane.getItems().add(new LeafNode(ctx, this, content));
    }

    public void add(ContentPane content, LeafNode source, Side side) {

        int sourceIndex = children().indexOf(source);
        if (sourceIndex < 0) throw new IllegalArgumentException("No such node in content");

        Orientation orientation = switch (side) {
            case TOP, BOTTOM -> Orientation.VERTICAL;
            default ->  Orientation.HORIZONTAL;
        };

        if (children().size() <= 1) {
            // add new leaf pane
            int insIndex = (side == Side.RIGHT || side == Side.BOTTOM)
                ? sourceIndex + 1
                : sourceIndex;
            addChild(insIndex, new LeafNode(ctx, this, content));
            splitPane.setOrientation(orientation);
        } else {
            removeChild(source);
            BranchNode newChild = new BranchNode(ctx, this);
            newChild.orientation(orientation);
            newChild.addChildren(List.of(source));
            int insIndex = (side == Side.RIGHT || side == Side.BOTTOM) ? 1 : 0;
            newChild.addChild(insIndex, new LeafNode(ctx, newChild, content));
            addChild(sourceIndex, newChild);
        }
    }

    private void orientation(Orientation value) {
        splitPane.setOrientation(value);
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
    public List<TreeNode> children() {
        return splitPane.getItems().stream()
            .filter(TreeNode.class::isInstance)
            .map(TreeNode.class::cast)
            .toList();
        }

    @Override
    public void addChildren(List<TreeNode> children) {
        for (TreeNode child : children) {
            child.parent(this);
            splitPane.getItems().add(child);
        }
    }

    @Override
    public void addChild(int index, TreeNode child) {
        child.parent(this);
        splitPane.getItems().add(index, child);
    }

    @Override
    public boolean removeChild(TreeNode child) {
        child.parent(null);
        return splitPane.getItems().remove(child);
    }

}
