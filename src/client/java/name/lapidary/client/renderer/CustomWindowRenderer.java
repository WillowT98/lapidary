package name.lapidary.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import name.lapidary.block.CustomWindowControllerBlock;
import name.lapidary.block.entity.CustomWindowControllerBlockEntity;
import name.lapidary.window.WindowDesign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class CustomWindowRenderer
        implements BlockEntityRenderer<
        CustomWindowControllerBlockEntity
        > {

    private static final float FRONT_POSITIVE =
            9.0F / 16.0F;

    private static final float FRONT_NEGATIVE =
            7.0F / 16.0F;

    private static final float BACK_POSITIVE =
            7.0F / 16.0F;

    private static final float BACK_NEGATIVE =
            9.0F / 16.0F;

    private static final int EDGE_ALPHA =
            150;

    private static final int GLASS_ALPHA =
            205;

    private static final int[] COLOR_RGB = {
            0xF9FFFE,
            0xF9801D,
            0xC74EBD,
            0x3AB3DA,
            0xFED83D,
            0x80C71F,
            0xF38BAA,
            0x474F52,
            0x9D9D97,
            0x169C9C,
            0x8932B8,
            0x3C44AA,
            0x835432,
            0x5E7C16,
            0xB02E26,
            0x1D1D21
    };

    private static final ResourceLocation
            WHITE_GLASS_TEXTURE =
            ResourceLocation.withDefaultNamespace(
                    "block/white_stained_glass"
            );

    public CustomWindowRenderer(
            BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    public void render(
            CustomWindowControllerBlockEntity controller,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        if (controller.getLevel() == null) {
            return;
        }

        WindowDesign design =
                controller.getDesign()
                        .orElse(null);

        if (design == null) {
            return;
        }

        BlockState state =
                controller.getBlockState();

        if (!state.hasProperty(
                CustomWindowControllerBlock.FACING
        )) {
            return;
        }

        Direction facing =
                state.getValue(
                        CustomWindowControllerBlock.FACING
                );

        TextureAtlasSprite glassSprite =
                Minecraft.getInstance()
                        .getTextureAtlas(
                                TextureAtlas.LOCATION_BLOCKS
                        )
                        .apply(
                                WHITE_GLASS_TEXTURE
                        );

        TextureAtlasSprite backgroundSprite =
                design.backgroundBlock()
                        == Blocks.AIR
                        ? null
                        : getSideSprite(
                        design.backgroundBlock()
                );

        VertexConsumer consumer =
                bufferSource.getBuffer(
                        RenderType.entityTranslucent(
                                TextureAtlas.LOCATION_BLOCKS
                        )
                );

        PoseStack.Pose pose =
                poseStack.last();

        renderFaces(
                design,
                facing,
                backgroundSprite,
                glassSprite,
                consumer,
                pose,
                packedLight,
                packedOverlay
        );

        renderOuterEdges(
                design,
                facing,
                glassSprite,
                consumer,
                pose,
                packedLight,
                packedOverlay
        );
    }

    @Override
    public boolean shouldRenderOffScreen(
            CustomWindowControllerBlockEntity
                    controller
    ) {
        /*
         * The renderer originates at the controller but may extend five
         * blocks upward and sideways.
         */
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    private static void renderFaces(
            WindowDesign design,
            Direction facing,
            TextureAtlasSprite backgroundSprite,
            TextureAtlasSprite glassSprite,
            VertexConsumer consumer,
            PoseStack.Pose pose,
            int packedLight,
            int packedOverlay
    ) {
        int pixelWidth =
                design.pixelWidth();

        int pixelHeight =
                design.pixelHeight();

        byte[] pixels =
                design.pixels();

        float frontDepth =
                facing.getAxisDirection()
                        == Direction.AxisDirection.POSITIVE
                        ? FRONT_POSITIVE
                        : FRONT_NEGATIVE;

        float backDepth =
                facing.getAxisDirection()
                        == Direction.AxisDirection.POSITIVE
                        ? BACK_POSITIVE
                        : BACK_NEGATIVE;

        for (int y = 0;
             y < pixelHeight;
             y++) {

            int x =
                    0;

            while (x < pixelWidth) {
                int value =
                        Byte.toUnsignedInt(
                                pixels[
                                        y * pixelWidth + x
                                        ]
                        );

                int tileBoundary =
                        Math.min(
                                pixelWidth,
                                (
                                        x / WindowDesign.PIXELS_PER_BLOCK
                                                + 1
                                )
                                        * WindowDesign.PIXELS_PER_BLOCK
                        );

                int end =
                        x + 1;

                while (end < tileBoundary
                        && Byte.toUnsignedInt(
                        pixels[
                                y * pixelWidth + end
                                ]
                ) == value) {

                    end++;
                }

                if (value
                        != WindowDesign.BACKGROUND_PIXEL
                        || backgroundSprite != null) {

                    TextureAtlasSprite sprite =
                            value
                                    == WindowDesign.BACKGROUND_PIXEL
                                    ? backgroundSprite
                                    : glassSprite;

                    int color =
                            value
                                    == WindowDesign.BACKGROUND_PIXEL
                                    ? 0xFFFFFF
                                    : COLOR_RGB[value];

                    int alpha =
                            value
                                    == WindowDesign.BACKGROUND_PIXEL
                                    ? 255
                                    : GLASS_ALPHA;

                    renderPixelRun(
                            design,
                            facing,
                            sprite,
                            consumer,
                            pose,
                            x,
                            end,
                            y,
                            frontDepth,
                            backDepth,
                            color,
                            alpha,
                            packedLight,
                            packedOverlay
                    );
                }

                x =
                        end;
            }
        }
    }

    private static void renderPixelRun(
            WindowDesign design,
            Direction facing,
            TextureAtlasSprite sprite,
            VertexConsumer consumer,
            PoseStack.Pose pose,
            int startPixel,
            int endPixel,
            int pixelY,
            float frontDepth,
            float backDepth,
            int color,
            int alpha,
            int packedLight,
            int packedOverlay
    ) {
        float left =
                (float) startPixel
                        / WindowDesign.PIXELS_PER_BLOCK;

        float right =
                (float) endPixel
                        / WindowDesign.PIXELS_PER_BLOCK;

        float top =
                design.blockHeight()
                        - (
                        (float) pixelY
                                / WindowDesign.PIXELS_PER_BLOCK
                );

        float bottom =
                design.blockHeight()
                        - (
                        (float) (pixelY + 1)
                                / WindowDesign.PIXELS_PER_BLOCK
                );

        int localStartX =
                Math.floorMod(
                        startPixel,
                        WindowDesign.PIXELS_PER_BLOCK
                );

        int localEndX =
                localStartX
                        + (
                        endPixel - startPixel
                );

        int localY =
                Math.floorMod(
                        pixelY,
                        WindowDesign.PIXELS_PER_BLOCK
                );

        float u0 =
                interpolate(
                        sprite.getU0(),
                        sprite.getU1(),
                        (float) localStartX
                                / WindowDesign.PIXELS_PER_BLOCK
                );

        float u1 =
                interpolate(
                        sprite.getU0(),
                        sprite.getU1(),
                        (float) localEndX
                                / WindowDesign.PIXELS_PER_BLOCK
                );

        float v0 =
                interpolate(
                        sprite.getV0(),
                        sprite.getV1(),
                        (float) localY
                                / WindowDesign.PIXELS_PER_BLOCK
                );

        float v1 =
                interpolate(
                        sprite.getV0(),
                        sprite.getV1(),
                        (float) (localY + 1)
                                / WindowDesign.PIXELS_PER_BLOCK
                );

        int red =
                color >> 16 & 255;

        int green =
                color >> 8 & 255;

        int blue =
                color & 255;

        Point frontBottomLeft =
                point(
                        facing,
                        left,
                        bottom,
                        frontDepth
                );

        Point frontBottomRight =
                point(
                        facing,
                        right,
                        bottom,
                        frontDepth
                );

        Point frontTopRight =
                point(
                        facing,
                        right,
                        top,
                        frontDepth
                );

        Point frontTopLeft =
                point(
                        facing,
                        left,
                        top,
                        frontDepth
                );

        quad(
                consumer,
                pose,
                frontBottomLeft,
                frontBottomRight,
                frontTopRight,
                frontTopLeft,
                u0, v1,
                u1, v1,
                u1, v0,
                u0, v0,
                red, green, blue, alpha,
                packedLight,
                packedOverlay,
                facing.getStepX(),
                0.0F,
                facing.getStepZ()
        );

        Direction backFacing =
                facing.getOpposite();

        Point backBottomLeft =
                point(
                        facing,
                        left,
                        bottom,
                        backDepth
                );

        Point backBottomRight =
                point(
                        facing,
                        right,
                        bottom,
                        backDepth
                );

        Point backTopRight =
                point(
                        facing,
                        right,
                        top,
                        backDepth
                );

        Point backTopLeft =
                point(
                        facing,
                        left,
                        top,
                        backDepth
                );

        quad(
                consumer,
                pose,
                backBottomRight,
                backBottomLeft,
                backTopLeft,
                backTopRight,
                u1, v1,
                u0, v1,
                u0, v0,
                u1, v0,
                red, green, blue, alpha,
                packedLight,
                packedOverlay,
                backFacing.getStepX(),
                0.0F,
                backFacing.getStepZ()
        );
    }

    private static void renderOuterEdges(
            WindowDesign design,
            Direction facing,
            TextureAtlasSprite sprite,
            VertexConsumer consumer,
            PoseStack.Pose pose,
            int packedLight,
            int packedOverlay
    ) {
        float frontDepth =
                facing.getAxisDirection()
                        == Direction.AxisDirection.POSITIVE
                        ? FRONT_POSITIVE
                        : FRONT_NEGATIVE;

        float backDepth =
                facing.getAxisDirection()
                        == Direction.AxisDirection.POSITIVE
                        ? BACK_POSITIVE
                        : BACK_NEGATIVE;

        for (int x = 0;
             x < design.blockWidth();
             x++) {

            renderHorizontalEdge(
                    facing,
                    sprite,
                    consumer,
                    pose,
                    x,
                    x + 1.0F,
                    0.0F,
                    frontDepth,
                    backDepth,
                    false,
                    packedLight,
                    packedOverlay
            );

            renderHorizontalEdge(
                    facing,
                    sprite,
                    consumer,
                    pose,
                    x,
                    x + 1.0F,
                    design.blockHeight(),
                    frontDepth,
                    backDepth,
                    true,
                    packedLight,
                    packedOverlay
            );
        }

        Direction rightDirection =
                switch (facing) {
                    case NORTH -> Direction.WEST;
                    case SOUTH -> Direction.EAST;
                    case EAST -> Direction.NORTH;
                    case WEST -> Direction.SOUTH;
                    default -> Direction.EAST;
                };

        for (int y = 0;
             y < design.blockHeight();
             y++) {

            renderVerticalEdge(
                    facing,
                    sprite,
                    consumer,
                    pose,
                    0.0F,
                    y,
                    y + 1.0F,
                    frontDepth,
                    backDepth,
                    rightDirection.getOpposite(),
                    packedLight,
                    packedOverlay
            );

            renderVerticalEdge(
                    facing,
                    sprite,
                    consumer,
                    pose,
                    design.blockWidth(),
                    y,
                    y + 1.0F,
                    frontDepth,
                    backDepth,
                    rightDirection,
                    packedLight,
                    packedOverlay
            );
        }
    }

    private static void renderHorizontalEdge(
            Direction facing,
            TextureAtlasSprite sprite,
            VertexConsumer consumer,
            PoseStack.Pose pose,
            float left,
            float right,
            float y,
            float frontDepth,
            float backDepth,
            boolean top,
            int packedLight,
            int packedOverlay
    ) {
        Point leftBack =
                point(
                        facing,
                        left,
                        y,
                        backDepth
                );

        Point rightBack =
                point(
                        facing,
                        right,
                        y,
                        backDepth
                );

        Point rightFront =
                point(
                        facing,
                        right,
                        y,
                        frontDepth
                );

        Point leftFront =
                point(
                        facing,
                        left,
                        y,
                        frontDepth
                );

        if (top) {
            quad(
                    consumer,
                    pose,
                    leftBack,
                    rightBack,
                    rightFront,
                    leftFront,
                    sprite.getU0(), sprite.getV1(),
                    sprite.getU1(), sprite.getV1(),
                    sprite.getU1(), sprite.getV0(),
                    sprite.getU0(), sprite.getV0(),
                    255, 255, 255, EDGE_ALPHA,
                    packedLight,
                    packedOverlay,
                    0.0F, 1.0F, 0.0F
            );
        } else {
            quad(
                    consumer,
                    pose,
                    leftFront,
                    rightFront,
                    rightBack,
                    leftBack,
                    sprite.getU0(), sprite.getV1(),
                    sprite.getU1(), sprite.getV1(),
                    sprite.getU1(), sprite.getV0(),
                    sprite.getU0(), sprite.getV0(),
                    255, 255, 255, EDGE_ALPHA,
                    packedLight,
                    packedOverlay,
                    0.0F, -1.0F, 0.0F
            );
        }
    }

    private static void renderVerticalEdge(
            Direction facing,
            TextureAtlasSprite sprite,
            VertexConsumer consumer,
            PoseStack.Pose pose,
            float horizontal,
            float bottom,
            float top,
            float frontDepth,
            float backDepth,
            Direction normal,
            int packedLight,
            int packedOverlay
    ) {
        Point bottomBack =
                point(
                        facing,
                        horizontal,
                        bottom,
                        backDepth
                );

        Point bottomFront =
                point(
                        facing,
                        horizontal,
                        bottom,
                        frontDepth
                );

        Point topFront =
                point(
                        facing,
                        horizontal,
                        top,
                        frontDepth
                );

        Point topBack =
                point(
                        facing,
                        horizontal,
                        top,
                        backDepth
                );

        quad(
                consumer,
                pose,
                bottomBack,
                bottomFront,
                topFront,
                topBack,
                sprite.getU0(), sprite.getV1(),
                sprite.getU1(), sprite.getV1(),
                sprite.getU1(), sprite.getV0(),
                sprite.getU0(), sprite.getV0(),
                255, 255, 255, EDGE_ALPHA,
                packedLight,
                packedOverlay,
                normal.getStepX(),
                0.0F,
                normal.getStepZ()
        );
    }

    private static TextureAtlasSprite getSideSprite(
            Block block
    ) {
        BlockState state =
                block.defaultBlockState();

        BakedModel model =
                Minecraft.getInstance()
                        .getBlockRenderer()
                        .getBlockModel(
                                state
                        );

        List<BakedQuad> sideQuads =
                model.getQuads(
                        state,
                        Direction.NORTH,
                        RandomSource.create(0L)
                );

        for (BakedQuad quad :
                sideQuads) {

            if (quad.getTintIndex() < 0) {
                return quad.getSprite();
            }
        }

        if (!sideQuads.isEmpty()) {
            return sideQuads.getFirst()
                    .getSprite();
        }

        List<BakedQuad> unculledQuads =
                model.getQuads(
                        state,
                        null,
                        RandomSource.create(0L)
                );

        if (!unculledQuads.isEmpty()) {
            return unculledQuads.getFirst()
                    .getSprite();
        }

        return model.getParticleIcon();
    }

    private static Point point(
            Direction facing,
            float horizontal,
            float vertical,
            float depth
    ) {
        return switch (facing) {
            case SOUTH ->
                    new Point(
                            horizontal,
                            vertical,
                            depth
                    );

            case NORTH ->
                    new Point(
                            1.0F - horizontal,
                            vertical,
                            depth
                    );

            case EAST ->
                    new Point(
                            depth,
                            vertical,
                            1.0F - horizontal
                    );

            case WEST ->
                    new Point(
                            depth,
                            vertical,
                            horizontal
                    );

            default ->
                    new Point(
                            horizontal,
                            vertical,
                            depth
                    );
        };
    }

    private static float interpolate(
            float start,
            float end,
            float fraction
    ) {
        return start
                + (
                end - start
        ) * fraction;
    }

    private static void quad(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            Point point1,
            Point point2,
            Point point3,
            Point point4,
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
                point1,
                u1, v1,
                red, green, blue, alpha,
                packedLight,
                packedOverlay,
                normalX, normalY, normalZ
        );

        vertex(
                consumer,
                pose,
                point2,
                u2, v2,
                red, green, blue, alpha,
                packedLight,
                packedOverlay,
                normalX, normalY, normalZ
        );

        vertex(
                consumer,
                pose,
                point3,
                u3, v3,
                red, green, blue, alpha,
                packedLight,
                packedOverlay,
                normalX, normalY, normalZ
        );

        vertex(
                consumer,
                pose,
                point4,
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
            Point point,
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
                        point.x(),
                        point.y(),
                        point.z()
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

    private record Point(
            float x,
            float y,
            float z
    ) {
    }
}
