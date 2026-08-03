package name.lapidary.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import name.lapidary.block.entity.RingDisplayBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public final class RingDisplayBlockEntityRenderer
        implements BlockEntityRenderer<
        RingDisplayBlockEntity
        > {

    /*
     * This is the same slant used by the second visual pass, which had
     * the correct orientation relative to the angled display board.
     */
    private static final float BOARD_ANGLE =
            22.5F;

    private static final double BOARD_ORIGIN_X =
            8.0D / 16.0D;

    private static final double BOARD_ORIGIN_Y =
            2.0D / 16.0D;

    private static final double BOARD_ORIGIN_Z =
            8.0D / 16.0D;

    /*
     * Move the complete already-correct grid straight upward without
     * changing its angle, spacing, or depth relative to itself.
     */
    private static final double RING_VERTICAL_OFFSET =
            3.0D / 16.0D;

    private static final double FIRST_COLUMN_X =
            0.215D;

    private static final double COLUMN_SPACING =
            0.19D;

    private static final double FIRST_ROW_Y =
            0.91D;

    private static final double ROW_SPACING =
            0.21D;

    /*
     * The board's visible face is on its south/front side. Move the
     * already-correct ring grid forward by three quarters of a model
     * pixel so each item rests just above that face rather than being
     * partially buried inside it.
     */
    private static final double RING_PLANE_Z =
            8.03D / 16.0D;

    private static final double RING_BOARD_DOWN_OFFSET =
            3.0D / 16.0D;

    public RingDisplayBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    public void render(
            RingDisplayBlockEntity display,
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

        /*
         * This translation happens before the board-angle transform, so
         * it raises the whole ring grid vertically instead of altering
         * its slant along the board.
         */
        poseStack.translate(
                0.0D,
                RING_VERTICAL_OFFSET,
                0.0D
        );

        poseStack.translate(
                BOARD_ORIGIN_X,
                BOARD_ORIGIN_Y,
                BOARD_ORIGIN_Z
        );

        poseStack.mulPose(
                Axis.XP.rotationDegrees(
                        BOARD_ANGLE
                )
        );

        poseStack.translate(
                -BOARD_ORIGIN_X,
                -BOARD_ORIGIN_Y,
                -BOARD_ORIGIN_Z
        );

        /*
         * Move the ring grid downward along the board rather than straight
         * downward in world space. This preserves its current offset from
         * the board's visible face.
         */
        poseStack.translate(
                0.0D,
                -RING_BOARD_DOWN_OFFSET,
                0.0D
        );

        for (int slot = 0;
             slot < RingDisplayBlockEntity.SIZE;
             slot++) {

            int row =
                    slot / 4;

            int column =
                    slot % 4;

            double x =
                    FIRST_COLUMN_X
                            + column
                            * COLUMN_SPACING;

            double y =
                    FIRST_ROW_Y
                            - row
                            * ROW_SPACING;

            DisplayItemRenderer.render(
                    display,
                    display.getItem(slot),
                    x,
                    y,
                    RING_PLANE_Z,
                    0.235F,
                    0.0F,
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
