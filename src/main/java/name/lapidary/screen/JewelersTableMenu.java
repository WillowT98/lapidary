package name.lapidary.screen;

import name.lapidary.block.ModBlocks;
import name.lapidary.item.ModItems;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class JewelersTableMenu
        extends AbstractContainerMenu {

    public static final int GEM_SLOT = 0;
    public static final int JEWELRY_SLOT = 1;
    public static final int RESULT_SLOT = 2;

    private static final int PLAYER_INVENTORY_START = 3;
    private static final int PLAYER_INVENTORY_END = 30;

    private static final int HOTBAR_START = 30;
    private static final int HOTBAR_END = 39;

    private final ContainerLevelAccess access;

    private final Container inputContainer =
            new SimpleContainer(2) {
                @Override
                public void setChanged() {
                    super.setChanged();

                    JewelersTableMenu.this.slotsChanged(
                            this
                    );
                }
            };

    private final ResultContainer resultContainer =
            new ResultContainer();

    /*
     * Client-side constructor used by the registered MenuType.
     */
    public JewelersTableMenu(
            int containerId,
            Inventory playerInventory
    ) {
        this(
                containerId,
                playerInventory,
                ContainerLevelAccess.NULL
        );
    }

    /*
     * Server-side constructor used by JewelersTableBlock.
     */
    public JewelersTableMenu(
            int containerId,
            Inventory playerInventory,
            ContainerLevelAccess access
    ) {
        super(
                ModMenus.JEWELERS_TABLE,
                containerId
        );

        this.access = access;

        /*
         * Cut-gem input.
         */
        this.addSlot(
                new Slot(
                        inputContainer,
                        GEM_SLOT,
                        27,
                        47
                ) {
                    @Override
                    public boolean mayPlace(
                            ItemStack stack
                    ) {
                        return isAcceptedGem(stack);
                    }
                }
        );

        /*
         * Empty-jewelry input.
         */
        this.addSlot(
                new Slot(
                        inputContainer,
                        JEWELRY_SLOT,
                        76,
                        47
                ) {
                    @Override
                    public boolean mayPlace(
                            ItemStack stack
                    ) {
                        return isAcceptedJewelry(stack);
                    }
                }
        );

        /*
         * Combined jewelry output.
         */
        this.addSlot(
                new Slot(
                        resultContainer,
                        0,
                        134,
                        47
                ) {
                    @Override
                    public boolean mayPlace(
                            ItemStack stack
                    ) {
                        return false;
                    }

                    @Override
                    public void onTake(
                            Player player,
                            ItemStack stack
                    ) {
                        JewelersTableMenu.this.inputContainer
                                .removeItem(
                                        GEM_SLOT,
                                        1
                                );

                        JewelersTableMenu.this.inputContainer
                                .removeItem(
                                        JEWELRY_SLOT,
                                        1
                                );

                        JewelersTableMenu.this.setupResult();

                        super.onTake(
                                player,
                                stack
                        );
                    }
                }
        );

        addPlayerInventory(
                playerInventory
        );

        /*
         * Establish the initial empty output.
         */
        setupResult();
    }

    private static boolean isAcceptedGem(
            ItemStack stack
    ) {
        return stack.is(
                ModItems.SEA_GLASS_EMERALD
        );
    }

    private static boolean isAcceptedJewelry(
            ItemStack stack
    ) {
        return stack.is(
                ModItems.NECKLACE_EMPTY
        );
    }

    private void setupResult() {
        ItemStack gem =
                inputContainer.getItem(
                        GEM_SLOT
                );

        ItemStack jewelry =
                inputContainer.getItem(
                        JEWELRY_SLOT
                );

        if (gem.is(ModItems.SEA_GLASS_EMERALD)
                && jewelry.is(ModItems.NECKLACE_EMPTY)) {

            resultContainer.setItem(
                    0,
                    new ItemStack(
                            ModItems.NECKLACE_SEA_GLASS
                    )
            );
        } else {
            resultContainer.setItem(
                    0,
                    ItemStack.EMPTY
            );
        }

        this.broadcastChanges();
    }

    @Override
    public void slotsChanged(
            Container container
    ) {
        super.slotsChanged(container);

        if (container == inputContainer) {
            setupResult();
        }
    }

    private void addPlayerInventory(
            Inventory playerInventory
    ) {
        /*
         * Main inventory: three rows of nine.
         */
        for (int row = 0; row < 3; row++) {
            for (int column = 0;
                 column < 9;
                 column++) {

                this.addSlot(
                        new Slot(
                                playerInventory,
                                column
                                        + row * 9
                                        + 9,
                                8 + column * 18,
                                84 + row * 18
                        )
                );
            }
        }

        /*
         * Hotbar.
         */
        for (int column = 0;
             column < 9;
             column++) {

            this.addSlot(
                    new Slot(
                            playerInventory,
                            column,
                            8 + column * 18,
                            142
                    )
            );
        }
    }

    @Override
    public boolean stillValid(
            Player player
    ) {
        return stillValid(
                access,
                player,
                ModBlocks.JEWELERS_TABLE
        );
    }

    @Override
    public void removed(
            Player player
    ) {
        super.removed(player);

        /*
         * Return unused ingredients when the interface closes.
         */
        access.execute(
                (level, position) ->
                        this.clearContainer(
                                player,
                                inputContainer
                        )
        );
    }

    @Override
    public ItemStack quickMoveStack(
            Player player,
            int slotIndex
    ) {
        Slot slot =
                this.slots.get(slotIndex);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack slotStack =
                slot.getItem();

        ItemStack originalCopy =
                slotStack.copy();

        if (slotIndex == RESULT_SLOT) {
            /*
             * Result → player inventory.
             */
            if (!this.moveItemStackTo(
                    slotStack,
                    PLAYER_INVENTORY_START,
                    HOTBAR_END,
                    true
            )) {
                return ItemStack.EMPTY;
            }

            slot.onQuickCraft(
                    slotStack,
                    originalCopy
            );
        } else if (slotIndex == GEM_SLOT
                || slotIndex == JEWELRY_SLOT) {

            /*
             * Input → player inventory.
             */
            if (!this.moveItemStackTo(
                    slotStack,
                    PLAYER_INVENTORY_START,
                    HOTBAR_END,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        } else if (isAcceptedGem(slotStack)) {
            /*
             * Player inventory → gem slot.
             */
            if (!this.moveItemStackTo(
                    slotStack,
                    GEM_SLOT,
                    GEM_SLOT + 1,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        } else if (isAcceptedJewelry(slotStack)) {
            /*
             * Player inventory → jewelry slot.
             */
            if (!this.moveItemStackTo(
                    slotStack,
                    JEWELRY_SLOT,
                    JEWELRY_SLOT + 1,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex >= PLAYER_INVENTORY_START
                && slotIndex < PLAYER_INVENTORY_END) {

            /*
             * Main inventory → hotbar.
             */
            if (!this.moveItemStackTo(
                    slotStack,
                    HOTBAR_START,
                    HOTBAR_END,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex >= HOTBAR_START
                && slotIndex < HOTBAR_END) {

            /*
             * Hotbar → main inventory.
             */
            if (!this.moveItemStackTo(
                    slotStack,
                    PLAYER_INVENTORY_START,
                    PLAYER_INVENTORY_END,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        }

        if (slotStack.isEmpty()) {
            slot.set(
                    ItemStack.EMPTY
            );
        } else {
            slot.setChanged();
        }

        if (slotStack.getCount()
                == originalCopy.getCount()) {

            return ItemStack.EMPTY;
        }

        slot.onTake(
                player,
                slotStack
        );

        return originalCopy;
    }

    @Override
    public boolean canTakeItemForPickAll(
            ItemStack stack,
            Slot slot
    ) {
        return slot.container
                != resultContainer
                && super.canTakeItemForPickAll(
                stack,
                slot
        );
    }
}