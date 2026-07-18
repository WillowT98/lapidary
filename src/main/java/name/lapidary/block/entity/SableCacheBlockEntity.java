package name.lapidary.block.entity;

import name.lapidary.entity.SableEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SableCacheBlockEntity
        extends ChestBlockEntity {

    private static final String LINKED_SABLE_KEY =
            "LinkedSable";

    /*
     * The sable that created this cache.
     */
    private UUID linkedSableUuid;

    /*
     * Each player receives their own snapshot when opening the cache.
     *
     * This map is temporary runtime data and does not need to be saved.
     */
    private final Map<UUID, List<ItemStack>>
            openingSnapshots =
            new HashMap<>();

    public SableCacheBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        /*
         * ChestBlockEntity has a protected constructor specifically
         * allowing subclasses to provide another block-entity type.
         */
        super(
                ModBlockEntities.SABLE_CACHE,
                position,
                state
        );
    }

    public UUID getLinkedSableUuid() {
        return linkedSableUuid;
    }

    public void setLinkedSableUuid(
            UUID linkedSableUuid
    ) {
        this.linkedSableUuid =
                linkedSableUuid;

        setChanged();
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable(
                "container.lapidary.sable_cache"
        );
    }

    @Override
    public void startOpen(
            Player player
    ) {
        super.startOpen(player);

        if (!player.level().isClientSide) {
            /*
             * Do not overwrite an existing snapshot if Minecraft calls
             * startOpen more than once during the same session.
             */
            openingSnapshots.putIfAbsent(
                    player.getUUID(),
                    createInventorySnapshot()
            );
        }
    }

    @Override
    public void stopOpen(
            Player player
    ) {
        if (!player.level().isClientSide) {
            List<ItemStack> originalInventory =
                    openingSnapshots.remove(
                            player.getUUID()
                    );

            if (originalInventory != null
                    && wasAnythingRemoved(
                    originalInventory
            )) {

                notifySableOfTheft(player);
            }
        }

        super.stopOpen(player);
    }

    /**
     * Called when a player physically destroys the cache block.
     */
    public void onBrokenBy(
            Player player
    ) {
        notifySableOfDestruction(player);
    }

    private List<ItemStack>
    createInventorySnapshot() {

        List<ItemStack> snapshot =
                new ArrayList<>(
                        getContainerSize()
                );

        for (int slot = 0;
             slot < getContainerSize();
             slot++) {

            snapshot.add(
                    getItem(slot).copy()
            );
        }

        return snapshot;
    }

    /**
     * Returns true if the final cache inventory contains fewer of any
     * original item than it contained when the player opened it.
     *
     * Moving an item between cache slots does not count as theft.
     * Taking an item and replacing it with a different item does.
     */
    private boolean wasAnythingRemoved(
            List<ItemStack> originalInventory
    ) {
        for (ItemStack original
                : originalInventory) {

            if (original.isEmpty()) {
                continue;
            }

            int originalCount =
                    countMatchingInSnapshot(
                            originalInventory,
                            original
                    );

            int currentCount =
                    countMatchingInCurrentInventory(
                            original
                    );

            if (currentCount < originalCount) {
                return true;
            }
        }

        return false;
    }

    private static int countMatchingInSnapshot(
            List<ItemStack> inventory,
            ItemStack reference
    ) {
        int total = 0;

        for (ItemStack stack : inventory) {
            if (ItemStack
                    .isSameItemSameComponents(
                            stack,
                            reference
                    )) {

                total += stack.getCount();
            }
        }

        return total;
    }

    private int countMatchingInCurrentInventory(
            ItemStack reference
    ) {
        int total = 0;

        for (int slot = 0;
             slot < getContainerSize();
             slot++) {

            ItemStack stack =
                    getItem(slot);

            if (ItemStack
                    .isSameItemSameComponents(
                            stack,
                            reference
                    )) {

                total += stack.getCount();
            }
        }

        return total;
    }

    private void notifySableOfTheft(
            Player player
    ) {
        SableEntity sable =
                findLinkedSable(player);

        if (sable != null) {
            sable.onCacheStolenFrom(
                    player
            );
        }
    }

    private void notifySableOfDestruction(
            Player player
    ) {
        SableEntity sable =
                findLinkedSable(player);

        if (sable != null) {
            sable.onCacheDestroyedBy(
                    player
            );
        }
    }

    private SableEntity findLinkedSable(
            Player player
    ) {
        if (linkedSableUuid == null
                || !(player.level()
                instanceof ServerLevel serverLevel)) {

            return null;
        }

        Entity entity =
                serverLevel.getEntity(
                        linkedSableUuid
                );

        if (entity
                instanceof SableEntity sable) {
            return sable;
        }

        return null;
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

        if (linkedSableUuid != null) {
            tag.putUUID(
                    LINKED_SABLE_KEY,
                    linkedSableUuid
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

        if (tag.hasUUID(
                LINKED_SABLE_KEY
        )) {
            linkedSableUuid =
                    tag.getUUID(
                            LINKED_SABLE_KEY
                    );
        } else {
            linkedSableUuid = null;
        }
    }
}