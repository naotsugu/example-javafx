package com.mammb.javafx.mosaic;

import javafx.scene.layout.StackPane;

public class LeafNode extends StackPane implements ChildOf<BranchNode> {

    private BranchNode parent;

    @Override
    public BranchNode parent() {
        return parent;
    }

}
