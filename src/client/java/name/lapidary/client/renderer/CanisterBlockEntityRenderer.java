package name.lapidary.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import name.lapidary.block.CanisterBlock;
import name.lapidary.block.entity.CanisterBlockEntity;
import name.lapidary.fluid.CanisterFluidStorage;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

/**
 * Adds the liquid inside a canister JSON model.
 *
 * The JSON blockstate rotates horizontal percolator canisters. A block
 * entity renderer does not inherit that model rotation automatically, so
 * the same orientation is applied here to the liquid cuboid.
 */
public final class CanisterBlockEntityRenderer
        implements BlockEntityRenderer<CanisterBlockEntity> {

    public CanisterBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    public void render(
            CanisterBlockEntity canister,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        CanisterFluidStorage storage =
                canister.getStorage();

        if (storage.isEmpty()
                || canister.getLevel() == null) {

            return;
        }

        Direction attachmentDirection =
                canister.getBlockState()
                        .getValue(CanisterBlock.FACING);

        poseStack.pushPose();

        rotateForAttachment(
                poseStack,
                attachmentDirection
        );

        CanisterLiquidRenderer.render(
                storage.getLiquid(),
                storage.getAmount(),
                canister.getLevel(),
                canister.getBlockPos(),
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay
        );

        poseStack.popPose();
    }

    private static void rotateForAttachment(
            PoseStack poseStack,
            Direction attachmentDirection
    ) {
        if (attachmentDirection == Direction.DOWN) {
            return;
        }

        poseStack.translate(0.5D, 0.5D, 0.5D);

        switch (attachmentDirection) {
            case UP -> poseStack.mulPose(
                    Axis.XP.rotationDegrees(180.0F)
            );

            case NORTH -> poseStack.mulPose(
                    Axis.XP.rotationDegrees(90.0F)
            );

            case SOUTH -> poseStack.mulPose(
                    Axis.XP.rotationDegrees(-90.0F)
            );

            case EAST -> poseStack.mulPose(
                    Axis.ZP.rotationDegrees(90.0F)
            );

            case WEST -> poseStack.mulPose(
                    Axis.ZP.rotationDegrees(-90.0F)
            );

            case DOWN -> {
                // Already returned above.
            }
        }

        poseStack.translate(-0.5D, -0.5D, -0.5D);
    }
}
