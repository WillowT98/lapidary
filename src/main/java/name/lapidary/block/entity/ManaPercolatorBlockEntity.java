package name.lapidary.block.entity;

import name.lapidary.block.CanisterBlock;
import name.lapidary.block.ManaPercolatorBlock;
import name.lapidary.block.ModBlocks;
import name.lapidary.fluid.CanisterFluidStorage;
import name.lapidary.fluid.CanisterLiquid;
import name.lapidary.tag.ModItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
 * Controller state for the three-block mana percolator.
 *
 * The controller stores only the gem, one-bucket chamber, and progress.
 * Input and output are neighboring real CanisterBlockEntity instances.
 */
public final class ManaPercolatorBlockEntity
        extends BlockEntity {

    public static final int PROCESSING_DURATION_TICKS =
            20 * 20;

    public static final int CONVERSIONS_PER_GEM =
            8;

    private static final String GEM_KEY =
            "Gem";

    /*
     * Retained only to migrate saves made by the earlier implementation,
     * which stored mounted canisters inside this block entity.
     */
    private static final String LEGACY_INPUT_CANISTER_KEY =
            "InputCanister";

    private static final String LEGACY_OUTPUT_CANISTER_KEY =
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

    private ItemStack legacyInputCanister =
            ItemStack.EMPTY;

    private ItemStack legacyOutputCanister =
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
        percolator.returnLegacyCanisters();

        /*
         * Finished mana leaves first. This permits the same tick to pull
         * the next bucket of water and begin another cycle.
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

    private boolean tryPullWaterFromInputCanister() {
        CanisterBlockEntity inputCanister =
                getInputCanister();

        if (inputCanister == null) {
            return false;
        }

        CanisterFluidStorage storage =
                inputCanister.getStorage();

        long available =
                storage.extract(
                        CanisterLiquid.WATER,
                        CanisterFluidStorage.BUCKET,
                        true
                );

        if (available != CanisterFluidStorage.BUCKET) {
            return false;
        }

        return storage.extract(
                CanisterLiquid.WATER,
                CanisterFluidStorage.BUCKET,
                false
        ) == CanisterFluidStorage.BUCKET;
    }

    private boolean trySendManaToOutputCanister() {
        CanisterBlockEntity outputCanister =
                getOutputCanister();

        if (outputCanister == null) {
            return false;
        }

        CanisterFluidStorage storage =
                outputCanister.getStorage();

        long insertable =
                storage.insert(
                        CanisterLiquid.MANA,
                        CanisterFluidStorage.BUCKET,
                        true
                );

        if (insertable != CanisterFluidStorage.BUCKET) {
            return false;
        }

        return storage.insert(
                CanisterLiquid.MANA,
                CanisterFluidStorage.BUCKET,
                false
        ) == CanisterFluidStorage.BUCKET;
    }

    private CanisterBlockEntity getInputCanister() {
        if (level == null) {
            return null;
        }

        Direction facing =
                getBlockState().getValue(
                        ManaPercolatorBlock.FACING
                );

        BlockPos canisterPosition =
                worldPosition.relative(facing);

        BlockState canisterState =
                level.getBlockState(canisterPosition);

        if (!canisterState.is(ModBlocks.CANISTER)
                || canisterState.getValue(CanisterBlock.FACING)
                != facing.getOpposite()) {

            return null;
        }

        if (level.getBlockEntity(canisterPosition)
                instanceof CanisterBlockEntity canister) {

            return canister;
        }

        return null;
    }

    private CanisterBlockEntity getOutputCanister() {
        if (level == null) {
            return null;
        }

        BlockPos canisterPosition =
                worldPosition.above();

        BlockState canisterState =
                level.getBlockState(canisterPosition);

        if (!canisterState.is(ModBlocks.CANISTER)
                || canisterState.getValue(CanisterBlock.FACING)
                != Direction.DOWN) {

            return null;
        }

        if (level.getBlockEntity(canisterPosition)
                instanceof CanisterBlockEntity canister) {

            return canister;
        }

        return null;
    }

    /**
     * Old versions stored mounted canisters inside this block entity.
     * Return those exact item stacks once after the updated chunk begins
     * ticking, allowing the player to remount them as real blocks.
     */
    private void returnLegacyCanisters() {
        if (level == null || level.isClientSide) {
            return;
        }

        boolean changed =
                false;

        if (!legacyInputCanister.isEmpty()) {
            Block.popResource(
                    level,
                    worldPosition,
                    legacyInputCanister
            );

            legacyInputCanister =
                    ItemStack.EMPTY;

            changed =
                    true;
        }

        if (!legacyOutputCanister.isEmpty()) {
            Block.popResource(
                    level,
                    worldPosition,
                    legacyOutputCanister
            );

            legacyOutputCanister =
                    ItemStack.EMPTY;

            changed =
                    true;
        }

        if (changed) {
            synchronize();
        }
    }

    /**
     * Drops an unused gem when the controller is broken. A committed gem
     * remains consumed by the machine so breaking the controller cannot
     * reset its remaining-use budget.
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

        if (!legacyInputCanister.isEmpty()) {
            Block.popResource(
                    level,
                    worldPosition,
                    legacyInputCanister
            );
        }

        if (!legacyOutputCanister.isEmpty()) {
            Block.popResource(
                    level,
                    worldPosition,
                    legacyOutputCanister
            );
        }

        gem =
                ItemStack.EMPTY;

        legacyInputCanister =
                ItemStack.EMPTY;

        legacyOutputCanister =
                ItemStack.EMPTY;

        setChanged();
    }

    public ItemStack getGem() {
        return gem;
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
        super.saveAdditional(tag, registries);

        saveStack(
                tag,
                GEM_KEY,
                gem,
                registries
        );

        saveStack(
                tag,
                LEGACY_INPUT_CANISTER_KEY,
                legacyInputCanister,
                registries
        );

        saveStack(
                tag,
                LEGACY_OUTPUT_CANISTER_KEY,
                legacyOutputCanister,
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
        super.loadAdditional(tag, registries);

        gem = loadStack(
                tag,
                GEM_KEY,
                registries
        );

        legacyInputCanister = loadStack(
                tag,
                LEGACY_INPUT_CANISTER_KEY,
                registries
        );

        legacyOutputCanister = loadStack(
                tag,
                LEGACY_OUTPUT_CANISTER_KEY,
                registries
        );

        gemCommitted =
                tag.getBoolean(GEM_COMMITTED_KEY);

        gemUsesRemaining =
                Math.max(
                        0,
                        Math.min(
                                CONVERSIONS_PER_GEM,
                                tag.getInt(GEM_USES_KEY)
                        )
                );

        chamber =
                ChamberContents.byName(
                        tag.getString(CHAMBER_KEY)
                );

        processingTicks =
                Math.max(
                        0,
                        Math.min(
                                PROCESSING_DURATION_TICKS,
                                tag.getInt(PROCESSING_TICKS_KEY)
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
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(
            HolderLookup.Provider registries
    ) {
        return saveWithoutMetadata(registries);
    }

    public enum ChamberContents {
        EMPTY("empty"),
        WATER("water"),
        MANA("mana");

        private final String serializedName;

        ChamberContents(String serializedName) {
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
