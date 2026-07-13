package com.mammb.javafx.mosaic;

import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
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
        addChildren(List.of(new LeafNode(ctx, content)));
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
            addChild(insIndex, new LeafNode(ctx, content));
            splitPane.setOrientation(orientation);
        } else {
            removeChild(source);
            BranchNode newChild = new BranchNode(ctx, this);
            newChild.orientation(orientation);
            newChild.addChildren(List.of(source));
            int insIndex = (side == Side.RIGHT || side == Side.BOTTOM) ? 1 : 0;
            newChild.addChild(insIndex, new LeafNode(ctx, content));
            addChild(sourceIndex, newChild);
        }
    }

    void eject(TreeNode node) {
        removeChild(node);
        balance();
    }

    private void balance() {

        List<TreeNode> children = children();
        if (isRoot() && children.isEmpty()) {
            if (ctx.stages().size() == 1) {
                addChildren(List.of(new LeafNode(ctx, ctx.contentSupplier().apply(""))));
            } else {
                ((Stage) getScene().getWindow()).close();
            }
        }

        if (isRoot()) return;

        int childrenSize = children.size();
        if (childrenSize <= 1) {
            if (childrenSize == 1) {
                parent.addChild(parent.children().indexOf(this), children.getFirst());
            }
            BranchNode prevParent = parent;
            parent.removeChild(this);
            prevParent.balance();
            return;
        }
        parent.balance();
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

    public Orientation orientation() {
        return splitPane.getOrientation();
    }

    public void orientation(Orientation value) {
        splitPane.setOrientation(value);
    }

    public double[] dividerPositions() {
        return splitPane.getDividerPositions();
    }

    public void dividerPositions(double[] value) {
        splitPane.setDividerPositions(value);
    }

}
