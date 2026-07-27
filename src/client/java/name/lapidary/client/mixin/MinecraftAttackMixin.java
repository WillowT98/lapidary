package name.lapidary.client.mixin;

import name.lapidary.magic.focus.SpellcastingFocusHelper;
import name.lapidary.network.CastSelectedSpellPayload;
import name.lapidary.network.ChannelSelectedSpellPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftAttackMixin {
    @Shadow public LocalPlayer player;
    @Shadow public MultiPlayerGameMode gameMode;

    @Unique
    private int lapidary$channelTicks;

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void lapidary$castWithStaff(
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (player == null || !SpellcastingFocusHelper.isHoldingFocus(player)) {
            return;
        }

        lapidary$channelTicks = 0;
        ClientPlayNetworking.send(CastSelectedSpellPayload.INSTANCE);
        callback.setReturnValue(false);
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void lapidary$channelWithStaff(
            boolean attackHeld,
            CallbackInfo callback
    ) {
        if (player == null || !SpellcastingFocusHelper.isHoldingFocus(player)) {
            lapidary$channelTicks = 0;
            return;
        }

        if (gameMode != null) {
            gameMode.stopDestroyBlock();
        }

        if (attackHeld) {
            lapidary$channelTicks++;
            if (lapidary$channelTicks % 5 == 0) {
                ClientPlayNetworking.send(ChannelSelectedSpellPayload.INSTANCE);
            }
        } else {
            lapidary$channelTicks = 0;
        }

        callback.cancel();
    }
}
