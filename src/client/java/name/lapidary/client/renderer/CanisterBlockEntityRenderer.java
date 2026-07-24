package name.lapidary.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import name.lapidary.block.entity.CanisterBlockEntity;
import name.lapidary.fluid.CanisterFluidStorage;
import name.lapidary.fluid.CanisterLiquid;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public final class CanisterBlockEntityRenderer
        implements BlockEntityRenderer<
        CanisterBlockEntity
        > {

    /*
     * The glass interior is approximately X/Z 5 through 11,
     * with usable vertical space from Y=2 through Y=14.
     *
     * Tiny offsets keep the liquid from z-fighting with the glass.
     */
    private static final float MIN_X =
            5.02F / 16.0F;

    private static final float MAX_X =
            10.98F / 16.0F;

    private static final float MIN_Z =
            5.02F / 16.0F;

    private static final float MAX_Z =
            10.98F / 16.0F;

    private static final float BOTTOM_Y_PIXELS =
            2.02F;

    private static final float LIQUID_HEIGHT_PIXELS =
            11.96F;

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

        if (storage.isEmpty()) {
            return;
        }

        CanisterLiquid liquid =
                storage.getLiquid();

        FluidVariant variant =
                FluidVariant.of(
                        liquid.renderFluid()
                );

        TextureAtlasSprite sprite =
                FluidVariantRendering.getSprite(
                        variant
                );

        if (sprite == null) {
            return;
        }

        float fillFraction =
                Math.min(
                        1.0F,
                        Math.max(
                                0.0F,
                                (float) storage.getAmount()
                                        / (float) storage
                                        .getCapacity()
                        )
                );

        float minimumY =
                BOTTOM_Y_PIXELS
                        / 16.0F;

        float maximumY =
                (
                        BOTTOM_Y_PIXELS
                                + LIQUID_HEIGHT_PIXELS
                                * fillFraction
                ) / 16.0F;

        int tint;

        if (liquid.usesWhiteRenderTint()) {
            tint = 0xFFFFFFFF;
        } else {
            tint =
                    FluidVariantRendering
                            .getColor(
                                    variant,
                                    canister.getLevel(),
                                    canister.getBlockPos()
                            );
        }

        int red =
                tint >> 16
                        & 255;

        int green =
                tint >> 8
                        & 255;

        int blue =
                tint
                        & 255;

        int alpha =
                liquid == CanisterLiquid.LAVA
                        ? 255
                        : 215;

        VertexConsumer consumer =
                bufferSource.getBuffer(
                        RenderType.entityTranslucent(
                                TextureAtlas
                                        .LOCATION_BLOCKS
                        )
                );

        PoseStack.Pose pose =
                poseStack.last();

        float u0 =
                sprite.getU0();

        float u1 =
                sprite.getU1();

        float v0 =
                sprite.getV0();

        float v1 =
                sprite.getV1();

        /*
         * Top.
         */
        quad(
                consumer,
                pose,

                MIN_X, maximumY, MIN_Z,
                MIN_X, maximumY, MAX_Z,
                MAX_X, maximumY, MAX_Z,
                MAX_X, maximumY, MIN_Z,

                u0, v0,
                u0, v1,
                u1, v1,
                u1, v0,

                red,
                green,
                blue,
                alpha,
                packedLight,
                packedOverlay,

                0.0F,
                1.0F,
                0.0F
        );

        /*
         * Bottom.
         */
        quad(
                consumer,
                pose,

                MAX_X, minimumY, MIN_Z,
                MAX_X, minimumY, MAX_Z,
                MIN_X, minimumY, MAX_Z,
                MIN_X, minimumY, MIN_Z,

                u0, v0,
                u0, v1,
                u1, v1,
                u1, v0,

                red,
                green,
                blue,
                alpha,
                packedLight,
                packedOverlay,

                0.0F,
                -1.0F,
                0.0F
        );

        /*
         * North.
         */
        quad(
                consumer,
                pose,

                MAX_X, minimumY, MIN_Z,
                MIN_X, minimumY, MIN_Z,
                MIN_X, maximumY, MIN_Z,
                MAX_X, maximumY, MIN_Z,

                u0, v1,
                u1, v1,
                u1, v0,
                u0, v0,

                red,
                green,
                blue,
                alpha,
                packedLight,
                packedOverlay,

                0.0F,
                0.0F,
                -1.0F
        );

        /*
         * South.
         */
        quad(
                consumer,
                pose,

                MIN_X, minimumY, MAX_Z,
                MAX_X, minimumY, MAX_Z,
                MAX_X, maximumY, MAX_Z,
                MIN_X, maximumY, MAX_Z,

                u0, v1,
                u1, v1,
                u1, v0,
                u0, v0,

                red,
                green,
                blue,
                alpha,
                packedLight,
                packedOverlay,

                0.0F,
                0.0F,
                1.0F
        );

        /*
         * West.
         */
        quad(
                consumer,
                pose,

                MIN_X, minimumY, MIN_Z,
                MIN_X, minimumY, MAX_Z,
                MIN_X, maximumY, MAX_Z,
                MIN_X, maximumY, MIN_Z,

                u0, v1,
                u1, v1,
                u1, v0,
                u0, v0,

                red,
                green,
                blue,
                alpha,
                packedLight,
                packedOverlay,

                -1.0F,
                0.0F,
                0.0F
        );

        /*
         * East.
         */
        quad(
                consumer,
                pose,

                MAX_X, minimumY, MAX_Z,
                MAX_X, minimumY, MIN_Z,
                MAX_X, maximumY, MIN_Z,
                MAX_X, maximumY, MAX_Z,

                u0, v1,
                u1, v1,
                u1, v0,
                u0, v0,

                red,
                green,
                blue,
                alpha,
                packedLight,
                packedOverlay,

                1.0F,
                0.0F,
                0.0F
        );
    }

    private static void quad(
            VertexConsumer consumer,
            PoseStack.Pose pose,

            float x1,
            float y1,
            float z1,

            float x2,
            float y2,
            float z2,

            float x3,
            float y3,
            float z3,

            float x4,
            float y4,
            float z4,

            float u1,
            float v1,

            float u2,
            float v2,

            float u3,
            float v3,

            float u4,
            float v4,

            int red,
            int green,
            int blue,
            int alpha,

            int packedLight,
            int packedOverlay,

            float normalX,
            float normalY,
            float normalZ
    ) {
        vertex(
                consumer,
                pose,
                x1,
                y1,
                z1,
                u1,
                v1,
                red,
                green,
                blue,
                alpha,
                packedLight,
                packedOverlay,
                normalX,
                normalY,
                normalZ
        );

        vertex(
                consumer,
                pose,
                x2,
                y2,
                z2,
                u2,
                v2,
                red,
                green,
                blue,
                alpha,
                packedLight,
                packedOverlay,
                normalX,
                normalY,
                normalZ
        );

        vertex(
                consumer,
                pose,
                x3,
                y3,
                z3,
                u3,
                v3,
                red,
                green,
                blue,
                alpha,
                packedLight,
                packedOverlay,
                normalX,
                normalY,
                normalZ
        );

        vertex(
                consumer,
                pose,
                x4,
                y4,
                z4,
                u4,
                v4,
                red,
                green,
                blue,
                alpha,
                packedLight,
                packedOverlay,
                normalX,
                normalY,
                normalZ
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

            int red,
            int green,
            int blue,
            int alpha,

            int packedLight,
            int packedOverlay,

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
                        red,
                        green,
                        blue,
                        alpha
                )
                .setUv(
                        u,
                        v
                )
                .setOverlay(
                        packedOverlay
                )
                .setLight(
                        packedLight
                )
                .setNormal(
                        pose,
                        normalX,
                        normalY,
                        normalZ
                );
    }
}