package name.lapidary.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import name.lapidary.fluid.CanisterLiquid;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

/**
 * Renders the one-bucket contents of the mana percolator's glass chamber.
 *
 * The bounds sit slightly inside the new Blockbench model's chamber:
 *
 * x/z = 1..15 pixels
 * y   = 2..12 pixels
 *
 * The small inset prevents the fluid and glass from occupying the exact
 * same planes, which would produce z-fighting or dark translucent haze.
 */
public final class PercolatorFluidRenderer {

    private static final float MIN_X =
            1.05F / 16.0F;

    private static final float MAX_X =
            14.95F / 16.0F;

    private static final float MIN_Y =
            2.05F / 16.0F;

    private static final float MAX_Y =
            11.95F / 16.0F;

    private static final float MIN_Z =
            1.05F / 16.0F;

    private static final float MAX_Z =
            14.95F / 16.0F;

    private PercolatorFluidRenderer() {
    }

    public static void render(
            CanisterLiquid liquid,
            BlockAndTintGetter level,
            BlockPos position,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        if (liquid == null) {
            return;
        }

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

        int tint =
                liquid.usesWhiteRenderTint()
                        ? 0x3F76E4
                        : FluidVariantRendering.getColor(
                                variant,
                                level,
                                position
                        );

        int red = tint >> 16 & 255;
        int green = tint >> 8 & 255;
        int blue = tint & 255;
        int alpha = 215;

        VertexConsumer consumer =
                bufferSource.getBuffer(
                        RenderType.entityTranslucent(
                                TextureAtlas.LOCATION_BLOCKS
                        )
                );

        PoseStack.Pose pose =
                poseStack.last();

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        /* Top surface. */
        quad(
                consumer,
                pose,
                MIN_X, MAX_Y, MIN_Z,
                MIN_X, MAX_Y, MAX_Z,
                MAX_X, MAX_Y, MAX_Z,
                MAX_X, MAX_Y, MIN_Z,
                u0, v0,
                u0, v1,
                u1, v1,
                u1, v0,
                red, green, blue, alpha,
                packedLight,
                packedOverlay,
                0.0F, 1.0F, 0.0F
        );

        /* North face. */
        quad(
                consumer,
                pose,
                MAX_X, MIN_Y, MIN_Z,
                MIN_X, MIN_Y, MIN_Z,
                MIN_X, MAX_Y, MIN_Z,
                MAX_X, MAX_Y, MIN_Z,
                u0, v1,
                u1, v1,
                u1, v0,
                u0, v0,
                red, green, blue, alpha,
                packedLight,
                packedOverlay,
                0.0F, 0.0F, -1.0F
        );

        /* South face. */
        quad(
                consumer,
                pose,
                MIN_X, MIN_Y, MAX_Z,
                MAX_X, MIN_Y, MAX_Z,
                MAX_X, MAX_Y, MAX_Z,
                MIN_X, MAX_Y, MAX_Z,
                u0, v1,
                u1, v1,
                u1, v0,
                u0, v0,
                red, green, blue, alpha,
                packedLight,
                packedOverlay,
                0.0F, 0.0F, 1.0F
        );

        /* West face. */
        quad(
                consumer,
                pose,
                MIN_X, MIN_Y, MIN_Z,
                MIN_X, MIN_Y, MAX_Z,
                MIN_X, MAX_Y, MAX_Z,
                MIN_X, MAX_Y, MIN_Z,
                u0, v1,
                u1, v1,
                u1, v0,
                u0, v0,
                red, green, blue, alpha,
                packedLight,
                packedOverlay,
                -1.0F, 0.0F, 0.0F
        );

        /* East face. */
        quad(
                consumer,
                pose,
                MAX_X, MIN_Y, MAX_Z,
                MAX_X, MIN_Y, MIN_Z,
                MAX_X, MAX_Y, MIN_Z,
                MAX_X, MAX_Y, MAX_Z,
                u0, v1,
                u1, v1,
                u1, v0,
                u0, v0,
                red, green, blue, alpha,
                packedLight,
                packedOverlay,
                1.0F, 0.0F, 0.0F
        );
    }

    private static void quad(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4,
            float u1, float v1,
            float u2, float v2,
            float u3, float v3,
            float u4, float v4,
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
                x1, y1, z1,
                u1, v1,
                red, green, blue, alpha,
                packedLight,
                packedOverlay,
                normalX, normalY, normalZ
        );

        vertex(
                consumer,
                pose,
                x2, y2, z2,
                u2, v2,
                red, green, blue, alpha,
                packedLight,
                packedOverlay,
                normalX, normalY, normalZ
        );

        vertex(
                consumer,
                pose,
                x3, y3, z3,
                u3, v3,
                red, green, blue, alpha,
                packedLight,
                packedOverlay,
                normalX, normalY, normalZ
        );

        vertex(
                consumer,
                pose,
                x4, y4, z4,
                u4, v4,
                red, green, blue, alpha,
                packedLight,
                packedOverlay,
                normalX, normalY, normalZ
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
                .setUv(u, v)
                .setOverlay(packedOverlay)
                .setLight(packedLight)
                .setNormal(
                        pose,
                        normalX,
                        normalY,
                        normalZ
                );
    }
}
