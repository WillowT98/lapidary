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

        if (level != null) {
            /*
             * Sends the new saved data to nearby clients so that the
             * rendered liquid level updates immediately.
             */
            level.sendBlockUpdated(
                    worldPosition,
                    getBlockState(),
                    getBlockState(),
                    Block.UPDATE_CLIENTS
            );
        }
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