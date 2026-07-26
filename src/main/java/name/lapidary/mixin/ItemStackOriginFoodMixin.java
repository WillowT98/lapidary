package name.lapidary.mixin;

import name.lapidary.origin.OriginFoodHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackOriginFoodMixin {

    @Inject(
            method = "use",
            at = @At("HEAD"),
            cancellable = true
    )
    private void lapidary$applyOriginDiet(
            Level level,
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<
                    InteractionResultHolder<ItemStack>
                    > callbackInfo
    ) {
        ItemStack stack =
                (ItemStack) (Object) this;

        InteractionResultHolder<ItemStack> result =
                OriginFoodHandler.interceptUse(
                        stack,
                        level,
                        player,
                        hand
                );

        if (result != null) {
            callbackInfo.setReturnValue(
                    result
            );
        }
    }
}
