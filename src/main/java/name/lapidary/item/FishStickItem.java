package name.lapidary.item;

import net.minecraft.world.item.Item;

/**
 * Lures and eventually controls Lapidary's aerial mounts.
 *
 * The active riding behavior will be added once mantas and coatls
 * implement the shared steerable-mount interface.
 */
public final class FishStickItem extends Item {

    public FishStickItem(Properties properties) {
        super(properties);
    }
}