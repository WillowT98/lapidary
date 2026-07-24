package name.lapidary.progression.tome;

import java.util.List;

/**
 * One node in a Tome page.
 *
 * id:
 *     Stable save-data and network identifier.
 *
 * pageId:
 *     The page on which this node appears.
 *
 * x/y:
 *     Visual coordinates relative to the page's center.
 *
 * prerequisites:
 *     Stable IDs of nodes that must already be purchased.
 */
public record TomeNode(
        String id,
        String pageId,
        int x,
        int y,
        int cost,
        List<String> prerequisites,
        boolean root
) {

    public TomeNode {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Tome node ID cannot be blank"
            );
        }

        if (pageId == null || pageId.isBlank()) {
            throw new IllegalArgumentException(
                    "Tome page ID cannot be blank"
            );
        }

        if (cost < 0) {
            throw new IllegalArgumentException(
                    "Tome node cost cannot be negative"
            );
        }

        prerequisites = List.copyOf(
                prerequisites
        );
    }

    public String translationKey() {
        return "tome.lapidary.node."
                + id.replace('/', '.');
    }
}