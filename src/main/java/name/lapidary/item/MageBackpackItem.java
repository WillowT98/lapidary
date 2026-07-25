package name.lapidary.item;

import dev.emi.trinkets.api.TrinketItem;
import net.minecraft.world.item.Item;

public final class MageBackpackItem
        extends TrinketItem {

    public static final int INVENTORY_ROWS = 2;

    public static final int INVENTORY_SIZE =
            INVENTORY_ROWS * 9;

    public MageBackpackItem(
            Item.Properties properties
    ) {
        super(properties);
    }

    /**
     * Prevents this backpack from being stored inside other
     * item-based containers that respect Minecraft's nesting rule.
     */
    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }
}