package com.mammb.javafx.mosaic;

public interface ChildOfBranch extends ChildOf<BranchNode> {
    BranchNode parent();
    void parent(BranchNode parent);
}
