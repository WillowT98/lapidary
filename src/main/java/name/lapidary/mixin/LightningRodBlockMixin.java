package name.lapidary.mixin;

import name.lapidary.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningRodBlock.class)
public abstract class LightningRodBlockMixin {

    @Inject(method = "onLightningStrike", at = @At("TAIL"))
    private void lapidary$transformChestContents(
            BlockState state,
            Level level,
            BlockPos lightningRodPos,
            CallbackInfo callback
    ) {
        /*
         * Inventory changes should only be performed by the logical server.
         */
        if (level.isClientSide) {
            return;
        }

        BlockPos chestPos = lightningRodPos.below();
        BlockEntity blockEntity = level.getBlockEntity(chestPos);

        if (!(blockEntity instanceof ChestBlockEntity chest)) {
            return;
        }

        boolean changed = false;

        for (int slot = 0; slot < chest.getContainerSize(); slot++) {
            ItemStack stack = chest.getItem(slot);

            /*
             * Temporary test transformation:
             * Copper ingots become amethyst shards.
             */
            if (stack.is(ModItems.ELECTROSTATIC_MIX)) {
                int count = stack.getCount();

                chest.setItem(
                        slot,
                        new ItemStack(ModItems.FULGURITE, count)
                );

                changed = true;
            }
        }

        if (changed) {
            chest.setChanged();
        }
    }
}