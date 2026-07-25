package name.lapidary.inventory;

import name.lapidary.block.ModBlocks;
import name.lapidary.item.MageBackpackItem;
import name.lapidary.item.ModItems;
import name.lapidary.screen.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class MageBackpackMenu
        extends AbstractContainerMenu {

    public static final int CANISTER_SLOT_X = 185;
    public static final int CANISTER_SLOT_Y = 18;

    public static final int CANISTER_MENU_SLOT =
            MageBackpackItem.CANISTER_SLOT_INDEX;

    private static final int GENERAL_SLOT_START = 0;

    private static final int GENERAL_SLOT_END =
            MageBackpackItem.GENERAL_INVENTORY_SIZE;

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

    /**
     * Client-side constructor used by MenuType.
     *
     * The server synchronizes the real contents into this temporary
     * client container.
     */
    public MageBackpackMenu(
            int containerId,
            Inventory playerInventory
    ) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(
                        MageBackpackItem.INVENTORY_SIZE
                )
        );
    }

    /**
     * Server-side constructor used when opening the equipped backpack.
     */
    public MageBackpackMenu(
            int containerId,
            Inventory playerInventory,
            Container backpackContainer
    ) {
        super(
                ModMenus.MAGE_BACKPACK,
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

        addGeneralBackpackSlots();
        addCanisterSlot();
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    private void addGeneralBackpackSlots() {
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
                                )
                                        && !stack.is(
                                        ModBlocks.CANISTER
                                                .asItem()
                                );
                            }
                        }
                );
            }
        }
    }

    private void addCanisterSlot() {
        this.addSlot(
                new Slot(
                        backpackContainer,
                        MageBackpackItem
                                .CANISTER_SLOT_INDEX,
                        CANISTER_SLOT_X,
                        CANISTER_SLOT_Y
                ) {
                    @Override
                    public boolean mayPlace(
                            ItemStack stack
                    ) {
                        return stack.is(
                                ModBlocks.CANISTER.asItem()
                        );
                    }

                    @Override
                    public int getMaxStackSize() {
                        return 1;
                    }
                }
        );
    }

    private void addPlayerInventory(
            Inventory playerInventory
    ) {
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

        /*
         * Any backpack-owned slot moves to the player inventory.
         */
        if (slotIndex >= GENERAL_SLOT_START
                && slotIndex < BACKPACK_SLOT_END) {

            if (!this.moveItemStackTo(
                    sourceStack,
                    PLAYER_INVENTORY_START,
                    HOTBAR_END,
                    true
            )) {
                return ItemStack.EMPTY;
            }
        } else {
            if (sourceStack.is(
                    ModItems.MAGE_BACKPACK
            )) {
                return ItemStack.EMPTY;
            }

            boolean movedIntoBackpack;

            if (sourceStack.is(
                    ModBlocks.CANISTER.asItem()
            )) {
                /*
                 * Canisters route only to the dedicated mount.
                 */
                movedIntoBackpack =
                        this.moveItemStackTo(
                                sourceStack,
                                CANISTER_MENU_SLOT,
                                CANISTER_MENU_SLOT + 1,
                                false
                        );
            } else {
                /*
                 * Ordinary items route only to general storage.
                 */
                movedIntoBackpack =
                        this.moveItemStackTo(
                                sourceStack,
                                GENERAL_SLOT_START,
                                GENERAL_SLOT_END,
                                false
                        );
            }

            if (!movedIntoBackpack) {
                if (slotIndex
                        >= PLAYER_INVENTORY_START
                        && slotIndex
                        < PLAYER_INVENTORY_END) {

                    if (!this.moveItemStackTo(
                            sourceStack,
                            HOTBAR_START,
                            HOTBAR_END,
                            false
                    )) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotIndex
                        >= HOTBAR_START
                        && slotIndex
                        < HOTBAR_END) {

                    if (!this.moveItemStackTo(
                            sourceStack,
                            PLAYER_INVENTORY_START,
                            PLAYER_INVENTORY_END,
                            false
                    )) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
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