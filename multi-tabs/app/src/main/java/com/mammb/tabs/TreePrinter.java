package com.mammb.tabs;

import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.List;

public class TreePrinter {

    public static String print(List<Stage> stages) {
        StringBuilder sb = new StringBuilder();
        sb.append("Context\n");
        for (int i = 0; i < stages.size(); i++) {
            Stage stage = stages.get(i);
            boolean isLastStage = (i == stages.size() - 1);
            String prefix = isLastStage ? "└── " : "├── ";
            String childPrefix = isLastStage ? "    " : "│   ";
            sb.append(prefix)
              .append("Stage [title=").append(stage.getTitle())
              .append(", width=").append(stage.getWidth())
              .append(", height=").append(stage.getHeight())
              .append("]\n");
            Scene scene = stage.getScene();
            if (scene != null && scene.getRoot() instanceof BranchNode root) {
                printBranchNode(root, childPrefix, true, sb);
            }
        }
        return sb.toString();
    }

    private static void printBranchNode(BranchNode node, String prefix, boolean isLast, StringBuilder sb) {
        String marker = isLast ? "└── " : "├── ";
        String childPrefix = prefix + (isLast ? "    " : "│   ");
        sb.append(prefix).append(marker)
          .append("BranchNode [orientation=").append(node.getOrientation())
          .append("]\n");

        var items = node.getItems();
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            boolean isLastItem = (i == items.size() - 1);
            if (item instanceof BranchNode childBranch) {
                printBranchNode(childBranch, childPrefix, isLastItem, sb);
            } else if (item instanceof LeafPane leaf) {
                printLeafPane(leaf, childPrefix, isLastItem, sb);
            } else {
                String itemMarker = isLastItem ? "└── " : "├── ";
                sb.append(childPrefix).append(itemMarker).append(item.getClass().getSimpleName()).append("\n");
            }
        }
    }

    private static void printLeafPane(LeafPane leaf, String prefix, boolean isLast, StringBuilder sb) {
        String marker = isLast ? "└── " : "├── ";
        String childPrefix = prefix + (isLast ? "    " : "│   ");
        sb.append(prefix).append(marker).append("LeafPane\n");

        var tabs = leaf.getTabs();
        for (int i = 0; i < tabs.size(); i++) {
            var tab = tabs.get(i);
            boolean isLastTab = (i == tabs.size() - 1);
            String tabMarker = isLastTab ? "└── " : "├── ";
            String name = tab.content().nameProperty().get();
            sb.append(childPrefix).append(tabMarker).append("TabContent [name=").append(name).append("]\n");
        }
    }

}
