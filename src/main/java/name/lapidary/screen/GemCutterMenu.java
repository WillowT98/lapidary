package name.lapidary.screen;

import name.lapidary.block.ModBlocks;
import name.lapidary.item.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class GemCutterMenu
        extends AbstractContainerMenu {

    public static final int INPUT_SLOT = 0;
    public static final int RESULT_SLOT = 1;

    private static final int PLAYER_INVENTORY_START = 2;
    private static final int PLAYER_INVENTORY_END = 29;
    private static final int HOTBAR_START = 29;
    private static final int HOTBAR_END = 38;

    private final ContainerLevelAccess access;

    /*
     * This temporary inventory behaves like the vanilla stonecutter:
     * the input exists only while the menu is open.
     */
    private final Container inputContainer =
            new SimpleContainer(1) {
                @Override
                public void setChanged() {
                    super.setChanged();

                    GemCutterMenu.this.slotsChanged(
                            this
                    );
                }
            };

    private final ResultContainer resultContainer =
            new ResultContainer();

    /*
     * -1 means no cut is currently selected.
     */
    private final DataSlot selectedCut =
            DataSlot.standalone();

    /*
     * Client-side constructor used by MenuType.
     */
    public GemCutterMenu(
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
     * Server-side constructor used by GemCutterBlock.
     */
    public GemCutterMenu(
            int containerId,
            Inventory playerInventory,
            ContainerLevelAccess access
    ) {
        super(
                ModMenus.GEM_CUTTER,
                containerId
        );

        this.access = access;
        this.selectedCut.set(-1);

        /*
         * Gem input slot.
         */
        this.addSlot(
                new Slot(
                        inputContainer,
                        0,
                        20,
                        33
                ) {
                    @Override
                    public boolean mayPlace(
                            ItemStack stack
                    ) {
                        return isValidGem(stack);
                    }
                }
        );

        /*
         * Finished-cut output slot.
         */
        this.addSlot(
                new Slot(
                        resultContainer,
                        0,
                        143,
                        33
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
                        /*
                         * Consume one gem for each result taken.
                         */
                        GemCutterMenu.this.inputContainer
                                .removeItem(
                                        0,
                                        1
                                );

                        GemCutterMenu.this.selectedCut
                                .set(-1);

                        GemCutterMenu.this.setupResult();

                        GemCutterMenu.this.access.execute(
                                (level, position) ->
                                        level.playSound(
                                                null,
                                                position,
                                                SoundEvents.UI_STONECUTTER_SELECT_RECIPE,
                                                SoundSource.BLOCKS,
                                                1.0F,
                                                1.0F
                                        )
                        );

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

        this.addDataSlot(
                selectedCut
        );
    }

    private void addPlayerInventory(
            Inventory playerInventory
    ) {
        /*
         * Main player inventory: three rows of nine.
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

    private static boolean isValidGem(
            ItemStack stack
    ) {
        return stack.is(
                ModItems.SEA_GLASS
        );
    }

    /*
     * This is deliberately hard-coded for the first milestone.
     *
     * Later, this method can read actual gem-cutting recipes.
     */
    public List<ItemStack> getAvailableCuts() {
        if (!isValidGem(
                inputContainer.getItem(0)
        )) {
            return List.of();
        }

        return List.of(
                new ItemStack(
                        ModItems.SEA_GLASS_EMERALD
                )
        );
    }

    public int getAvailableCutCount() {
        return getAvailableCuts().size();
    }

    public ItemStack getCutResult(
            int index
    ) {
        List<ItemStack> cuts =
                getAvailableCuts();

        if (index < 0
                || index >= cuts.size()) {

            return ItemStack.EMPTY;
        }

        return cuts.get(index);
    }

    public int getSelectedCut() {
        return selectedCut.get();
    }

    public boolean hasInputItem() {
        return !inputContainer
                .getItem(0)
                .isEmpty();
    }

    /*
     * Called when the player clicks one of the graphical cut choices.
     * Minecraft automatically sends this button ID to the server.
     */
    @Override
    public boolean clickMenuButton(
            Player player,
            int buttonId
    ) {
        if (buttonId < 0
                || buttonId
                >= getAvailableCutCount()) {

            return false;
        }

        selectedCut.set(buttonId);
        setupResult();

        return true;
    }

    @Override
    public void slotsChanged(
            Container container
    ) {
        if (container == inputContainer) {
            selectedCut.set(-1);

            resultContainer.setItem(
                    0,
                    ItemStack.EMPTY
            );
        }

        this.broadcastChanges();
    }

    private void setupResult() {
        int selectedIndex =
                selectedCut.get();

        ItemStack result =
                getCutResult(selectedIndex);

        resultContainer.setItem(
                0,
                result.copy()
        );

        this.broadcastChanges();
    }

    @Override
    public boolean stillValid(
            Player player
    ) {
        return stillValid(
                access,
                player,
                ModBlocks.GEM_CUTTER
        );
    }

    /*
     * Return the uncut gem when the player closes the interface.
     */
    @Override
    public void removed(
            Player player
    ) {
        super.removed(player);

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
        ItemStack originalCopy =
                ItemStack.EMPTY;

        Slot slot =
                this.slots.get(slotIndex);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack slotStack =
                slot.getItem();

        originalCopy =
                slotStack.copy();

        if (slotIndex == RESULT_SLOT) {
            /*
             * Shift-clicking the result moves it into the inventory.
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
        } else if (slotIndex == INPUT_SLOT) {
            /*
             * Shift-clicking the input returns it to the player.
             */
            if (!this.moveItemStackTo(
                    slotStack,
                    PLAYER_INVENTORY_START,
                    HOTBAR_END,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        } else if (isValidGem(slotStack)) {
            /*
             * Shift-clicking sea glass inserts it into the cutter.
             */
            if (!this.moveItemStackTo(
                    slotStack,
                    INPUT_SLOT,
                    INPUT_SLOT + 1,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex
                >= PLAYER_INVENTORY_START
                && slotIndex
                < PLAYER_INVENTORY_END) {

            if (!this.moveItemStackTo(
                    slotStack,
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