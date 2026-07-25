package name.lapidary.client.mixin;

import name.lapidary.magic.focus.SpellcastingFocusHelper;
import name.lapidary.network.CastSelectedSpellPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftAttackMixin {

    @Shadow
    public LocalPlayer player;

    @Shadow
    public MultiPlayerGameMode gameMode;

    /**
     * Handles the initial attack-button press.
     */
    @Inject(
            method = "startAttack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void lapidary$castWithStaff(
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (player == null
                || !SpellcastingFocusHelper
                .isHoldingFocus(player)) {

            return;
        }

        ClientPlayNetworking.send(
                CastSelectedSpellPayload.INSTANCE
        );

        /*
         * A staff left-click casts instead of attacking or beginning
         * to break a block.
         */
        callback.setReturnValue(false);
    }

    /**
     * Vanilla separately continues block breaking while the attack
     * button remains held. Cancel that continuation while holding a
     * staff so mining cannot begin on the following tick.
     */
    @Inject(
            method = "continueAttack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void lapidary$preventStaffMining(
            boolean attackHeld,
            CallbackInfo callback
    ) {
        if (player == null
                || !SpellcastingFocusHelper
                .isHoldingFocus(player)) {

            return;
        }

        if (gameMode != null) {
            gameMode.stopDestroyBlock();
        }

        callback.cancel();
    }
}