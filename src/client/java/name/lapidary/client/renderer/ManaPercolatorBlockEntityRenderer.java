package name.lapidary.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import name.lapidary.block.ManaPercolatorBlock;
import name.lapidary.block.entity.ManaPercolatorBlockEntity;
import name.lapidary.fluid.CanisterLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renders only the percolator's dynamic internal contents.
 *
 * Mounted canisters are now real neighboring blocks and are rendered by
 * their own block model and CanisterBlockEntityRenderer.
 */
public final class ManaPercolatorBlockEntityRenderer
        implements BlockEntityRenderer<ManaPercolatorBlockEntity> {

    private static final double GEM_X =
            0.5D;

    private static final double GEM_Y =
            2.15D / 16.0D;

    private static final double GEM_Z =
            0.5D;

    private static final float GEM_SCALE =
            0.34F;

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

        poseStack.pushPose();

        rotateToFacing(poseStack, facing);

        renderGem(
                percolator,
                poseStack,
                bufferSource,
                packedLight
        );

        renderChamberFluid(
                percolator,
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay
        );

        poseStack.popPose();
    }

    private static void rotateToFacing(
            PoseStack poseStack,
            Direction facing
    ) {
        poseStack.translate(0.5D, 0.0D, 0.5D);

        poseStack.mulPose(
                Axis.YP.rotationDegrees(
                        facing.toYRot()
                )
        );

        poseStack.translate(-0.5D, 0.0D, -0.5D);
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
                Axis.XP.rotationDegrees(90.0F)
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

    private static void renderChamberFluid(
            ManaPercolatorBlockEntity percolator,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        CanisterLiquid renderedLiquid =
                switch (percolator.getChamber()) {
                    case EMPTY -> null;
                    case WATER -> CanisterLiquid.WATER;
                    case MANA -> CanisterLiquid.MANA;
                };

        if (renderedLiquid == null) {
            return;
        }

        PercolatorFluidRenderer.render(
                renderedLiquid,
                percolator.getLevel(),
                percolator.getBlockPos(),
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay
        );
    }
}
