package name.lapidary.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import name.lapidary.network.OriginActionPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class OriginKeybinds {

    private static final KeyMapping ACTIVE =
            register(
                    "key.lapidary.origin_active",
                    GLFW.GLFW_KEY_V
            );

    private static final KeyMapping MAGIC =
            register(
                    "key.lapidary.origin_magic",
                    GLFW.GLFW_KEY_G
            );

    private static final KeyMapping VOCALIZE =
            register(
                    "key.lapidary.origin_vocalize",
                    GLFW.GLFW_KEY_N
            );

    private static boolean jumpWasDown;

    private OriginKeybinds() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(
                client -> {
                    if (client.player == null
                            || client.screen != null) {

                        jumpWasDown =
                                false;

                        return;
                    }

                    while (ACTIVE.consumeClick()) {
                        send(
                                OriginActionPayload.ACTIVE
                        );
                    }

                    while (MAGIC.consumeClick()) {
                        send(
                                OriginActionPayload.MAGIC
                        );
                    }

                    while (VOCALIZE.consumeClick()) {
                        send(
                                OriginActionPayload.VOCALIZE
                        );
                    }

                    boolean jumpDown =
                            client.options
                                    .keyJump
                                    .isDown();

                    if (jumpDown
                            && !jumpWasDown
                            && !client.player
                            .onGround()) {

                        send(
                                OriginActionPayload.FLAP
                        );
                    }

                    jumpWasDown =
                            jumpDown;
                }
        );
    }

    private static KeyMapping register(
            String key,
            int defaultKey
    ) {
        return KeyBindingHelper.registerKeyBinding(
                new KeyMapping(
                        key,
                        InputConstants.Type.KEYSYM,
                        defaultKey,
                        "key.categories.lapidary"
                )
        );
    }

    private static void send(
            int action
    ) {
        if (!ClientPlayNetworking.canSend(
                OriginActionPayload.TYPE
        )) {
            return;
        }

        ClientPlayNetworking.send(
                new OriginActionPayload(
                        action
                )
        );
    }
}
