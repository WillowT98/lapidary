package name.lapidary.item;

import dev.emi.trinkets.api.TrinketItem;
import name.lapidary.block.ModBlocks;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public final class MageBackpackItem
        extends TrinketItem {

    public static final int INVENTORY_ROWS = 2;

    /*
     * The ordinary two rows remain unchanged.
     */
    public static final int GENERAL_INVENTORY_SIZE =
            INVENTORY_ROWS * 9;

    /*
     * The dedicated canister is stored immediately after the
     * eighteen ordinary inventory slots.
     */
    public static final int CANISTER_SLOT_INDEX =
            GENERAL_INVENTORY_SIZE;

    public static final int INVENTORY_SIZE =
            GENERAL_INVENTORY_SIZE + 1;

    public MageBackpackItem(
            Item.Properties properties
    ) {
        super(properties);
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    /**
     * Reads the canister mounted in a backpack ItemStack.
     *
     * This works on both the logical server and client because the
     * entire backpack inventory is stored in DataComponents.CONTAINER.
     */
    public static ItemStack getMountedCanister(
            ItemStack backpackStack
    ) {
        if (!backpackStack.is(
                ModItems.MAGE_BACKPACK
        )) {
            return ItemStack.EMPTY;
        }

        ItemContainerContents contents =
                backpackStack.get(
                        DataComponents.CONTAINER
                );

        if (contents == null) {
            return ItemStack.EMPTY;
        }

        NonNullList<ItemStack> storedItems =
                NonNullList.withSize(
                        INVENTORY_SIZE,
                        ItemStack.EMPTY
                );

        contents.copyInto(
                storedItems
        );

        ItemStack canisterStack =
                storedItems.get(
                        CANISTER_SLOT_INDEX
                );

        if (!canisterStack.is(
                ModBlocks.CANISTER.asItem()
        )) {
            return ItemStack.EMPTY;
        }

        return canisterStack;
    }
}