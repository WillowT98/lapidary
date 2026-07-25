package name.lapidary.inventory;

import name.lapidary.item.MageBackpackItem;
import name.lapidary.item.ModItems;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class MageBackpackMenu
        extends AbstractContainerMenu {

    private static final int BACKPACK_SLOT_START = 0;

    private static final int BACKPACK_SLOT_END =
            MageBackpackItem.INVENTORY_SIZE;

    private static final int PLAYER_INVENTORY_START =
            BACKPACK_SLOT_END;

    private static final int PLAYER_INVENTORY_END =
            PLAYER_INVENTORY_START + 27;

    private static final int HOTBAR_START =
            PLAYER_INVENTORY_END;

    private static final int HOTBAR_END =
            HOTBAR_START + 9;

    private final Container backpackContainer;

    public MageBackpackMenu(
            int containerId,
            Inventory playerInventory,
            Container backpackContainer
    ) {
        /*
         * We retain the vanilla two-row chest MenuType so Minecraft
         * continues to use its standard two-row chest screen.
         */
        super(
                MenuType.GENERIC_9x2,
                containerId
        );

        checkContainerSize(
                backpackContainer,
                MageBackpackItem.INVENTORY_SIZE
        );

        this.backpackContainer =
                backpackContainer;

        backpackContainer.startOpen(
                playerInventory.player
        );

        addBackpackSlots();
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    private void addBackpackSlots() {
        for (int row = 0;
             row < MageBackpackItem.INVENTORY_ROWS;
             row++) {

            for (int column = 0;
                 column < 9;
                 column++) {

                int containerSlot =
                        column + row * 9;

                this.addSlot(
                        new Slot(
                                backpackContainer,
                                containerSlot,
                                8 + column * 18,
                                18 + row * 18
                        ) {
                            @Override
                            public boolean mayPlace(
                                    ItemStack stack
                            ) {
                                return !stack.is(
                                        ModItems.MAGE_BACKPACK
                                );
                            }
                        }
                );
            }
        }
    }

    private void addPlayerInventory(
            Inventory playerInventory
    ) {
        /*
         * The vertical offset matches vanilla's two-row chest menu.
         */
        int verticalOffset =
                (
                        MageBackpackItem.INVENTORY_ROWS
                                - 4
                ) * 18;

        for (int row = 0;
             row < 3;
             row++) {

            for (int column = 0;
                 column < 9;
                 column++) {

                int playerSlot =
                        column
                                + row * 9
                                + 9;

                this.addSlot(
                        new Slot(
                                playerInventory,
                                playerSlot,
                                8 + column * 18,
                                103
                                        + row * 18
                                        + verticalOffset
                        )
                );
            }
        }
    }

    private void addPlayerHotbar(
            Inventory playerInventory
    ) {
        int verticalOffset =
                (
                        MageBackpackItem.INVENTORY_ROWS
                                - 4
                ) * 18;

        for (int column = 0;
             column < 9;
             column++) {

            this.addSlot(
                    new Slot(
                            playerInventory,
                            column,
                            8 + column * 18,
                            161 + verticalOffset
                    )
            );
        }
    }

    @Override
    public boolean stillValid(
            Player player
    ) {
        return backpackContainer.stillValid(
                player
        );
    }

    @Override
    public ItemStack quickMoveStack(
            Player player,
            int slotIndex
    ) {
        Slot sourceSlot =
                this.slots.get(slotIndex);

        if (!sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack =
                sourceSlot.getItem();

        ItemStack originalStack =
                sourceStack.copy();

        if (slotIndex >= BACKPACK_SLOT_START
                && slotIndex < BACKPACK_SLOT_END) {

            /*
             * Backpack inventory → player inventory.
             */
            if (!this.moveItemStackTo(
                    sourceStack,
                    PLAYER_INVENTORY_START,
                    HOTBAR_END,
                    true
            )) {
                return ItemStack.EMPTY;
            }
        } else {
            /*
             * Explicitly reject shift-clicking a backpack into
             * the equipped backpack.
             */
            if (sourceStack.is(
                    ModItems.MAGE_BACKPACK
            )) {
                return ItemStack.EMPTY;
            }

            /*
             * Player inventory → backpack inventory.
             */
            if (!this.moveItemStackTo(
                    sourceStack,
                    BACKPACK_SLOT_START,
                    BACKPACK_SLOT_END,
                    false
            )) {
                /*
                 * Preserve ordinary main-inventory/hotbar movement
                 * when the backpack cannot accept the item.
                 */
                if (slotIndex >= PLAYER_INVENTORY_START
                        && slotIndex < PLAYER_INVENTORY_END) {

                    if (!this.moveItemStackTo(
                            sourceStack,
                            HOTBAR_START,
                            HOTBAR_END,
                            false
                    )) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotIndex >= HOTBAR_START
                        && slotIndex < HOTBAR_END) {

                    if (!this.moveItemStackTo(
                            sourceStack,
                            PLAYER_INVENTORY_START,
                            PLAYER_INVENTORY_END,
                            false
                    )) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(
                    ItemStack.EMPTY
            );
        } else {
            sourceSlot.setChanged();
        }

        if (sourceStack.getCount()
                == originalStack.getCount()) {

            return ItemStack.EMPTY;
        }

        sourceSlot.onTake(
                player,
                sourceStack
        );

        return originalStack;
    }

    @Override
    public void removed(
            Player player
    ) {
        super.removed(player);

        backpackContainer.stopOpen(
                player
        );
    }
}