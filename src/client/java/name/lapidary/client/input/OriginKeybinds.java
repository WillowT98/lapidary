package name.lapidary.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import name.lapidary.client.origin.ClientOriginState;
import name.lapidary.network.OriginActionPayload;
import name.lapidary.origin.OriginKind;
import name.lapidary.origin.OriginManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lwjgl.glfw.GLFW;

import java.util.OptionalDouble;

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

    /*
     * The previous locally rendered Fairy height lets the client guarantee
     * a minimum descent over cliffs, even while the jump key is held.
     */
    private static double previousFairyY =
            Double.NaN;

    private OriginKeybinds() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(
                client -> {
                    if (client.player == null) {
                        jumpWasDown =
                                false;

                        previousFairyY =
                                Double.NaN;

                        return;
                    }

                    /*
                     * Apply the height rule locally before the next rendered
                     * frame. The server performs the same validation, but it
                     * no longer needs to teleport the player downward.
                     */
                    updateFairyFlight(
                            client
                    );

                    if (client.screen != null) {
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

    private static void updateFairyFlight(
            Minecraft client
    ) {
        LocalPlayer player =
                client.player;

        if (player == null
                || ClientOriginState.originKind()
                != OriginKind.FAIRY.ordinal()
                || !player.getAbilities()
                .flying) {

            previousFairyY =
                    Double.NaN;

            return;
        }

        double currentY =
                player.getY();

        /*
         * Do not interpret a dimension change, respawn or teleport as a
         * cliff that the Fairy must descend from.
         */
        if (!Double.isNaN(
                previousFairyY
        )
                && Math.abs(
                currentY - previousFairyY
        ) > 2.0D) {

            previousFairyY =
                    currentY;
        }

        OptionalDouble supportSurface =
                findFairySupportSurface(
                        player
                );

        Vec3 movement =
                player.getDeltaMovement();

        if (supportSurface.isPresent()) {
            double ceilingY =
                    supportSurface.getAsDouble()
                            + OriginManager
                            .FAIRY_MAX_FLIGHT_HEIGHT;

            /*
             * This is a local ceiling, not a server correction. The player
             * never sees herself move above the permitted height, so there
             * is no snap or rubber-band effect.
             */
            if (currentY > ceilingY) {
                player.setPos(
                        player.getX(),
                        ceilingY,
                        player.getZ()
                );

                currentY =
                        ceilingY;
            }

            if (currentY
                    >= ceilingY - 0.001D
                    && movement.y > 0.0D) {

                player.setDeltaMovement(
                        movement.x,
                        0.0D,
                        movement.z
                );
            }
        } else {
            /*
             * The Fairy crossed above terrain more than eight blocks below.
             * Keep creative flight enabled, but guarantee a slow descent
             * until a supporting surface comes back into range.
             */
            double targetY =
                    Double.isNaN(
                            previousFairyY
                    )
                            ? currentY
                            : previousFairyY
                            - OriginManager
                            .FAIRY_UNSUPPORTED_DESCENT_SPEED;

            if (currentY > targetY) {
                player.setPos(
                        player.getX(),
                        targetY,
                        player.getZ()
                );

                currentY =
                        targetY;
            }

            if (movement.y
                    > -OriginManager
                    .FAIRY_UNSUPPORTED_DESCENT_SPEED) {

                player.setDeltaMovement(
                        movement.x,
                        -OriginManager
                                .FAIRY_UNSUPPORTED_DESCENT_SPEED,
                        movement.z
                );
            }
        }

        previousFairyY =
                currentY;
    }

    private static OptionalDouble findFairySupportSurface(
            LocalPlayer player
    ) {
        double feetY =
                player.getY();

        BlockPos origin =
                BlockPos.containing(
                        player.getX(),
                        feetY,
                        player.getZ()
                );

        for (int depth = 0;
             depth <= 10;
             depth++) {

            BlockPos tested =
                    origin.below(
                            depth
                    );

            BlockState state =
                    player.level()
                            .getBlockState(
                                    tested
                            );

            VoxelShape collision =
                    state.getCollisionShape(
                            player.level(),
                            tested
                    );

            if (collision.isEmpty()) {
                continue;
            }

            double surfaceY =
                    tested.getY()
                            + collision.max(
                                    Direction.Axis.Y
                            );

            double distance =
                    feetY - surfaceY;

            if (distance >= -0.01D
                    && distance
                    <= OriginManager
                    .FAIRY_MAX_FLIGHT_HEIGHT
                    + 0.01D) {

                return OptionalDouble.of(
                        surfaceY
                );
            }
        }

        return OptionalDouble.empty();
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
