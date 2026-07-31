package name.lapidary.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import name.lapidary.block.ManaPercolatorBlock;
import name.lapidary.block.entity.ManaPercolatorBlockEntity;
import name.lapidary.fluid.CanisterLiquid;
import name.lapidary.particle.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Renders only the percolator's dynamic internal contents.
 *
 * Mounted canisters are real neighboring blocks and are rendered by their own
 * block model and CanisterBlockEntityRenderer. Bubble spawning is deliberately
 * client-only and renderer-driven; no functional server ticker is modified.
 */
public final class ManaPercolatorBlockEntityRenderer
        implements BlockEntityRenderer<ManaPercolatorBlockEntity> {

    private static final double GEM_X =
            0.5D;
    private static final double GEM_Y =
            2.75D / 16.0D;
    private static final double GEM_Z =
            0.5D;

    private static final float GEM_SCALE =
            0.34F;

    private static final double BUBBLE_MIN_XZ =
            2.65D / 16.0D;
    private static final double BUBBLE_XZ_SPAN =
            10.70D / 16.0D;
    private static final double BUBBLE_START_Y =
            2.75D / 16.0D;

    private final Map<ManaPercolatorBlockEntity, Long>
            lastBubbleSpawnTicks =
            new WeakHashMap<>();

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

        spawnProcessingBubbles(percolator);
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

    private void spawnProcessingBubbles(
            ManaPercolatorBlockEntity percolator
    ) {
        if (!percolator.isProcessing()
                || !(percolator.getLevel()
                instanceof ClientLevel clientLevel)) {

            return;
        }

        long gameTime =
                clientLevel.getGameTime();

        Long previousSpawnTick =
                lastBubbleSpawnTicks.put(
                        percolator,
                        gameTime
                );

        if (previousSpawnTick != null
                && previousSpawnTick == gameTime) {

            return;
        }

        /*
         * Two-tick cadence produces a continuous column without flooding the
         * global particle engine. Rendering may happen multiple times per game
         * tick, so the weak-map guard above is essential.
         */
        if ((gameTime & 1L) != 0L) {
            return;
        }

        RandomSource random =
                clientLevel.random;

        int particleCount =
                1 + random.nextInt(2);

        BlockPos position =
                percolator.getBlockPos();

        for (int index = 0;
             index < particleCount;
             index++) {

            double x =
                    position.getX()
                            + BUBBLE_MIN_XZ
                            + random.nextDouble()
                            * BUBBLE_XZ_SPAN;

            double y =
                    position.getY()
                            + BUBBLE_START_Y
                            + random.nextDouble()
                            * (1.4D / 16.0D);

            double z =
                    position.getZ()
                            + BUBBLE_MIN_XZ
                            + random.nextDouble()
                            * BUBBLE_XZ_SPAN;

            double velocityX =
                    (random.nextDouble() - 0.5D)
                            * 0.0035D;

            double velocityY =
                    0.020D
                            + random.nextDouble()
                            * 0.007D;

            double velocityZ =
                    (random.nextDouble() - 0.5D)
                            * 0.0035D;

            clientLevel.addParticle(
                    ModParticles.PERCOLATOR_BUBBLE,
                    x,
                    y,
                    z,
                    velocityX,
                    velocityY,
                    velocityZ
            );
        }
    }
}
