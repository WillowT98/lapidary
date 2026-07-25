package name.lapidary.item;

import name.lapidary.magic.PlayerMagic;
import name.lapidary.magic.focus.SpellcastingFocus;
import name.lapidary.network.OpenSpellRadialPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class StaffItem
        extends Item
        implements SpellcastingFocus {

    public StaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack =
                player.getItemInHand(hand);

        /*
         * Staff casting currently uses the main hand, so the radial
         * should configure that same casting focus.
         */
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(
                    stack
            );
        }

        if (!level.isClientSide()
                && player
                instanceof ServerPlayer serverPlayer
                && ServerPlayNetworking.canSend(
                serverPlayer,
                OpenSpellRadialPayload.TYPE
        )) {
            /*
             * Send the latest authoritative loadout first. Payloads
             * sent over the same connection retain their order.
             */
            PlayerMagic.sync(
                    serverPlayer
            );

            ServerPlayNetworking.send(
                    serverPlayer,
                    OpenSpellRadialPayload.INSTANCE
            );
        }

        return InteractionResultHolder.sidedSuccess(
                stack,
                level.isClientSide()
        );
    }
}