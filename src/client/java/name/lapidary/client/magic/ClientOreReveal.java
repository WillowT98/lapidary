package name.lapidary.client.magic;

import com.mojang.blaze3d.systems.RenderSystem;
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

import java.util.Optional;

/** Client-only temporary ore outlines for the Reveal Ores spell. */
public final class ClientOreReveal {
    private static final RenderType VANILLA_LINES = RenderType.lines();

    /**
     * Uses the ordinary Minecraft line shader and vertex format, but disables
     * depth testing and depth writes while this batch is drawn. That makes the
     * ore boxes visible through intervening blocks without disturbing the
     * world's depth buffer for later render passes.
     */
    private static final RenderType THROUGH_WALL_LINES = new RenderType(
            "lapidary_reveal_ores_through_walls",
            VANILLA_LINES.format(),
            VANILLA_LINES.mode(),
            VANILLA_LINES.bufferSize(),
            VANILLA_LINES.affectsCrumbling(),
            VANILLA_LINES.sortOnUpload(),
            () -> {
                VANILLA_LINES.setupRenderState();
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(false);
            },
            () -> {
                VANILLA_LINES.clearRenderState();
                RenderSystem.depthMask(true);
                RenderSystem.enableDepthTest();
            }
    ) {
        @Override
        public Optional<RenderType> outline() {
            return Optional.empty();
        }

        @Override
        public boolean isOutline() {
            return false;
        }
    };

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
        VertexConsumer lines = consumers.getBuffer(THROUGH_WALL_LINES);

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