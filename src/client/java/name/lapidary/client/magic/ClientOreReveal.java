package name.lapidary.client.magic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import name.lapidary.network.RevealOresPayload;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Client-only temporary ore outlines for the Reveal Ores spell. */
public final class ClientOreReveal {
    private static long[] positions = new long[0];
    private static long expiresAt;
    private static boolean initialized;

    private ClientOreReveal() {
    }

    public static void apply(RevealOresPayload payload) {
        initialize();
        positions = payload.packedPositions();

        Minecraft client = Minecraft.getInstance();
        expiresAt = client.level == null
                ? 0L
                : client.level.getGameTime()
                + payload.durationTicks();
    }

    public static void clear() {
        positions = new long[0];
        expiresAt = 0L;
    }

    private static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(
                ClientOreReveal::render
        );
    }

    private static void render(WorldRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null
                || positions.length == 0
                || client.level.getGameTime() >= expiresAt) {
            clear();
            return;
        }

        PoseStack poseStack = context.matrixStack();
        MultiBufferSource consumers = context.consumers();
        if (poseStack == null || consumers == null) {
            return;
        }

        Vec3 camera = context.camera().getPosition();
        VertexConsumer lines = consumers.getBuffer(RenderType.lines());

        poseStack.pushPose();
        poseStack.translate(
                -camera.x,
                -camera.y,
                -camera.z
        );

        for (long packedPosition : positions) {
            BlockPos pos = BlockPos.of(packedPosition);
            AABB box = new AABB(pos).inflate(0.0125D);
            LevelRenderer.renderLineBox(
                    poseStack,
                    lines,
                    box,
                    0.15F,
                    0.95F,
                    1.0F,
                    1.0F
            );
        }

        poseStack.popPose();
    }
}
