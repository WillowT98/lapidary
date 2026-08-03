package name.lapidary.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import name.lapidary.block.entity.AmuletDisplayBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public final class AmuletDisplayBlockEntityRenderer
        implements BlockEntityRenderer<
        AmuletDisplayBlockEntity
        > {

    private static final double FIRST_HOOK_X =
            0.08D;

    private static final double HOOK_SPACING =
            0.12D;

    private static final double AMULET_CENTER_Y =
            0.65D;

    private static final double AMULET_Z =
            0.50D;

    /*
     * Keep the successful Y-axis quarter-turn from the previous pass.
     * Increase the dimensions that make the necklace taller and extend
     * farther over the arm, while keeping its rack-width dimension
     * comparatively narrow so eight necklaces still fit side by side.
     */
    private static final float AMULET_SCALE_OVER_ARM =
            0.58F;

    private static final float AMULET_SCALE_VERTICAL =
            0.74F;

    private static final float AMULET_SCALE_ALONG_RACK =
            0.24F;

    public AmuletDisplayBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    public void render(
            AmuletDisplayBlockEntity display,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        poseStack.pushPose();

        DisplayItemRenderer.beginFacingTransform(
                display,
                poseStack
        );

        for (int slot = 0;
             slot < AmuletDisplayBlockEntity.SIZE;
             slot++) {

            double x =
                    FIRST_HOOK_X
                            + slot
                            * HOOK_SPACING;

            DisplayItemRenderer.render(
                    display,
                    display.getItem(slot),
                    x,
                    AMULET_CENTER_Y,
                    AMULET_Z,
                    AMULET_SCALE_OVER_ARM,
                    AMULET_SCALE_VERTICAL,
                    AMULET_SCALE_ALONG_RACK,
                    0.0F,
                    90.0F,
                    0.0F,
                    poseStack,
                    bufferSource,
                    packedLight,
                    slot
            );
        }

        poseStack.popPose();
    }
}
