package name.lapidary.inventory;

import dev.emi.trinkets.api.TrinketInventory;
import name.lapidary.item.MageBackpackItem;
import name.lapidary.item.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.List;

public final class MageBackpackContainer
        extends SimpleContainer {

    private final ItemStack backpackStack;
    private final TrinketInventory trinketInventory;
    private final int trinketSlotIndex;

    /*
     * Prevent loading the component from immediately writing it
     * back once for every slot.
     */
    private boolean loading;

    public MageBackpackContainer(
            ItemStack backpackStack,
            TrinketInventory trinketInventory,
            int trinketSlotIndex
    ) {
        super(
                MageBackpackItem.INVENTORY_SIZE
        );

        this.backpackStack =
                backpackStack;

        this.trinketInventory =
                trinketInventory;

        this.trinketSlotIndex =
                trinketSlotIndex;

        loadContents();
    }

    private void loadContents() {
        this.loading = true;

        NonNullList<ItemStack> loadedItems =
                NonNullList.withSize(
                        MageBackpackItem.INVENTORY_SIZE,
                        ItemStack.EMPTY
                );

        ItemContainerContents contents =
                backpackStack.get(
                        DataComponents.CONTAINER
                );

        if (contents != null) {
            contents.copyInto(
                    loadedItems
            );
        }

        for (int slot = 0;
             slot < loadedItems.size();
             slot++) {

            super.setItem(
                    slot,
                    loadedItems.get(slot)
            );
        }

        this.loading = false;
    }

    @Override
    public void setChanged() {
        super.setChanged();

        if (!loading) {
            saveContents();
        }
    }

    private void saveContents() {
        List<ItemStack> savedItems =
                new ArrayList<>(
                        getContainerSize()
                );

        for (int slot = 0;
             slot < getContainerSize();
             slot++) {

            savedItems.add(
                    getItem(slot).copy()
            );
        }

        backpackStack.set(
                DataComponents.CONTAINER,
                ItemContainerContents.fromItems(
                        savedItems
                )
        );

        /*
         * Tell Trinkets that the equipped stack changed so its
         * updated data component is synchronized to the client.
         */
        trinketInventory.markUpdate();
    }

    @Override
    public boolean canPlaceItem(
            int slot,
            ItemStack stack
    ) {
        /*
         * Prevent recursive backpack storage and the associated
         * duplication and save-depth problems.
         */
        return !stack.is(
                ModItems.MAGE_BACKPACK
        );
    }

    @Override
    public boolean stillValid(
            Player player
    ) {
        if (trinketSlotIndex < 0
                || trinketSlotIndex
                >= trinketInventory
                .getContainerSize()) {

            return false;
        }

        ItemStack equippedStack =
                trinketInventory.getItem(
                        trinketSlotIndex
                );

        return equippedStack.is(
                ModItems.MAGE_BACKPACK
        );
    }
}