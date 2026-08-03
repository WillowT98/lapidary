package name.lapidary.screen;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public abstract class DisplayStorageMenu
        extends AbstractContainerMenu {

    private final Container displayInventory;
    private final int displaySlotCount;
    private final int displayColumns;
    private final int displayRows;
    private final int displayStartX;
    private final int displayStartY;
    private final int playerInventoryY;
    private final int screenHeight;

    protected DisplayStorageMenu(
            MenuType<?> menuType,
            int containerId,
            Inventory playerInventory,
            Container displayInventory,
            int displaySlotCount,
            int displayColumns,
            int displayRows,
            int displayStartX,
            int displayStartY,
            int playerInventoryY,
            int screenHeight,
            int slotLimit,
            Predicate<ItemStack> acceptance
    ) {
        super(
                menuType,
                containerId
        );

        checkContainerSize(
                displayInventory,
                displaySlotCount
        );

        this.displayInventory =
                displayInventory;

        this.displaySlotCount =
                displaySlotCount;

        this.displayColumns =
                displayColumns;

        this.displayRows =
                displayRows;

        this.displayStartX =
                displayStartX;

        this.displayStartY =
                displayStartY;

        this.playerInventoryY =
                playerInventoryY;

        this.screenHeight =
                screenHeight;

        displayInventory.startOpen(
                playerInventory.player
        );

        for (int row = 0;
             row < displayRows;
             row++) {

            for (int column = 0;
                 column < displayColumns;
                 column++) {

                int slot =
                        column
                                + row
                                * displayColumns;

                if (slot >= displaySlotCount) {
                    break;
                }

                addSlot(
                        new Slot(
                                displayInventory,
                                slot,
                                displayStartX
                                        + column * 18,
                                displayStartY
                                        + row * 18
                        ) {
                            @Override
                            public boolean mayPlace(
                                    ItemStack stack
                            ) {
                                return acceptance.test(
                                        stack
                                );
                            }

                            @Override
                            public int getMaxStackSize() {
                                return slotLimit;
                            }

                            @Override
                            public int getMaxStackSize(
                                    ItemStack stack
                            ) {
                                return Math.min(
                                        slotLimit,
                                        stack.getMaxStackSize()
                                );
                            }
                        }
                );
            }
        }

        addPlayerInventory(
                playerInventory
        );
    }

    protected static Container clientInventory(
            int size
    ) {
        return new SimpleContainer(size);
    }

    private void addPlayerInventory(
            Inventory playerInventory
    ) {
        for (int row = 0;
             row < 3;
             row++) {

            for (int column = 0;
                 column < 9;
                 column++) {

                addSlot(
                        new Slot(
                                playerInventory,
                                column
                                        + row * 9
                                        + 9,
                                8 + column * 18,
                                playerInventoryY
                                        + row * 18
                        )
                );
            }
        }

        int hotbarY =
                playerInventoryY + 58;

        for (int column = 0;
             column < 9;
             column++) {

            addSlot(
                    new Slot(
                            playerInventory,
                            column,
                            8 + column * 18,
                            hotbarY
                    )
            );
        }
    }

    @Override
    public boolean stillValid(
            Player player
    ) {
        return displayInventory
                .stillValid(player);
    }

    @Override
    public void removed(
            Player player
    ) {
        super.removed(player);

        displayInventory.stopOpen(player);
    }

    @Override
    public ItemStack quickMoveStack(
            Player player,
            int slotIndex
    ) {
        Slot slot =
                slots.get(slotIndex);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack slotStack =
                slot.getItem();

        ItemStack original =
                slotStack.copy();

        int playerStart =
                displaySlotCount;

        int playerEnd =
                slots.size();

        if (slotIndex < displaySlotCount) {
            if (!moveItemStackTo(
                    slotStack,
                    playerStart,
                    playerEnd,
                    true
            )) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(
                    slotStack,
                    0,
                    displaySlotCount,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        }

        if (slotStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (slotStack.getCount()
                == original.getCount()) {

            return ItemStack.EMPTY;
        }

        slot.onTake(
                player,
                slotStack
        );

        return original;
    }

    public int getDisplaySlotCount() {
        return displaySlotCount;
    }

    public int getDisplayColumns() {
        return displayColumns;
    }

    public int getDisplayRows() {
        return displayRows;
    }

    public int getDisplayStartX() {
        return displayStartX;
    }

    public int getDisplayStartY() {
        return displayStartY;
    }

    public int getPlayerInventoryY() {
        return playerInventoryY;
    }

    public int getScreenHeight() {
        return screenHeight;
    }
}
