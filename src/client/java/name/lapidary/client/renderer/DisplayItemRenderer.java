package name.lapidary.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import name.lapidary.block.WorkshopDisplayBlock;
import name.lapidary.block.entity.DisplayStorageBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

final class DisplayItemRenderer {

    private DisplayItemRenderer() {
    }

    /**
     * Rotates dynamic contents in exactly the same way as the display's
     * horizontally-facing block model.
     */
    static void beginFacingTransform(
            DisplayStorageBlockEntity display,
            PoseStack poseStack
    ) {
        Direction facing =
                display.getBlockState()
                        .getValue(
                                WorkshopDisplayBlock.FACING
                        );

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

    /**
     * Convenience overload for displays that use uniform scaling.
     */
    static void render(
            DisplayStorageBlockEntity display,
            ItemStack stack,
            double x,
            double y,
            double z,
            float scale,
            float xRotation,
            float yRotation,
            float zRotation,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int seed
    ) {
        render(
                display,
                stack,
                x,
                y,
                z,
                scale,
                scale,
                scale,
                xRotation,
                yRotation,
                zRotation,
                poseStack,
                bufferSource,
                packedLight,
                seed
        );
    }

    /**
     * Renders a stored item with independent scale on each local axis.
     *
     * Nonuniform scaling is useful for the necklace rack: the necklace
     * sprites can be made long enough to hang from the rail without also
     * becoming too wide for eight side-by-side slots.
     */
    static void render(
            DisplayStorageBlockEntity display,
            ItemStack stack,
            double x,
            double y,
            double z,
            float scaleX,
            float scaleY,
            float scaleZ,
            float xRotation,
            float yRotation,
            float zRotation,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int seed
    ) {
        if (stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        poseStack.translate(
                x,
                y,
                z
        );

        poseStack.mulPose(
                Axis.XP.rotationDegrees(
                        xRotation
                )
        );

        poseStack.mulPose(
                Axis.YP.rotationDegrees(
                        yRotation
                )
        );

        poseStack.mulPose(
                Axis.ZP.rotationDegrees(
                        zRotation
                )
        );

        poseStack.scale(
                scaleX,
                scaleY,
                scaleZ
        );

        Minecraft.getInstance()
                .getItemRenderer()
                .renderStatic(
                        stack,
                        ItemDisplayContext.FIXED,
                        packedLight,
                        OverlayTexture.NO_OVERLAY,
                        poseStack,
                        bufferSource,
                        display.getLevel(),
                        seed
                );

        poseStack.popPose();
    }
}
