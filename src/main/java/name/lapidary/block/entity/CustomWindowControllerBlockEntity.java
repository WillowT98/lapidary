package name.lapidary.block.entity;

import name.lapidary.window.WindowDesign;
import name.lapidary.window.WindowDesignData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public final class CustomWindowControllerBlockEntity
        extends BlockEntity {

    private static final String DESIGN_TAG =
            "Design";

    private WindowDesign design;

    public CustomWindowControllerBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        super(
                ModBlockEntities.CUSTOM_WINDOW_CONTROLLER,
                position,
                state
        );
    }

    public Optional<WindowDesign> getDesign() {
        return Optional.ofNullable(
                design
        );
    }

    public void setDesign(
            WindowDesign design
    ) {
        this.design =
                design;

        setChanged();

        if (level != null) {
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

        if (design != null) {
            WindowDesignData.writeToTag(
                    tag,
                    DESIGN_TAG,
                    design
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

        design =
                WindowDesignData.readFromTag(
                        tag,
                        DESIGN_TAG
                ).orElse(null);
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
}
