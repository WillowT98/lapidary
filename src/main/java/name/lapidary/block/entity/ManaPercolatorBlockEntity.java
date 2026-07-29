package name.lapidary.block.entity;

import name.lapidary.fluid.CanisterFluidStorage;
import name.lapidary.fluid.CanisterItemContents;
import name.lapidary.fluid.CanisterLiquid;
import name.lapidary.tag.ModItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Menu-free storage and processing logic for the mana percolator.
 *
 * The chamber holds one logical bucket at a time. Mounted canisters are
 * stored as their actual ItemStacks so their existing block-entity item
 * data is preserved when mounted, changed, removed, or dropped.
 */
public final class ManaPercolatorBlockEntity
        extends BlockEntity {

    public static final int PROCESSING_DURATION_TICKS =
            20 * 20;

    public static final int CONVERSIONS_PER_GEM =
            8;

    private static final String GEM_KEY =
            "Gem";

    private static final String INPUT_CANISTER_KEY =
            "InputCanister";

    private static final String OUTPUT_CANISTER_KEY =
            "OutputCanister";

    private static final String GEM_COMMITTED_KEY =
            "GemCommitted";

    private static final String GEM_USES_KEY =
            "GemUsesRemaining";

    private static final String CHAMBER_KEY =
            "Chamber";

    private static final String PROCESSING_TICKS_KEY =
            "ProcessingTicks";

    private ItemStack gem =
            ItemStack.EMPTY;

    private ItemStack inputCanister =
            ItemStack.EMPTY;

    private ItemStack outputCanister =
            ItemStack.EMPTY;

    private boolean gemCommitted;

    private int gemUsesRemaining;

    private ChamberContents chamber =
            ChamberContents.EMPTY;

    private int processingTicks;

    public ManaPercolatorBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        super(
                ModBlockEntities.MANA_PERCOLATOR,
                position,
                state
        );
    }

    public static void serverTick(
            Level level,
            BlockPos position,
            BlockState state,
            ManaPercolatorBlockEntity percolator
    ) {
        /*
         * Finished mana leaves first. This allows the same server tick
         * to pull the next bucket of water and begin the following cycle.
         */
        if (percolator.chamber == ChamberContents.MANA
                && percolator.trySendManaToOutputCanister()) {

            percolator.chamber =
                    ChamberContents.EMPTY;

            percolator.processingTicks =
                    0;

            percolator.synchronize();
        }

        if (percolator.chamber == ChamberContents.EMPTY
                && !percolator.gem.isEmpty()
                && percolator.tryPullWaterFromInputCanister()) {

            percolator.chamber =
                    ChamberContents.WATER;

            percolator.gemCommitted =
                    true;

            percolator.processingTicks =
                    0;

            percolator.synchronize();
        }

        if (percolator.chamber != ChamberContents.WATER
                || percolator.gem.isEmpty()) {

            return;
        }

        percolator.processingTicks++;

        /*
         * Save progress once per second without sending a render packet
         * every tick. The client only needs the start and finish states.
         */
        if (percolator.processingTicks % 20 == 0) {
            percolator.setChanged();
        }

        if (percolator.processingTicks
                < PROCESSING_DURATION_TICKS) {

            return;
        }

        percolator.processingTicks =
                0;

        percolator.chamber =
                ChamberContents.MANA;

        percolator.gemUsesRemaining--;

        if (percolator.gemUsesRemaining <= 0) {
            percolator.gem =
                    ItemStack.EMPTY;

            percolator.gemUsesRemaining =
                    0;

            percolator.gemCommitted =
                    false;
        }

        percolator.synchronize();
    }

    public boolean canInsertGem(
            ItemStack testedStack
    ) {
        return gem.isEmpty()
                && chamber == ChamberContents.EMPTY
                && testedStack.is(ModItemTags.GEMS);
    }

    public boolean insertGem(
            ItemStack insertedStack
    ) {
        if (!canInsertGem(insertedStack)) {
            return false;
        }

        gem = insertedStack.copy();
        gem.setCount(1);

        gemCommitted =
                false;

        gemUsesRemaining =
                CONVERSIONS_PER_GEM;

        processingTicks =
                0;

        synchronize();

        return true;
    }

    public boolean canRemoveGem() {
        return !gem.isEmpty()
                && !gemCommitted
                && chamber == ChamberContents.EMPTY
                && processingTicks == 0;
    }

    public ItemStack removeGem() {
        if (!canRemoveGem()) {
            return ItemStack.EMPTY;
        }

        ItemStack removed =
                gem;

        gem =
                ItemStack.EMPTY;

        gemUsesRemaining =
                0;

        gemCommitted =
                false;

        synchronize();

        return removed;
    }

    public boolean canInsertWaterBucket() {
        return chamber == ChamberContents.EMPTY
                && !gem.isEmpty();
    }

    public boolean insertWaterBucket() {
        if (!canInsertWaterBucket()) {
            return false;
        }

        chamber =
                ChamberContents.WATER;

        gemCommitted =
                true;

        processingTicks =
                0;

        synchronize();

        return true;
    }

    public boolean hasFinishedMana() {
        return chamber == ChamberContents.MANA;
    }

    public boolean removeFinishedMana() {
        if (!hasFinishedMana()) {
            return false;
        }

        chamber =
                ChamberContents.EMPTY;

        processingTicks =
                0;

        synchronize();

        return true;
    }

    public boolean canMountInputCanister(
            ItemStack testedStack
    ) {
        if (!inputCanister.isEmpty()) {
            return false;
        }

        CanisterItemContents.Contents contents =
                CanisterItemContents.read(
                        testedStack
                );

        return contents.liquid()
                == CanisterLiquid.WATER
                && contents.amount()
                >= CanisterFluidStorage.BUCKET;
    }

    public boolean mountInputCanister(
            ItemStack mountedStack
    ) {
        if (!canMountInputCanister(mountedStack)) {
            return false;
        }

        inputCanister =
                singleCopy(mountedStack);

        synchronize();

        return true;
    }

    public boolean canMountOutputCanister(
            ItemStack testedStack
    ) {
        if (!outputCanister.isEmpty()) {
            return false;
        }

        if (!testedStack.is(
                name.lapidary.block.ModBlocks
                        .CANISTER.asItem()
        )) {
            return false;
        }

        CanisterItemContents.Contents contents =
                CanisterItemContents.read(
                        testedStack
                );

        if (!contents.isEmpty()
                && contents.liquid()
                != CanisterLiquid.MANA) {

            return false;
        }

        return CanisterFluidStorage.CAPACITY
                - contents.amount()
                >= CanisterFluidStorage.BUCKET;
    }

    public boolean mountOutputCanister(
            ItemStack mountedStack
    ) {
        if (!canMountOutputCanister(mountedStack)) {
            return false;
        }

        outputCanister =
                singleCopy(mountedStack);

        synchronize();

        return true;
    }

    public ItemStack removeInputCanister() {
        if (inputCanister.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack removed =
                inputCanister;

        inputCanister =
                ItemStack.EMPTY;

        synchronize();

        return removed;
    }

    public ItemStack removeOutputCanister() {
        if (outputCanister.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack removed =
                outputCanister;

        outputCanister =
                ItemStack.EMPTY;

        synchronize();

        return removed;
    }

    private boolean tryPullWaterFromInputCanister() {
        if (inputCanister.isEmpty()) {
            return false;
        }

        return CanisterItemContents.tryExtractExact(
                inputCanister,
                CanisterLiquid.WATER,
                CanisterFluidStorage.BUCKET
        );
    }

    private boolean trySendManaToOutputCanister() {
        if (outputCanister.isEmpty()) {
            return false;
        }

        return CanisterItemContents.tryInsertExact(
                outputCanister,
                CanisterLiquid.MANA,
                CanisterFluidStorage.BUCKET
        );
    }

    private static ItemStack singleCopy(
            ItemStack source
    ) {
        ItemStack copied =
                source.copy();

        copied.setCount(1);

        return copied;
    }

    /**
     * Drops mounted canisters and an unused gem when the block is broken.
     *
     * A committed gem is intentionally not dropped. Allowing it to become
     * a normal portable gem again would reset its remaining eight-use
     * budget and make gem consumption avoidable by breaking the machine.
     */
    public void dropStoredItems() {
        if (level == null || level.isClientSide) {
            return;
        }

        if (!gem.isEmpty() && !gemCommitted) {
            Block.popResource(
                    level,
                    worldPosition,
                    gem
            );
        }

        if (!inputCanister.isEmpty()) {
            Block.popResource(
                    level,
                    worldPosition,
                    inputCanister
            );
        }

        if (!outputCanister.isEmpty()) {
            Block.popResource(
                    level,
                    worldPosition,
                    outputCanister
            );
        }

        gem =
                ItemStack.EMPTY;

        inputCanister =
                ItemStack.EMPTY;

        outputCanister =
                ItemStack.EMPTY;

        setChanged();
    }

    public ItemStack getGem() {
        return gem;
    }

    public ItemStack getInputCanister() {
        return inputCanister;
    }

    public ItemStack getOutputCanister() {
        return outputCanister;
    }

    public ChamberContents getChamber() {
        return chamber;
    }

    public boolean isProcessing() {
        return chamber == ChamberContents.WATER
                && !gem.isEmpty();
    }

    public int getGemUsesRemaining() {
        return gemUsesRemaining;
    }

    public int getProcessingTicks() {
        return processingTicks;
    }

    private void synchronize() {
        setChanged();

        if (level == null) {
            return;
        }

        level.sendBlockUpdated(
                worldPosition,
                getBlockState(),
                getBlockState(),
                Block.UPDATE_CLIENTS
        );
    }

    @Override
    protected void saveAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.saveAdditional(
                tag,
                registries
        );

        saveStack(
                tag,
                GEM_KEY,
                gem,
                registries
        );

        saveStack(
                tag,
                INPUT_CANISTER_KEY,
                inputCanister,
                registries
        );

        saveStack(
                tag,
                OUTPUT_CANISTER_KEY,
                outputCanister,
                registries
        );

        tag.putBoolean(
                GEM_COMMITTED_KEY,
                gemCommitted
        );

        tag.putInt(
                GEM_USES_KEY,
                gemUsesRemaining
        );

        tag.putString(
                CHAMBER_KEY,
                chamber.serializedName
        );

        tag.putInt(
                PROCESSING_TICKS_KEY,
                processingTicks
        );
    }

    @Override
    protected void loadAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.loadAdditional(
                tag,
                registries
        );

        gem = loadStack(
                tag,
                GEM_KEY,
                registries
        );

        inputCanister = loadStack(
                tag,
                INPUT_CANISTER_KEY,
                registries
        );

        outputCanister = loadStack(
                tag,
                OUTPUT_CANISTER_KEY,
                registries
        );

        gemCommitted =
                tag.getBoolean(
                        GEM_COMMITTED_KEY
                );

        gemUsesRemaining =
                Math.max(
                        0,
                        Math.min(
                                CONVERSIONS_PER_GEM,
                                tag.getInt(
                                        GEM_USES_KEY
                                )
                        )
                );

        chamber =
                ChamberContents.byName(
                        tag.getString(
                                CHAMBER_KEY
                        )
                );

        processingTicks =
                Math.max(
                        0,
                        Math.min(
                                PROCESSING_DURATION_TICKS,
                                tag.getInt(
                                        PROCESSING_TICKS_KEY
                                )
                        )
                );

        normalizeLoadedState();
    }

    private void normalizeLoadedState() {
        if (gem.isEmpty()) {
            gemUsesRemaining =
                    0;

            gemCommitted =
                    false;

            if (chamber == ChamberContents.WATER) {
                chamber =
                        ChamberContents.EMPTY;

                processingTicks =
                        0;
            }

            return;
        }

        if (gemUsesRemaining <= 0) {
            gemUsesRemaining =
                    CONVERSIONS_PER_GEM;
        }

        if (chamber == ChamberContents.WATER
                || gemUsesRemaining
                < CONVERSIONS_PER_GEM) {

            gemCommitted =
                    true;
        }

        if (chamber != ChamberContents.WATER) {
            processingTicks =
                    0;
        }
    }

    private static void saveStack(
            CompoundTag parent,
            String key,
            ItemStack stack,
            HolderLookup.Provider registries
    ) {
        if (stack.isEmpty()) {
            return;
        }

        parent.put(
                key,
                stack.save(registries)
        );
    }

    private static ItemStack loadStack(
            CompoundTag parent,
            String key,
            HolderLookup.Provider registries
    ) {
        if (!parent.contains(key)) {
            return ItemStack.EMPTY;
        }

        return ItemStack.parseOptional(
                registries,
                parent.getCompound(key)
        );
    }

    @Override
    public Packet<ClientGamePacketListener>
    getUpdatePacket() {
        return ClientboundBlockEntityDataPacket
                .create(this);
    }

    @Override
    public CompoundTag getUpdateTag(
            HolderLookup.Provider registries
    ) {
        return saveWithoutMetadata(
                registries
        );
    }

    public enum ChamberContents {
        EMPTY("empty"),
        WATER("water"),
        MANA("mana");

        private final String serializedName;

        ChamberContents(
                String serializedName
        ) {
            this.serializedName =
                    serializedName;
        }

        private static ChamberContents byName(
                String testedName
        ) {
            for (ChamberContents contents : values()) {
                if (contents.serializedName
                        .equals(testedName)) {

                    return contents;
                }
            }

            return EMPTY;
        }
    }
}
