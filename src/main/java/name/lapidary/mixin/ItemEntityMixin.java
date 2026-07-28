package name.lapidary.mixin;

import name.lapidary.block.ModBlocks;
import name.lapidary.item.ModItems;
import name.lapidary.tag.ModFluidTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    /*
     * Check four times per second rather than every tick.
     *
     * The inexpensive item check still happens first, so ordinary
     * dropped items never perform a fluid lookup.
     */
    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void lapidary$transformItemsInMana(
            CallbackInfo callbackInfo
    ) {
        ItemEntity itemEntity =
                (ItemEntity) (Object) this;

        /*
         * Transformations must happen only on the logical server.
         * The resulting ItemStack will synchronize to clients.
         */
        if (itemEntity.level().isClientSide()) {
            return;
        }

        ItemStack currentStack =
                itemEntity.getItem();

        boolean isLoam =
                currentStack.is(ModBlocks.LOAM.asItem());

        boolean isSableFur =
                currentStack.is(ModItems.SABLE_FUR);

        /*
         * Ordinary dropped items stop here without checking fluids.
         */
        if (!isLoam && !isSableFur) {
            return;
        }

        if (itemEntity.tickCount % 5 != 0) {
            return;
        }

        if (!lapidary$isTouchingMana(itemEntity)) {
            return;
        }

        /*
         * Transform the entire dropped stack one-for-one.
         *
         * Reusing the existing ItemEntity preserves its position,
         * velocity, age, and pickup delay.
         */
        ItemStack transformedStack =
                new ItemStack(
                        isLoam
                                ? ModItems.HEARTROOT
                                : ModItems.MANA_FUR,
                        currentStack.getCount()
                );

        itemEntity.setItem(transformedStack);
    }

    /**
     * Checks the entity's current block and the block immediately
     * below it.
     *
     * Dropped items float near a liquid surface, so their center can
     * occasionally be just above the actual fluid block.
     */
    private static boolean lapidary$isTouchingMana(
            ItemEntity itemEntity
    ) {
        FluidState currentFluid =
                itemEntity.level()
                        .getFluidState(
                                itemEntity.blockPosition()
                        );

        if (currentFluid.is(ModFluidTags.MANA)) {
            return true;
        }

        FluidState fluidBelow =
                itemEntity.level()
                        .getFluidState(
                                itemEntity.blockPosition()
                                        .below()
                        );

        return fluidBelow.is(ModFluidTags.MANA);
    }
}