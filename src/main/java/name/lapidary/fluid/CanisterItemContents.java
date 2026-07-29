package name.lapidary.fluid;

import name.lapidary.block.ModBlocks;
import name.lapidary.block.entity.CanisterBlockEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Reads and changes the liquid preserved on a canister ItemStack.
 *
 * Mounted percolator canisters remain ordinary canister items, so this
 * helper bridges their BLOCK_ENTITY_DATA component and machine automation.
 */
public final class CanisterItemContents {

    private CanisterItemContents() {
    }

    public static Contents read(
            ItemStack stack
    ) {
        if (!stack.is(
                ModBlocks.CANISTER.asItem()
        )) {
            return Contents.EMPTY;
        }

        CustomData blockEntityData =
                stack.get(
                        DataComponents.BLOCK_ENTITY_DATA
                );

        if (blockEntityData == null
                || blockEntityData.isEmpty()) {

            return Contents.EMPTY;
        }

        CompoundTag tag =
                blockEntityData.copyTag();

        CanisterLiquid liquid =
                CanisterLiquid.byId(
                        tag.getString(
                                CanisterBlockEntity.LIQUID_KEY
                        )
                );

        long amount =
                tag.getLong(
                        CanisterBlockEntity.AMOUNT_KEY
                );

        if (liquid == null || amount <= 0L) {
            return Contents.EMPTY;
        }

        return new Contents(
                liquid,
                Math.min(
                        amount,
                        CanisterFluidStorage.CAPACITY
                )
        );
    }

    /**
     * Atomically removes exactly the requested amount when available.
     * Partial transfers are deliberately rejected.
     */
    public static boolean tryExtractExact(
            ItemStack stack,
            CanisterLiquid requestedLiquid,
            long requestedAmount
    ) {
        if (requestedLiquid == null
                || requestedAmount <= 0L) {

            return false;
        }

        Contents contents = read(stack);

        if (contents.liquid() != requestedLiquid
                || contents.amount() < requestedAmount) {

            return false;
        }

        write(
                stack,
                requestedLiquid,
                contents.amount() - requestedAmount
        );

        return true;
    }

    /**
     * Atomically inserts exactly the requested amount when the canister
     * is empty or already contains the same liquid and has enough room.
     */
    public static boolean tryInsertExact(
            ItemStack stack,
            CanisterLiquid insertedLiquid,
            long insertedAmount
    ) {
        if (!stack.is(ModBlocks.CANISTER.asItem())
                || insertedLiquid == null
                || insertedAmount <= 0L) {

            return false;
        }

        Contents contents = read(stack);

        if (!contents.isEmpty()
                && contents.liquid() != insertedLiquid) {

            return false;
        }

        if (CanisterFluidStorage.CAPACITY
                - contents.amount()
                < insertedAmount) {

            return false;
        }

        write(
                stack,
                insertedLiquid,
                contents.amount() + insertedAmount
        );

        return true;
    }

    private static void write(
            ItemStack stack,
            CanisterLiquid liquid,
            long amount
    ) {
        CustomData existingData =
                stack.get(
                        DataComponents.BLOCK_ENTITY_DATA
                );

        CompoundTag tag =
                existingData == null
                        ? new CompoundTag()
                        : existingData.copyTag();

        long clampedAmount =
                Math.max(
                        0L,
                        Math.min(
                                amount,
                                CanisterFluidStorage.CAPACITY
                        )
                );

        tag.putLong(
                CanisterBlockEntity.AMOUNT_KEY,
                clampedAmount
        );

        tag.putString(
                CanisterBlockEntity.LIQUID_KEY,
                clampedAmount <= 0L || liquid == null
                        ? ""
                        : liquid.id().toString()
        );

        /*
         * BLOCK_ENTITY_DATA must identify which block entity owns the
         * stored data. ItemStack serialization rejects the component
         * when this id is absent.
         */
        tag.putString(
                "id",
                "lapidary:canister"
        );

        CustomData.set(
                DataComponents.BLOCK_ENTITY_DATA,
                stack,
                tag
        );
    }

    public record Contents(
            CanisterLiquid liquid,
            long amount
    ) {
        public static final Contents EMPTY =
                new Contents(
                        null,
                        0L
                );

        public boolean isEmpty() {
            return liquid == null
                    || amount <= 0L;
        }
    }
}
