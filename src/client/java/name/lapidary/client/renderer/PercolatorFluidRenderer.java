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
 * The top surface and side walls are intentionally separated. The walls use
 * a much lower opacity and a culled translucent layer, while the top remains
 * the strong visual indicator of the chamber's contents. This lets the chamber
 * read as filled without surrounding the gem with several dense translucent
 * sheets.
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

    private static final int WATER_TOP_ALPHA =
            145;
    private static final int MANA_TOP_ALPHA =
            175;

    private static final int WATER_SIDE_ALPHA =
            34;
    private static final int MANA_SIDE_ALPHA =
            48;

    private PercolatorFluidRenderer() {
    }

    /**
     * Draws the faint interior side walls before the gem is rendered.
     */
    public static void renderSideWalls(
            CanisterLiquid liquid,
            BlockAndTintGetter level,
            BlockPos position,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        RenderData data =
                createRenderData(
                        liquid,
                        level,
                        position
                );

        if (data == null) {
            return;
        }

        int alpha =
                liquid == CanisterLiquid.MANA
                        ? MANA_SIDE_ALPHA
                        : WATER_SIDE_ALPHA;

        VertexConsumer consumer =
                bufferSource.getBuffer(
                        RenderType.entityTranslucentCull(
                                TextureAtlas.LOCATION_BLOCKS
                        )
                );

        PoseStack.Pose pose =
                poseStack.last();

        float u0 = data.sprite().getU0();
        float u1 = data.sprite().getU1();
        float v0 = data.sprite().getV0();
        float v1 = data.sprite().getV1();

        /* North wall: outward normal -Z. */
        quad(
                consumer,
                pose,
                MIN_X, MIN_Y, MIN_Z,
                MIN_X, MAX_Y, MIN_Z,
                MAX_X, MAX_Y, MIN_Z,
                MAX_X, MIN_Y, MIN_Z,
                u0, v1,
                u0, v0,
                u1, v0,
                u1, v1,
                data.red(), data.green(), data.blue(), alpha,
                packedLight,
                packedOverlay,
                0.0F, 0.0F, -1.0F
        );

        /* South wall: outward normal +Z. */
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
                data.red(), data.green(), data.blue(), alpha,
                packedLight,
                packedOverlay,
                0.0F, 0.0F, 1.0F
        );

        /* West wall: outward normal -X. */
        quad(
                consumer,
                pose,
                MIN_X, MIN_Y, MAX_Z,
                MIN_X, MAX_Y, MAX_Z,
                MIN_X, MAX_Y, MIN_Z,
                MIN_X, MIN_Y, MIN_Z,
                u0, v1,
                u0, v0,
                u1, v0,
                u1, v1,
                data.red(), data.green(), data.blue(), alpha,
                packedLight,
                packedOverlay,
                -1.0F, 0.0F, 0.0F
        );

        /* East wall: outward normal +X. */
        quad(
                consumer,
                pose,
                MAX_X, MIN_Y, MIN_Z,
                MAX_X, MAX_Y, MIN_Z,
                MAX_X, MAX_Y, MAX_Z,
                MAX_X, MIN_Y, MAX_Z,
                u0, v1,
                u0, v0,
                u1, v0,
                u1, v1,
                data.red(), data.green(), data.blue(), alpha,
                packedLight,
                packedOverlay,
                1.0F, 0.0F, 0.0F
        );
    }

    /**
     * Draws the clearly visible horizontal liquid surface after the gem.
     */
    public static void renderTopSurface(
            CanisterLiquid liquid,
            BlockAndTintGetter level,
            BlockPos position,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        RenderData data =
                createRenderData(
                        liquid,
                        level,
                        position
                );

        if (data == null) {
            return;
        }

        int alpha =
                liquid == CanisterLiquid.MANA
                        ? MANA_TOP_ALPHA
                        : WATER_TOP_ALPHA;

        VertexConsumer consumer =
                bufferSource.getBuffer(
                        RenderType.entityTranslucent(
                                TextureAtlas.LOCATION_BLOCKS
                        )
                );

        PoseStack.Pose pose =
                poseStack.last();

        float u0 = data.sprite().getU0();
        float u1 = data.sprite().getU1();
        float v0 = data.sprite().getV0();
        float v1 = data.sprite().getV1();

        quad(
                consumer,
                pose,
                MIN_X, MAX_Y, MAX_Z,
                MAX_X, MAX_Y, MAX_Z,
                MAX_X, MAX_Y, MIN_Z,
                MIN_X, MAX_Y, MIN_Z,
                u0, v1,
                u1, v1,
                u1, v0,
                u0, v0,
                data.red(), data.green(), data.blue(), alpha,
                packedLight,
                packedOverlay,
                0.0F, 1.0F, 0.0F
        );
    }

    private static RenderData createRenderData(
            CanisterLiquid liquid,
            BlockAndTintGetter level,
            BlockPos position
    ) {
        if (liquid == null) {
            return null;
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
            return null;
        }

        int tint =
                liquid.usesWhiteRenderTint()
                        ? 0xFFFFFF
                        : FluidVariantRendering.getColor(
                                variant,
                                level,
                                position
                        );

        return new RenderData(
                sprite,
                tint >> 16 & 255,
                tint >> 8 & 255,
                tint & 255
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

    private record RenderData(
            TextureAtlasSprite sprite,
            int red,
            int green,
            int blue
    ) {
    }
}
