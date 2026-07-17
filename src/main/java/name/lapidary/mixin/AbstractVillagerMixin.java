package name.lapidary.mixin;

import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerMixin {

    @Inject(method = "getOffers", at = @At("RETURN"))
    private void lapidary$removeEmeraldRewardTrades(
            CallbackInfoReturnable<MerchantOffers> callback
    ) {
        /*
         * AbstractVillager also includes wandering traders.
         * This guard limits Lapidary's rule to normal villagers.
         */
        Object merchant = this;

        if (!(merchant instanceof Villager)) {
            return;
        }

        MerchantOffers offers = callback.getReturnValue();

        offers.removeIf(offer ->
                offer.getResult().getItem() == Items.EMERALD
        );
    }
}