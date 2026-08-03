package name.lapidary.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import name.lapidary.block.entity.DisplayCaseBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public final class DisplayCaseBlockEntityRenderer
        implements BlockEntityRenderer<
        DisplayCaseBlockEntity
        > {

    private static final double FIRST_COLUMN_X =
            0.10D;

    private static final double COLUMN_SPACING =
            0.10D;

    private static final double FIRST_ROW_Z =
            0.22D;

    private static final double ROW_SPACING =
            0.28D;

    /*
     * Preserve the previous pass's X/Z positions and correctly oriented
     * slope. This offset raises every item by exactly the same amount.
     */
    private static final double DISPLAY_VERTICAL_OFFSET =
            2.0D / 16.0D;

    private static final double FIRST_ROW_Y =
            3.0D / 16.0D;

    private static final double DECK_SLOPE =
            0.41421356237D;

    private static final double ITEM_SURFACE_OFFSET =
            0.035D;

    /*
     * A generated item sprite begins in a vertical XY plane. Rotating
     * it 67.5 degrees around X places it parallel to the display deck's
     * 22.5-degree incline after the existing 180-degree Y turn.
     */
    private static final float ITEM_X_ROTATION =
            67.5F;

    public DisplayCaseBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    public void render(
            DisplayCaseBlockEntity display,
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
             slot < DisplayCaseBlockEntity.SIZE;
             slot++) {

            int row =
                    slot / 9;

            int column =
                    slot % 9;

            double x =
                    FIRST_COLUMN_X
                            + column
                            * COLUMN_SPACING;

            double z =
                    FIRST_ROW_Z
                            + row
                            * ROW_SPACING;

            double y =
                    FIRST_ROW_Y
                            + (z - FIRST_ROW_Z)
                            * DECK_SLOPE
                            + ITEM_SURFACE_OFFSET
                            + DISPLAY_VERTICAL_OFFSET;

            DisplayItemRenderer.render(
                    display,
                    display.getItem(slot),
                    x,
                    y,
                    z,
                    0.145F,
                    ITEM_X_ROTATION,
                    180.0F,
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
