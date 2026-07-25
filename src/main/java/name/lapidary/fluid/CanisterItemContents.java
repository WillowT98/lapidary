package name.lapidary.fluid;

import name.lapidary.block.ModBlocks;
import name.lapidary.block.entity.CanisterBlockEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

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