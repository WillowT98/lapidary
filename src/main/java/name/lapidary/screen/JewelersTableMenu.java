package name.lapidary.screen;

import name.lapidary.block.ModBlocks;
import name.lapidary.display.DisplayKind;
import name.lapidary.display.NearbyDisplayAccess;
import name.lapidary.item.ModItems;
import name.lapidary.tag.ModItemTags;
import net.minecraft.core.registries.BuiltInRegistries;
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

public final class JewelersTableMenu
        extends AbstractContainerMenu {

    public static final int GEM_SLOT = 0;
    public static final int JEWELRY_SLOT = 1;
    public static final int RESULT_SLOT = 2;

    private static final int PLAYER_INVENTORY_START = 3;
    private static final int PLAYER_INVENTORY_END = 30;
    private static final int HOTBAR_START = 30;
    private static final int HOTBAR_END = 39;

    private static final List<JewelrySet>
            JEWELRY_SETS =
            List.of(
                    new JewelrySet(
                            ModItems.SEA_GLASS_EMERALD,
                            ModItems.NECKLACE_SEA_GLASS,
                            ModItems.RING_SEAGLASS
                    ),
                    new JewelrySet(
                            ModItems.DIAMOND_EMERALD,
                            ModItems.NECKLACE_DIAMOND,
                            ModItems.RING_DIAMOND
                    ),
                    new JewelrySet(
                            ModItems.FULGURITE_EMERALD,
                            ModItems.NECKLACE_FULGURITE,
                            ModItems.RING_FULGURITE
                    ),
                    new JewelrySet(
                            ModItems.HEARTROOT_EMERALD,
                            ModItems.NECKLACE_HEARTROOT,
                            ModItems.RING_HEARTROOT
                    ),
                    new JewelrySet(
                            ModItems.LAPIS_EMERALD,
                            ModItems.NECKLACE_LAPIS,
                            ModItems.RING_LAPIS
                    )
            );

    private final ContainerLevelAccess access;

    private final Container inputContainer =
            new SimpleContainer(2) {
                @Override
                public void setChanged() {
                    super.setChanged();

                    JewelersTableMenu.this
                            .slotsChanged(this);
                }
            };

    private final ResultContainer resultContainer =
            new ResultContainer();

    private final DataSlot remoteGemItemId =
            DataSlot.standalone();

    private final DataSlot remoteJewelryItemId =
            DataSlot.standalone();

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

        remoteGemItemId.set(-1);
        remoteJewelryItemId.set(-1);

        addSlot(
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

        addSlot(
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

        addSlot(
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
                    public boolean mayPickup(
                            Player player
                    ) {
                        return !getItem().isEmpty()
                                && JewelersTableMenu.this
                                .hasConsumableInputs(
                                        player
                                );
                    }

                    @Override
                    public void onTake(
                            Player player,
                            ItemStack stack
                    ) {
                        if (JewelersTableMenu.this
                                .consumeInputs()) {

                            JewelersTableMenu.this
                                    .refreshRemoteInputs();

                            JewelersTableMenu.this
                                    .setupResult();
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

        addDataSlot(remoteGemItemId);
        addDataSlot(remoteJewelryItemId);

        refreshRemoteInputs();
        setupResult();
    }

    private static boolean isAcceptedGem(
            ItemStack stack
    ) {
        return findJewelrySet(stack)
                != null;
    }

    private static boolean isAcceptedJewelry(
            ItemStack stack
    ) {
        return stack.is(
                ModItems.NECKLACE_EMPTY
        ) || stack.is(
                ModItems.RING_EMPTY
        );
    }

    private static JewelrySet findJewelrySet(
            ItemStack gemStack
    ) {
        for (JewelrySet jewelrySet :
                JEWELRY_SETS) {

            if (gemStack.is(
                    jewelrySet.cutGem()
            )) {
                return jewelrySet;
            }
        }

        return null;
    }

    private static Item findResultItem(
            ItemStack gemStack,
            ItemStack jewelryStack
    ) {
        JewelrySet jewelrySet =
                findJewelrySet(
                        gemStack
                );

        if (jewelrySet == null) {
            return null;
        }

        if (jewelryStack.is(
                ModItems.NECKLACE_EMPTY
        )) {
            return jewelrySet.necklace();
        }

        if (jewelryStack.is(
                ModItems.RING_EMPTY
        )) {
            return jewelrySet.ring();
        }

        return null;
    }

    private ItemStack getEffectiveGem() {
        ItemStack local =
                inputContainer.getItem(
                        GEM_SLOT
                );

        if (!local.isEmpty()) {
            return local;
        }

        return stackFromRegistryId(
                remoteGemItemId.get()
        );
    }

    private ItemStack getEffectiveJewelry() {
        ItemStack local =
                inputContainer.getItem(
                        JEWELRY_SLOT
                );

        if (!local.isEmpty()) {
            return local;
        }

        return stackFromRegistryId(
                remoteJewelryItemId.get()
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

    public ItemStack getRemoteGemPreview() {
        if (!inputContainer
                .getItem(GEM_SLOT)
                .isEmpty()) {

            return ItemStack.EMPTY;
        }

        return getEffectiveGem()
                .copyWithCount(1);
    }

    public ItemStack getRemoteJewelryPreview() {
        if (!inputContainer
                .getItem(JEWELRY_SLOT)
                .isEmpty()) {

            return ItemStack.EMPTY;
        }

        return getEffectiveJewelry()
                .copyWithCount(1);
    }

    private boolean matchesRemoteGem(
            ItemStack stack
    ) {
        ItemStack expected =
                stackFromRegistryId(
                        remoteGemItemId.get()
                );

        return !expected.isEmpty()
                && ItemStack.isSameItemSameComponents(
                stack,
                expected
        );
    }

    private boolean matchesRemoteJewelry(
            ItemStack stack
    ) {
        ItemStack expected =
                stackFromRegistryId(
                        remoteJewelryItemId.get()
                );

        return !expected.isEmpty()
                && ItemStack.isSameItemSameComponents(
                stack,
                expected
        );
    }

    private void setupResult() {
        Item resultItem =
                findResultItem(
                        getEffectiveGem(),
                        getEffectiveJewelry()
                );

        resultContainer.setItem(
                0,
                resultItem == null
                        ? ItemStack.EMPTY
                        : new ItemStack(resultItem)
        );

        broadcastChanges();
    }

    @Override
    public void slotsChanged(
            Container container
    ) {
        super.slotsChanged(container);

        if (container == inputContainer) {
            refreshRemoteInputs();
            setupResult();
        }
    }

    private void refreshRemoteInputs() {
        if (!inputContainer
                .getItem(GEM_SLOT)
                .isEmpty()) {

            remoteGemItemId.set(-1);
        } else {
            int current =
                    remoteGemItemId.get();

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
                                                    JewelersTableMenu
                                                            ::isAcceptedGem
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

            remoteGemItemId.set(found);
        }

        if (!inputContainer
                .getItem(JEWELRY_SLOT)
                .isEmpty()) {

            remoteJewelryItemId.set(-1);
        } else {
            int current =
                    remoteJewelryItemId.get();

            int found =
                    access.evaluate(
                            (level, position) ->
                                    NearbyDisplayAccess
                                            .findSource(
                                                    level,
                                                    position,
                                                    List.of(
                                                            DisplayKind.RING,
                                                            DisplayKind.AMULET,
                                                            DisplayKind.CASE
                                                    ),
                                                    JewelersTableMenu
                                                            ::isAcceptedJewelry
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

            remoteJewelryItemId.set(found);
        }
    }

    private NearbyDisplayAccess.Source
    findRemoteGemSource() {
        return access.evaluate(
                (level, position) ->
                        NearbyDisplayAccess
                                .findSource(
                                        level,
                                        position,
                                        List.of(
                                                DisplayKind.CASE
                                        ),
                                        this::matchesRemoteGem
                                )
                                .orElse(null),
                null
        );
    }

    private NearbyDisplayAccess.Source
    findRemoteJewelrySource() {
        return access.evaluate(
                (level, position) ->
                        NearbyDisplayAccess
                                .findSource(
                                        level,
                                        position,
                                        List.of(
                                                DisplayKind.RING,
                                                DisplayKind.AMULET,
                                                DisplayKind.CASE
                                        ),
                                        this::matchesRemoteJewelry
                                )
                                .orElse(null),
                null
        );
    }

    private boolean hasConsumableInputs(
            Player player
    ) {
        boolean hasGem =
                !inputContainer
                        .getItem(GEM_SLOT)
                        .isEmpty();

        boolean hasJewelry =
                !inputContainer
                        .getItem(JEWELRY_SLOT)
                        .isEmpty();

        if (player.level().isClientSide) {
            return (hasGem
                    || remoteGemItemId.get() >= 0)
                    && (hasJewelry
                    || remoteJewelryItemId.get() >= 0);
        }

        NearbyDisplayAccess.Source gemSource =
                hasGem
                        ? null
                        : findRemoteGemSource();

        NearbyDisplayAccess.Source jewelrySource =
                hasJewelry
                        ? null
                        : findRemoteJewelrySource();

        return (hasGem
                || (gemSource != null
                && gemSource.isStillValid()))
                && (hasJewelry
                || (jewelrySource != null
                && jewelrySource.isStillValid()));
    }

    private boolean consumeInputs() {
        boolean localGem =
                !inputContainer
                        .getItem(GEM_SLOT)
                        .isEmpty();

        boolean localJewelry =
                !inputContainer
                        .getItem(JEWELRY_SLOT)
                        .isEmpty();

        NearbyDisplayAccess.Source gemSource =
                localGem
                        ? null
                        : findRemoteGemSource();

        NearbyDisplayAccess.Source jewelrySource =
                localJewelry
                        ? null
                        : findRemoteJewelrySource();

        if ((!localGem
                && (gemSource == null
                || !gemSource.isStillValid()))
                || (!localJewelry
                && (jewelrySource == null
                || !jewelrySource.isStillValid()))) {

            return false;
        }

        if (localGem) {
            inputContainer
                    .getItem(GEM_SLOT)
                    .shrink(1);
        } else if (!gemSource.consumeOne()) {
            return false;
        }

        if (localJewelry) {
            inputContainer
                    .getItem(JEWELRY_SLOT)
                    .shrink(1);
        } else if (!jewelrySource.consumeOne()) {
            if (localGem) {
                inputContainer
                        .getItem(GEM_SLOT)
                        .grow(1);
            } else {
                gemSource.restoreOne();
            }

            return false;
        }

        if (localGem || localJewelry) {
            inputContainer.setChanged();
        }

        return true;
    }

    private static List<DisplayKind>
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
     * Shift-clicking the finished jewelry sends it to the workshop.
     * When the empty ring or necklace came from a specialized stand,
     * that same visible slot is replaced first.
     */
    private boolean craftIntoNearbyDisplay(
            ItemStack result
    ) {
        if (result.isEmpty()) {
            return false;
        }

        boolean localGem =
                !inputContainer
                        .getItem(GEM_SLOT)
                        .isEmpty();

        boolean localJewelry =
                !inputContainer
                        .getItem(JEWELRY_SLOT)
                        .isEmpty();

        boolean crafted =
                access.evaluate(
                        (level, position) -> {
                            NearbyDisplayAccess.Source gemSource =
                                    localGem
                                            ? null
                                            : NearbyDisplayAccess
                                            .findSource(
                                                    level,
                                                    position,
                                                    List.of(
                                                            DisplayKind.CASE
                                                    ),
                                                    this::matchesRemoteGem
                                            )
                                            .orElse(null);

                            NearbyDisplayAccess.Source jewelrySource =
                                    localJewelry
                                            ? null
                                            : NearbyDisplayAccess
                                            .findSource(
                                                    level,
                                                    position,
                                                    List.of(
                                                            DisplayKind.RING,
                                                            DisplayKind.AMULET,
                                                            DisplayKind.CASE
                                                    ),
                                                    this::matchesRemoteJewelry
                                            )
                                            .orElse(null);

                            if ((!localGem
                                    && (gemSource == null
                                    || !gemSource.isStillValid()))
                                    || (!localJewelry
                                    && (jewelrySource == null
                                    || !jewelrySource.isStillValid()))) {

                                return false;
                            }

                            boolean replaceSourceSlot =
                                    jewelrySource != null
                                            && ((result.is(
                                            ModItemTags.RINGS
                                    ) && jewelrySource
                                            .display()
                                            .getDisplayKind()
                                            == DisplayKind.RING)
                                            || (result.is(
                                            ModItemTags.AMULETS
                                    ) && jewelrySource
                                            .display()
                                            .getDisplayKind()
                                            == DisplayKind.AMULET))
                                            && jewelrySource
                                            .display()
                                            .canPlaceItem(
                                                    jewelrySource.slot(),
                                                    result
                                            );

                            NearbyDisplayAccess.Destination
                                    destination =
                                    replaceSourceSlot
                                            ? null
                                            : NearbyDisplayAccess
                                            .findDestination(
                                                    level,
                                                    position,
                                                    result,
                                                    outputDestinationPriority(
                                                            result
                                                    )
                                            )
                                            .orElse(null);

                            if (!replaceSourceSlot
                                    && destination == null) {

                                return false;
                            }

                            if (localGem) {
                                inputContainer
                                        .getItem(GEM_SLOT)
                                        .shrink(1);
                            } else if (!gemSource.consumeOne()) {
                                return false;
                            }

                            if (localJewelry) {
                                inputContainer
                                        .getItem(JEWELRY_SLOT)
                                        .shrink(1);
                            } else if (!jewelrySource.consumeOne()) {
                                if (localGem) {
                                    inputContainer
                                            .getItem(GEM_SLOT)
                                            .grow(1);
                                } else {
                                    gemSource.restoreOne();
                                }

                                return false;
                            }

                            boolean inserted;

                            if (replaceSourceSlot) {
                                jewelrySource
                                        .display()
                                        .setItem(
                                                jewelrySource.slot(),
                                                result.copy()
                                        );

                                inserted =
                                        ItemStack
                                                .isSameItemSameComponents(
                                                        jewelrySource
                                                                .display()
                                                                .getItem(
                                                                        jewelrySource.slot()
                                                                ),
                                                        result
                                                );
                            } else {
                                inserted =
                                        destination.insert(result);
                            }

                            if (!inserted) {
                                if (localGem) {
                                    inputContainer
                                            .getItem(GEM_SLOT)
                                            .grow(1);
                                } else {
                                    gemSource.restoreOne();
                                }

                                if (localJewelry) {
                                    inputContainer
                                            .getItem(JEWELRY_SLOT)
                                            .grow(1);
                                } else {
                                    jewelrySource.restoreOne();
                                }

                                return false;
                            }

                            return true;
                        },
                        false
                );

        if (crafted) {
            if (localGem || localJewelry) {
                inputContainer.setChanged();
            }

            resultContainer.setItem(
                    0,
                    ItemStack.EMPTY
            );

            refreshRemoteInputs();
            setupResult();
        }

        return crafted;
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
        } else if (slotIndex == GEM_SLOT
                || slotIndex == JEWELRY_SLOT) {

            if (!moveItemStackTo(
                    slotStack,
                    PLAYER_INVENTORY_START,
                    HOTBAR_END,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        } else if (isAcceptedGem(slotStack)) {
            if (!moveItemStackTo(
                    slotStack,
                    GEM_SLOT,
                    GEM_SLOT + 1,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        } else if (isAcceptedJewelry(slotStack)) {
            if (!moveItemStackTo(
                    slotStack,
                    JEWELRY_SLOT,
                    JEWELRY_SLOT + 1,
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

    private record JewelrySet(
            Item cutGem,
            Item necklace,
            Item ring
    ) {
    }
}
