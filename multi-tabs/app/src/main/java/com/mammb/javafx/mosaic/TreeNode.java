package com.mammb.javafx.mosaic;

import javafx.scene.layout.StackPane;

public abstract class TreeNode extends StackPane implements ChildOf<BranchNode> {
    public abstract BranchNode parent();
    public abstract void parent(BranchNode parent);
}
