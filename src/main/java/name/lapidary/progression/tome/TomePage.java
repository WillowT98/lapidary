package name.lapidary.progression.tome;

import java.util.List;

public record TomePage(
        String id,
        String unlockNodeId,
        List<TomeNode> nodes
) {

    public TomePage {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Tome page ID cannot be blank"
            );
        }

        nodes = List.copyOf(nodes);

        long rootCount =
                nodes.stream()
                        .filter(TomeNode::root)
                        .count();

        if (rootCount != 1) {
            throw new IllegalArgumentException(
                    "Tome page "
                            + id
                            + " must contain exactly one root"
            );
        }

        for (TomeNode node : nodes) {
            if (!node.pageId().equals(id)) {
                throw new IllegalArgumentException(
                        "Node "
                                + node.id()
                                + " belongs to "
                                + node.pageId()
                                + ", not "
                                + id
                );
            }
        }
    }

    public boolean alwaysAvailable() {
        return unlockNodeId == null;
    }

    public TomeNode root() {
        for (TomeNode node : nodes) {
            if (node.root()) {
                return node;
            }
        }

        throw new IllegalStateException(
                "Page has no root: " + id
        );
    }

    public String translationKey() {
        return "tome.lapidary.page." + id;
    }
}