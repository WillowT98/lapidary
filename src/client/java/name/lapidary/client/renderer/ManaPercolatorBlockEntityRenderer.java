package name.lapidary.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import name.lapidary.block.ManaPercolatorBlock;
import name.lapidary.block.entity.ManaPercolatorBlockEntity;
import name.lapidary.fluid.CanisterLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renders the mana percolator's dynamic internal contents.
 *
 * The gem, fluid walls, fluid surface, and processing bubbles all belong to
 * this block-entity rendering scene. The bubbles are rendered geometry rather
 * than Particle instances, which prevents the percolator's translucent glass
 * from hiding them through Minecraft's separate particle-rendering pass.
 *
 * This class is client-only and does not alter processing, canister transfer,
 * save data, or the server ticker.
 */
public final class ManaPercolatorBlockEntityRenderer
        implements BlockEntityRenderer<ManaPercolatorBlockEntity> {

    private static final ResourceLocation BUBBLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft",
                    "textures/particle/bubble.png"
            );

    private static final double GEM_X =
            0.5D;
    private static final double GEM_Y =
            2.75D / 16.0D;
    private static final double GEM_Z =
            0.5D;

    private static final float GEM_SCALE =
            0.34F;

    private static final double BUBBLE_BOTTOM_Y =
            3.0D / 16.0D;
    private static final double BUBBLE_TOP_Y =
            11.15D / 16.0D;

    /*
     * Fixed starting locations keep the effect stable rather than making the
     * bubbles jump to unrelated positions every frame. Values are local block
     * coordinates and remain comfortably inside the glass chamber.
     */
    private static final double[] BUBBLE_X = {
            0.27D,
            0.39D,
            0.53D,
            0.67D,
            0.76D,
            0.32D,
            0.47D,
            0.61D,
            0.71D,
            0.43D
    };

    private static final double[] BUBBLE_Z = {
            0.35D,
            0.68D,
            0.43D,
            0.73D,
            0.51D,
            0.57D,
            0.29D,
            0.61D,
            0.38D,
            0.78D
    };

    private static final double[] BUBBLE_PHASE = {
            0.00D,
            0.13D,
            0.27D,
            0.38D,
            0.49D,
            0.61D,
            0.72D,
            0.81D,
            0.89D,
            0.95D
    };

    private static final double[] BUBBLE_SPEED = {
            0.0170D,
            0.0205D,
            0.0185D,
            0.0220D,
            0.0190D,
            0.0230D,
            0.0178D,
            0.0212D,
            0.0197D,
            0.0225D
    };

    private static final float[] BUBBLE_SIZE = {
            0.060F,
            0.082F,
            0.052F,
            0.074F,
            0.058F,
            0.090F,
            0.066F,
            0.050F,
            0.078F,
            0.056F
    };

    public ManaPercolatorBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    public void render(
            ManaPercolatorBlockEntity percolator,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        if (percolator.getLevel() == null) {
            return;
        }

        Direction facing =
                percolator.getBlockState()
                        .getValue(ManaPercolatorBlock.FACING);

        CanisterLiquid renderedLiquid =
                getRenderedLiquid(percolator);

        poseStack.pushPose();

        rotateToFacing(
                poseStack,
                facing
        );

        /*
         * The side walls establish the visible body of water or mana.
         * The gem and bubbles are then rendered as internal objects, followed
         * by the stronger horizontal liquid surface.
         */
        if (renderedLiquid != null) {
            PercolatorFluidRenderer.renderSideWalls(
                    renderedLiquid,
                    percolator.getLevel(),
                    percolator.getBlockPos(),
                    poseStack,
                    bufferSource,
                    packedLight,
                    packedOverlay
            );
        }

        renderGem(
                percolator,
                poseStack,
                bufferSource,
                packedLight
        );

        if (percolator.isProcessing()) {
            renderProcessingBubbles(
                    percolator,
                    partialTick,
                    poseStack,
                    bufferSource
            );
        }

        if (renderedLiquid != null) {
            PercolatorFluidRenderer.renderTopSurface(
                    renderedLiquid,
                    percolator.getLevel(),
                    percolator.getBlockPos(),
                    poseStack,
                    bufferSource,
                    packedLight,
                    packedOverlay
            );
        }

        poseStack.popPose();
    }

    private static CanisterLiquid getRenderedLiquid(
            ManaPercolatorBlockEntity percolator
    ) {
        return switch (percolator.getChamber()) {
            case EMPTY -> null;
            case WATER -> CanisterLiquid.WATER;
            case MANA -> CanisterLiquid.MANA;
        };
    }

    private static void rotateToFacing(
            PoseStack poseStack,
            Direction facing
    ) {
        poseStack.translate(
                0.5D,
                0.0D,
                0.5D
        );

        poseStack.mulPose(
                Axis.YP.rotationDegrees(
                        facing.toYRot()
                )
        );

        poseStack.translate(
                -0.5D,
                0.0D,
                -0.5D
        );
    }

    private static void renderGem(
            ManaPercolatorBlockEntity percolator,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        ItemStack gem =
                percolator.getGem();

        if (gem.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        poseStack.translate(
                GEM_X,
                GEM_Y,
                GEM_Z
        );

        poseStack.mulPose(
                Axis.XP.rotationDegrees(
                        90.0F
                )
        );

        poseStack.scale(
                GEM_SCALE,
                GEM_SCALE,
                GEM_SCALE
        );

        Minecraft.getInstance()
                .getItemRenderer()
                .renderStatic(
                        gem,
                        ItemDisplayContext.FIXED,
                        packedLight,
                        OverlayTexture.NO_OVERLAY,
                        poseStack,
                        bufferSource,
                        percolator.getLevel(),
                        0
                );

        poseStack.popPose();
    }

    /**
     * Draws a continuously looping collection of bubble sprites.
     *
     * These are crossed quads rendered through the block-entity pipeline, not
     * Particle objects. The two perpendicular planes make each bubble visible
     * from every side of the percolator without depending on camera APIs.
     */
    private static void renderProcessingBubbles(
            ManaPercolatorBlockEntity percolator,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource
    ) {
        double time =
                percolator.getLevel().getGameTime()
                        + partialTick;

        VertexConsumer consumer =
                bufferSource.getBuffer(
                        RenderType.entityCutoutNoCull(
                                BUBBLE_TEXTURE
                        )
                );

        for (int index = 0;
             index < BUBBLE_PHASE.length;
             index++) {

            double progress =
                    positiveModulo(
                            BUBBLE_PHASE[index]
                                    + time * BUBBLE_SPEED[index],
                            1.0D
                    );

            double y =
                    BUBBLE_BOTTOM_Y
                            + progress
                            * (BUBBLE_TOP_Y - BUBBLE_BOTTOM_Y);

            /*
             * Give each bubble a tiny, smooth sideways drift while preserving
             * its own recognizable column.
             */
            double driftX =
                    Math.sin(
                            time * 0.075D
                                    + index * 1.73D
                    ) * 0.012D;

            double driftZ =
                    Math.cos(
                            time * 0.063D
                                    + index * 1.29D
                    ) * 0.012D;

            float edgeScale =
                    calculateEdgeScale(progress);

            float size =
                    BUBBLE_SIZE[index]
                            * edgeScale;

            if (size <= 0.002F) {
                continue;
            }

            renderCrossedBubble(
                    poseStack,
                    consumer,
                    BUBBLE_X[index] + driftX,
                    y,
                    BUBBLE_Z[index] + driftZ,
                    size
            );
        }
    }

    private static float calculateEdgeScale(
            double progress
    ) {
        if (progress < 0.08D) {
            return (float) (progress / 0.08D);
        }

        if (progress > 0.92D) {
            return (float) ((1.0D - progress) / 0.08D);
        }

        return 1.0F;
    }

    private static double positiveModulo(
            double value,
            double modulus
    ) {
        double result =
                value % modulus;

        return result < 0.0D
                ? result + modulus
                : result;
    }

    private static void renderCrossedBubble(
            PoseStack poseStack,
            VertexConsumer consumer,
            double x,
            double y,
            double z,
            float size
    ) {
        float half =
                size * 0.5F;

        poseStack.pushPose();

        poseStack.translate(
                x,
                y,
                z
        );

        PoseStack.Pose pose =
                poseStack.last();

        /* Plane parallel to the XY axes. */
        quad(
                consumer,
                pose,
                -half, -half, 0.0F,
                half, -half, 0.0F,
                half, half, 0.0F,
                -half, half, 0.0F,
                0.0F, 1.0F,
                1.0F, 1.0F,
                1.0F, 0.0F,
                0.0F, 0.0F,
                0.0F, 0.0F, 1.0F
        );

        /* Plane parallel to the ZY axes. */
        quad(
                consumer,
                pose,
                0.0F, -half, -half,
                0.0F, -half, half,
                0.0F, half, half,
                0.0F, half, -half,
                0.0F, 1.0F,
                1.0F, 1.0F,
                1.0F, 0.0F,
                0.0F, 0.0F,
                1.0F, 0.0F, 0.0F
        );

        poseStack.popPose();
    }

    private static void quad(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4,
            float u1, float v1,
            float u2, float v2,
            float u3, float v3,
            float u4, float v4,
            float normalX,
            float normalY,
            float normalZ
    ) {
        vertex(
                consumer,
                pose,
                x1, y1, z1,
                u1, v1,
                normalX, normalY, normalZ
        );

        vertex(
                consumer,
                pose,
                x2, y2, z2,
                u2, v2,
                normalX, normalY, normalZ
        );

        vertex(
                consumer,
                pose,
                x3, y3, z3,
                u3, v3,
                normalX, normalY, normalZ
        );

        vertex(
                consumer,
                pose,
                x4, y4, z4,
                u4, v4,
                normalX, normalY, normalZ
        );
    }

    private static void vertex(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            float u,
            float v,
            float normalX,
            float normalY,
            float normalZ
    ) {
        consumer.addVertex(
                        pose,
                        x,
                        y,
                        z
                )
                .setColor(
                        255,
                        255,
                        255,
                        255
                )
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(
                        pose,
                        normalX,
                        normalY,
                        normalZ
                );
    }
}
