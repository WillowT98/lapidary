package name.lapidary.block.entity;

import name.lapidary.fluid.CanisterFluidStorage;
import name.lapidary.fluid.CanisterLiquid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;

public final class CanisterBlockEntity
        extends BlockEntity {

    public static final String LIQUID_KEY =
            "Liquid";

    public static final String AMOUNT_KEY =
            "Amount";

    private final CanisterFluidStorage storage =
            new CanisterFluidStorage(
                    this::onStorageChanged
            );

    public CanisterBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        super(
                ModBlockEntities.CANISTER,
                position,
                state
        );
    }

    public CanisterFluidStorage getStorage() {
        return storage;
    }


    private void onStorageChanged() {
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

        /*
         * Liquid in this canister may now be able to move into the
         * canister below it.
         *
         * This covers:
         * - adding liquid with a bucket;
         * - future machine insertion;
         * - liquid arriving from a canister above.
         */
        scheduleTransferFor(
                worldPosition
        );

        /*
         * A change to this canister may also have created room for the
         * canister immediately above it.
         *
         * This covers:
         * - withdrawing a bucket;
         * - future machine extraction;
         * - this canister draining into one below it.
         */
        scheduleTransferFor(
                worldPosition.above()
        );
    }

    private void scheduleTransferFor(
            BlockPos position
    ) {
        if (!(level instanceof ServerLevel
                serverLevel)) {

            return;
        }

        BlockState state =
                serverLevel.getBlockState(
                        position
                );

        /*
         * Schedule only another block of the same canister type.
         */
        if (!state.is(
                getBlockState().getBlock()
        )) {
            return;
        }

        serverLevel.scheduleTick(
                position,
                state.getBlock(),
                1
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

        /*
         * Always write an explicit amount so the client receives a
         * definite empty state when the final liquid is removed.
         */
        tag.putLong(
                AMOUNT_KEY,
                storage.getAmount()
        );

        if (storage.isEmpty()) {
            tag.putString(
                    LIQUID_KEY,
                    ""
            );
        } else {
            tag.putString(
                    LIQUID_KEY,
                    storage.getLiquid()
                            .id()
                            .toString()
            );
        }
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

        long loadedAmount =
                tag.getLong(
                        AMOUNT_KEY
                );

        /*
         * Explicitly clear the client-side storage when the synchronized
         * amount is zero.
         */
        if (loadedAmount <= 0L) {
            storage.loadContents(
                    null,
                    0L
            );

            return;
        }

        CanisterLiquid loadedLiquid =
                CanisterLiquid.byId(
                        tag.getString(
                                LIQUID_KEY
                        )
                );

        storage.loadContents(
                loadedLiquid,
                loadedAmount
        );
    }

    /**
     * Packet used for changes while the chunk is already loaded.
     */
    @Override
    public Packet<ClientGamePacketListener>
    getUpdatePacket() {
        return ClientboundBlockEntityDataPacket
                .create(this);
    }

    /**
     * Data sent when the client first receives this chunk.
     */
    @Override
    public CompoundTag getUpdateTag(
            HolderLookup.Provider registries
    ) {
        return saveWithoutMetadata(
                registries
        );
    }
}