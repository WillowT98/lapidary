package name.lapidary.mixin;

import name.lapidary.magic.HardenedBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Applies the same position-based mining multiplier on both sides. */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class HardenedBlockMixin {
    @Inject(
            method = "getDestroyProgress",
            at = @At("RETURN"),
            cancellable = true
    )
    private void lapidary$slowHardenedMining(
            Player player,
            BlockGetter level,
            BlockPos pos,
            CallbackInfoReturnable<Float> callback
    ) {
        if (!HardenedBlocks.isHardened(player.level(), pos)) {
            return;
        }

        callback.setReturnValue(
                callback.getReturnValueF()
                        / HardenedBlocks.MINING_MULTIPLIER
        );
    }
}
