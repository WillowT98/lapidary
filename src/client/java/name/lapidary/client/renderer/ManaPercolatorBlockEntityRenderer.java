package name.lapidary.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import name.lapidary.block.ManaPercolatorBlock;
import name.lapidary.block.ModBlocks;
import name.lapidary.block.entity.ManaPercolatorBlockEntity;
import name.lapidary.fluid.CanisterItemContents;
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
 * Temporary dynamic-content renderer. All positions are centralized here
 * so they can be adjusted to the future Blockbench model without changing
 * the percolator's storage or processing code.
 */
public final class ManaPercolatorBlockEntityRenderer
        implements BlockEntityRenderer<
        ManaPercolatorBlockEntity
        > {

    private static final double GEM_X =
            0.5D;

    private static final double GEM_Y =
            0.285D;

    private static final double GEM_Z =
            0.5D;

    private static final float GEM_SCALE =
            0.34F;

    /*
     * Local coordinates assume the unrotated machine faces south.
     * X near one is the machine's visual-left side.
     */
    private static final double INPUT_CANISTER_X =
            0.92D;

    private static final double INPUT_CANISTER_Y =
            0.44D;

    private static final double INPUT_CANISTER_Z =
            0.50D;

    private static final float INPUT_CANISTER_SCALE =
            0.42F;

    private static final double OUTPUT_CANISTER_X =
            0.50D;

    private static final double OUTPUT_CANISTER_Y =
            1.03D;

    private static final double OUTPUT_CANISTER_Z =
            0.50D;

    private static final float OUTPUT_CANISTER_SCALE =
            0.42F;

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
                        .getValue(
                                ManaPercolatorBlock.FACING
                        );

        poseStack.pushPose();

        rotateToFacing(
                poseStack,
                facing
        );

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

        renderMountedCanister(
                percolator.getInputCanister(),
                INPUT_CANISTER_X,
                INPUT_CANISTER_Y,
                INPUT_CANISTER_Z,
                INPUT_CANISTER_SCALE,
                percolator,
                poseStack,
                bufferSource,
                packedLight
        );

        renderMountedCanister(
                percolator.getOutputCanister(),
                OUTPUT_CANISTER_X,
                OUTPUT_CANISTER_Y,
                OUTPUT_CANISTER_Z,
                OUTPUT_CANISTER_SCALE,
                percolator,
                poseStack,
                bufferSource,
                packedLight
        );

        poseStack.popPose();
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

    private void renderGem(
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

    private void renderMountedCanister(
            ItemStack canisterStack,
            double x,
            double y,
            double z,
            float scale,
            ManaPercolatorBlockEntity percolator,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        if (canisterStack.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        poseStack.translate(
                x,
                y,
                z
        );

        poseStack.scale(
                scale,
                scale,
                scale
        );

        poseStack.translate(
                -0.5D,
                -0.5D,
                -0.5D
        );

        Minecraft.getInstance()
                .getBlockRenderer()
                .renderSingleBlock(
                        ModBlocks.CANISTER
                                .defaultBlockState(),
                        poseStack,
                        bufferSource,
                        packedLight,
                        OverlayTexture.NO_OVERLAY
                );

        CanisterItemContents.Contents contents =
                CanisterItemContents.read(
                        canisterStack
                );

        if (!contents.isEmpty()) {
            CanisterLiquidRenderer.render(
                    contents.liquid(),
                    contents.amount(),
                    percolator.getLevel(),
                    percolator.getBlockPos(),
                    poseStack,
                    bufferSource,
                    packedLight,
                    OverlayTexture.NO_OVERLAY
            );
        }

        poseStack.popPose();
    }
}
