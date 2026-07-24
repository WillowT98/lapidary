package name.lapidary.progression.tome;

import java.util.List;

/**
 * One purchasable node in the Tome progression tree.
 *
 * index:
 *     Stable numeric index used by saved bitmasks and networking.
 *
 * id:
 *     Stable human-readable identifier for this node.
 *
 * category:
 *     Future tree/category identifier, such as "nature" or "summoning".
 *
 * x/y:
 *     Position relative to the center of the progression screen.
 *
 * prerequisites:
 *     Node indices that must already be owned.
 */
public record TomeNode(
        int index,
        String id,
        String category,
        int x,
        int y,
        int cost,
        List<Integer> prerequisites,
        boolean root
) {

    public TomeNode {
        if (index < 0 || index >= 63) {
            throw new IllegalArgumentException(
                    "Tome node index must be between 0 and 62"
            );
        }

        if (cost < 0) {
            throw new IllegalArgumentException(
                    "Tome node cost cannot be negative"
            );
        }

        prerequisites = List.copyOf(prerequisites);
    }

    public String translationKey() {
        return "tome.lapidary.node." + id;
    }
}