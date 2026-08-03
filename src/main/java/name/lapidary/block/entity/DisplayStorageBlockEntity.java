package name.lapidary.block.entity;

import name.lapidary.display.DisplayKind;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class DisplayStorageBlockEntity
        extends BlockEntity
        implements Container, MenuProvider {

    private final NonNullList<ItemStack> items;
    private final int slotStackLimit;

    protected DisplayStorageBlockEntity(
            BlockEntityType<?> type,
            BlockPos position,
            BlockState state,
            int size,
            int slotStackLimit
    ) {
        super(
                type,
                position,
                state
        );

        this.items =
                NonNullList.withSize(
                        size,
                        ItemStack.EMPTY
                );

        this.slotStackLimit =
                slotStackLimit;
    }

    public abstract DisplayKind getDisplayKind();

    protected abstract boolean acceptsItem(
            ItemStack stack
    );

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getItem(
            int slot
    ) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(
            int slot,
            int amount
    ) {
        ItemStack removed =
                ContainerHelper.removeItem(
                        items,
                        slot,
                        amount
                );

        if (!removed.isEmpty()) {
            setChanged();
        }

        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(
            int slot
    ) {
        return ContainerHelper.takeItem(
                items,
                slot
        );
    }

    @Override
    public void setItem(
            int slot,
            ItemStack stack
    ) {
        if (!stack.isEmpty()
                && !canPlaceItem(slot, stack)) {

            return;
        }

        ItemStack stored =
                stack.copy();

        int maximum =
                Math.min(
                        slotStackLimit,
                        stored.getMaxStackSize()
                );

        if (stored.getCount() > maximum) {
            stored.setCount(maximum);
        }

        items.set(
                slot,
                stored
        );

        setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return slotStackLimit;
    }

    @Override
    public boolean canPlaceItem(
            int slot,
            ItemStack stack
    ) {
        return acceptsItem(stack);
    }

    public boolean canInsertIntoSlot(
            int slot,
            ItemStack offered
    ) {
        if (offered.isEmpty()
                || !canPlaceItem(slot, offered)) {

            return false;
        }

        ItemStack current =
                getItem(slot);

        if (current.isEmpty()) {
            return true;
        }

        if (!ItemStack.isSameItemSameComponents(
                current,
                offered
        )) {
            return false;
        }

        int maximum =
                Math.min(
                        slotStackLimit,
                        offered.getMaxStackSize()
                );

        return current.getCount() < maximum;
    }

    /**
     * Inserts into one known slot and returns the uninserted remainder.
     */
    public ItemStack insertIntoSlot(
            int slot,
            ItemStack offered,
            boolean simulate
    ) {
        if (!canInsertIntoSlot(
                slot,
                offered
        )) {
            return offered.copy();
        }

        ItemStack current =
                getItem(slot);

        int maximum =
                Math.min(
                        slotStackLimit,
                        offered.getMaxStackSize()
                );

        int room =
                current.isEmpty()
                        ? maximum
                        : maximum
                        - current.getCount();

        int inserted =
                Math.min(
                        room,
                        offered.getCount()
                );

        if (!simulate) {
            if (current.isEmpty()) {
                items.set(
                        slot,
                        offered.copyWithCount(
                                inserted
                        )
                );
            } else {
                current.grow(inserted);
            }

            setChanged();
        }

        ItemStack remainder =
                offered.copy();

        remainder.shrink(inserted);

        return remainder;
    }

    /**
     * Inserts into the first compatible slots in display order.
     */
    public ItemStack insertAnywhere(
            ItemStack offered,
            boolean simulate
    ) {
        ItemStack remainder =
                offered.copy();

        for (int slot = 0;
             slot < getContainerSize()
                     && !remainder.isEmpty();
             slot++) {

            remainder =
                    insertIntoSlot(
                            slot,
                            remainder,
                            simulate
                    );
        }

        return remainder;
    }

    @Override
    public boolean stillValid(
            Player player
    ) {
        if (level == null
                || level.getBlockEntity(
                worldPosition
        ) != this) {

            return false;
        }

        return player.distanceToSqr(
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D
        ) <= 64.0D;
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();

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

        ContainerHelper.saveAllItems(
                tag,
                items,
                registries
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

        items.clear();

        ContainerHelper.loadAllItems(
                tag,
                items,
                registries
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
}
