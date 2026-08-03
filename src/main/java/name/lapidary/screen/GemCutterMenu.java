package name.lapidary.screen;

import name.lapidary.block.ModBlocks;
import name.lapidary.display.DisplayKind;
import name.lapidary.display.NearbyDisplayAccess;
import name.lapidary.item.ModItems;
import name.lapidary.tag.ModItemTags;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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

    private final DataSlot selectedCut =
            DataSlot.standalone();

    /*
     * Registry ID of the first compatible gem in a nearby display case.
     * This lets the client draw the ordinary cut choices even though the
     * source ItemStack remains physically in the display case.
     */
    private final DataSlot remoteInputItemId =
            DataSlot.standalone();

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

        selectedCut.set(-1);
        remoteInputItemId.set(-1);

        addSlot(
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

        addSlot(
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
                    public boolean mayPickup(
                            Player player
                    ) {
                        return !getItem().isEmpty()
                                && GemCutterMenu.this
                                .hasConsumableInput(
                                        player
                                );
                    }

                    @Override
                    public void onTake(
                            Player player,
                            ItemStack stack
                    ) {
                        if (GemCutterMenu.this
                                .consumeOneInput()) {

                            GemCutterMenu.this
                                    .selectedCut
                                    .set(-1);

                            GemCutterMenu.this
                                    .refreshRemoteInput();

                            GemCutterMenu.this
                                    .setupResult();

                            GemCutterMenu.this
                                    .playCraftSound();
                        }

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

        addDataSlot(selectedCut);
        addDataSlot(remoteInputItemId);

        refreshRemoteInput();
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
                                84 + row * 18
                        )
                );
            }
        }

        for (int column = 0;
             column < 9;
             column++) {

            addSlot(
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
        return stack.is(ModItems.SEA_GLASS)
                || stack.is(Items.DIAMOND)
                || stack.is(ModItems.FULGURITE)
                || stack.is(ModItems.HEARTROOT)
                || stack.is(ModItems.PURE_LAPIS);
    }

    private ItemStack getEffectiveInput() {
        ItemStack local =
                inputContainer.getItem(
                        INPUT_SLOT
                );

        if (!local.isEmpty()) {
            return local;
        }

        return stackFromRegistryId(
                remoteInputItemId.get()
        );
    }

    public ItemStack getRemoteInputPreview() {
        if (!inputContainer
                .getItem(INPUT_SLOT)
                .isEmpty()) {

            return ItemStack.EMPTY;
        }

        return getEffectiveInput()
                .copyWithCount(1);
    }

    private boolean matchesRemoteInput(
            ItemStack stack
    ) {
        ItemStack expected =
                stackFromRegistryId(
                        remoteInputItemId.get()
                );

        return !expected.isEmpty()
                && ItemStack.isSameItemSameComponents(
                stack,
                expected
        );
    }

    private static ItemStack stackFromRegistryId(
            int itemId
    ) {
        if (itemId < 0) {
            return ItemStack.EMPTY;
        }

        Item item =
                BuiltInRegistries.ITEM
                        .byId(itemId);

        if (item == null
                || item == Items.AIR) {

            return ItemStack.EMPTY;
        }

        return new ItemStack(item);
    }

    public List<ItemStack> getAvailableCuts() {
        ItemStack input =
                getEffectiveInput();

        if (input.is(ModItems.SEA_GLASS)) {
            return List.of(
                    new ItemStack(
                            ModItems.SEA_GLASS_EMERALD
                    )
            );
        }

        if (input.is(Items.DIAMOND)) {
            return List.of(
                    new ItemStack(
                            ModItems.DIAMOND_EMERALD
                    )
            );
        }

        if (input.is(ModItems.FULGURITE)) {
            return List.of(
                    new ItemStack(
                            ModItems.FULGURITE_EMERALD
                    )
            );
        }

        if (input.is(ModItems.HEARTROOT)) {
            return List.of(
                    new ItemStack(
                            ModItems.HEARTROOT_EMERALD
                    )
            );
        }

        if (input.is(ModItems.PURE_LAPIS)) {
            return List.of(
                    new ItemStack(
                            ModItems.LAPIS_EMERALD
                    )
            );
        }

        return List.of();
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
        return !getEffectiveInput()
                .isEmpty();
    }

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

            refreshRemoteInput();
        }

        broadcastChanges();
    }

    private void setupResult() {
        ItemStack result =
                getCutResult(
                        selectedCut.get()
                );

        resultContainer.setItem(
                0,
                result.copy()
        );

        broadcastChanges();
    }

    private void refreshRemoteInput() {
        if (!inputContainer
                .getItem(INPUT_SLOT)
                .isEmpty()) {

            remoteInputItemId.set(-1);
            return;
        }

        int current =
                remoteInputItemId.get();

        int found =
                access.evaluate(
                        (level, position) ->
                                NearbyDisplayAccess
                                        .findSource(
                                                level,
                                                position,
                                                List.of(
                                                        DisplayKind.CASE
                                                ),
                                                GemCutterMenu
                                                        ::isValidGem
                                        )
                                        .map(
                                                source ->
                                                        BuiltInRegistries
                                                                .ITEM
                                                                .getId(
                                                                        source.currentStack()
                                                                                .getItem()
                                                                )
                                        )
                                        .orElse(-1),
                        current
                );

        remoteInputItemId.set(found);
    }

    private NearbyDisplayAccess.Source
    findRemoteInputSource() {
        return access.evaluate(
                (level, position) ->
                        NearbyDisplayAccess
                                .findSource(
                                        level,
                                        position,
                                        List.of(
                                                DisplayKind.CASE
                                        ),
                                        this::matchesRemoteInput
                                )
                                .orElse(null),
                null
        );
    }

    private boolean hasConsumableInput(
            Player player
    ) {
        ItemStack local =
                inputContainer.getItem(
                        INPUT_SLOT
                );

        if (!local.isEmpty()
                && isValidGem(local)) {

            return true;
        }

        if (player.level().isClientSide) {
            return remoteInputItemId.get() >= 0;
        }

        NearbyDisplayAccess.Source source =
                findRemoteInputSource();

        return source != null
                && source.isStillValid();
    }

    private boolean consumeOneInput() {
        ItemStack local =
                inputContainer.getItem(
                        INPUT_SLOT
                );

        if (!local.isEmpty()
                && isValidGem(local)) {

            inputContainer.removeItem(
                    INPUT_SLOT,
                    1
            );

            return true;
        }

        NearbyDisplayAccess.Source source =
                findRemoteInputSource();

        return source != null
                && source.consumeOne();
    }

    private List<DisplayKind>
    outputDestinationPriority(
            ItemStack result
    ) {
        if (result.is(ModItemTags.RINGS)) {
            return List.of(
                    DisplayKind.RING,
                    DisplayKind.CASE
            );
        }

        if (result.is(ModItemTags.AMULETS)) {
            return List.of(
                    DisplayKind.AMULET,
                    DisplayKind.CASE
            );
        }

        return List.of(
                DisplayKind.CASE
        );
    }

    /**
     * Shift-clicking the output uses the nearby workshop network first.
     * Ordinary clicking still takes the result into the player's cursor.
     */
    private boolean craftIntoNearbyDisplay(
            ItemStack result
    ) {
        if (result.isEmpty()) {
            return false;
        }

        boolean localSource =
                !inputContainer
                        .getItem(INPUT_SLOT)
                        .isEmpty();

        boolean crafted =
                access.evaluate(
                        (level, position) -> {
                            NearbyDisplayAccess.Source source =
                                    localSource
                                            ? null
                                            : NearbyDisplayAccess
                                            .findSource(
                                                    level,
                                                    position,
                                                    List.of(
                                                            DisplayKind.CASE
                                                    ),
                                                    this::matchesRemoteInput
                                            )
                                            .orElse(null);

                            if (!localSource
                                    && (source == null
                                    || !source.isStillValid())) {

                                return false;
                            }

                            NearbyDisplayAccess.Destination
                                    destination =
                                    NearbyDisplayAccess
                                            .findDestination(
                                                    level,
                                                    position,
                                                    result,
                                                    outputDestinationPriority(
                                                            result
                                                    )
                                            )
                                            .orElse(null);

                            if (destination == null) {
                                return false;
                            }

                            if (localSource) {
                                inputContainer
                                        .getItem(INPUT_SLOT)
                                        .shrink(1);
                            } else if (!source.consumeOne()) {
                                return false;
                            }

                            if (!destination.insert(result)) {
                                if (localSource) {
                                    inputContainer
                                            .getItem(INPUT_SLOT)
                                            .grow(1);
                                } else {
                                    source.restoreOne();
                                }

                                return false;
                            }

                            return true;
                        },
                        false
                );

        if (crafted) {
            if (localSource) {
                inputContainer.setChanged();
            }

            selectedCut.set(-1);

            resultContainer.setItem(
                    0,
                    ItemStack.EMPTY
            );

            refreshRemoteInput();
            broadcastChanges();
            playCraftSound();
        }

        return crafted;
    }

    private void playCraftSound() {
        access.execute(
                (level, position) ->
                        level.playSound(
                                null,
                                position,
                                SoundEvents
                                        .UI_STONECUTTER_SELECT_RECIPE,
                                SoundSource.BLOCKS,
                                1.0F,
                                1.0F
                        )
        );
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

    @Override
    public void removed(
            Player player
    ) {
        super.removed(player);

        access.execute(
                (level, position) ->
                        clearContainer(
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
                slots.get(slotIndex);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack slotStack =
                slot.getItem();

        ItemStack originalCopy =
                slotStack.copy();

        if (slotIndex == RESULT_SLOT) {
            if (craftIntoNearbyDisplay(
                    originalCopy
            )) {
                return originalCopy;
            }

            if (!moveItemStackTo(
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
            if (!moveItemStackTo(
                    slotStack,
                    PLAYER_INVENTORY_START,
                    HOTBAR_END,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        } else if (isValidGem(slotStack)) {
            if (!moveItemStackTo(
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

            if (!moveItemStackTo(
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

            if (!moveItemStackTo(
                    slotStack,
                    PLAYER_INVENTORY_START,
                    PLAYER_INVENTORY_END,
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
