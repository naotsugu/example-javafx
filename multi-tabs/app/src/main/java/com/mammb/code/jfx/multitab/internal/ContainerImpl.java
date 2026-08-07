package com.mammb.code.jfx.multitab.internal;

import com.mammb.code.jfx.multitab.Container;
import com.mammb.code.jfx.multitab.ContentPane;
import javafx.geometry.Side;
import javafx.scene.Node;
import java.util.Optional;

public class ContainerImpl implements Container {

    private final ContentPane pane;

    public ContainerImpl(ContentPane pane) {
        this.pane = pane;
    }

    @Override
    public void add(ContentPane contentPane) {
        leafNode().ifPresent(l -> l.add(contentPane));
    }

    @Override
    public void add(Side side, ContentPane contentPane) {
        leafNode().ifPresent(l -> l.add(contentPane, side));
    }

    private Optional<LeafNode> leafNode() {
        for (Node node = pane.getParent(); node != null; node = node.getParent()) {
            if (node instanceof LeafNode leafNode) {
                return Optional.of(leafNode);
            }
        }
        return Optional.empty();
    }
}
