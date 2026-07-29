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
 * Renders the percolator's dynamic contents:
 *
 * - the active gem,
 * - the chamber fluid,
 * - a horizontal, full-size water-input canister, and
 * - an upright, full-size mana-output canister.
 *
 * Local coordinates assume that the unrotated percolator faces south.
 * The outer facing transform rotates all dynamic contents together with
 * the block model.
 */
public final class ManaPercolatorBlockEntityRenderer
        implements BlockEntityRenderer<ManaPercolatorBlockEntity> {

    /*
     * The new model's chamber floor is at y = 2 pixels.
     */
    private static final double GEM_X =
            0.5D;

    private static final double GEM_Y =
            2.15D / 16.0D;

    private static final double GEM_Z =
            0.5D;

    private static final float GEM_SCALE =
            0.34F;

    /*
     * The input nozzle occupies:
     *
     * x = 5..11 pixels
     * y = 4..10 pixels
     * z = 15..16 pixels
     *
     * A canister rotated +90 degrees around X has its bottom stacking
     * plate facing back toward the nozzle. Translating to y = 15/16
     * aligns that six-pixel plate with the nozzle's y = 4..10 span.
     */
    private static final double INPUT_CANISTER_Y =
            15.0D / 16.0D;

    private static final double INPUT_CANISTER_Z =
            1.0D;

    /*
     * The top nozzle reaches the top of the percolator block, so an
     * upright canister begins exactly one block above the origin.
     */
    private static final double OUTPUT_CANISTER_Y =
            1.0D;

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

        renderInputCanister(
                percolator.getInputCanister(),
                percolator,
                poseStack,
                bufferSource,
                packedLight
        );

        renderOutputCanister(
                percolator.getOutputCanister(),
                percolator,
                poseStack,
                bufferSource,
                packedLight
        );

        poseStack.popPose();
    }

    /**
     * The mounted canisters extend beyond the percolator's normal
     * one-block bounds. Keeping the renderer active outside those bounds
     * prevents the attachments from disappearing at camera-edge angles.
     */
    @Override
    public boolean shouldRenderOffScreen(
            ManaPercolatorBlockEntity percolator
    ) {
        return true;
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

    /**
     * Renders the water-input canister horizontally against the front
     * nozzle of the south-facing base model.
     *
     * The canister model and its liquid are rendered under the same pose,
     * so the liquid rotates with the container instead of responding to
     * visual gravity.
     */
    private static void renderInputCanister(
            ItemStack canisterStack,
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
                0.0D,
                INPUT_CANISTER_Y,
                INPUT_CANISTER_Z
        );

        poseStack.mulPose(
                Axis.XP.rotationDegrees(
                        90.0F
                )
        );

        renderCanisterAtOrigin(
                canisterStack,
                percolator,
                poseStack,
                bufferSource,
                packedLight
        );

        poseStack.popPose();
    }

    /**
     * Renders the mana-output canister upright on the top nozzle.
     */
    private static void renderOutputCanister(
            ItemStack canisterStack,
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
                0.0D,
                OUTPUT_CANISTER_Y,
                0.0D
        );

        renderCanisterAtOrigin(
                canisterStack,
                percolator,
                poseStack,
                bufferSource,
                packedLight
        );

        poseStack.popPose();
    }

    /**
     * Renders a full-size canister block at the current pose origin,
     * followed by its actual stored liquid and fill level.
     */
    private static void renderCanisterAtOrigin(
            ItemStack canisterStack,
            ManaPercolatorBlockEntity percolator,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
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

        if (contents.isEmpty()) {
            return;
        }

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
}
