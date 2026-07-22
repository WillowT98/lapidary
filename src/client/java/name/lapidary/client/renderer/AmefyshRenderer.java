package name.lapidary.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import name.lapidary.Lapidary;
import name.lapidary.entity.AmefyshEntity;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class AmefyshRenderer
        extends MobRenderer<
        AmefyshEntity,
        TropicalFishModelB<AmefyshEntity>
        > {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    Lapidary.MOD_ID,
                    "textures/entity/amefysh.png"
            );

    public AmefyshRenderer(
            EntityRendererProvider.Context context
    ) {
        super(
                context,
                new TropicalFishModelB<>(
                        context.bakeLayer(
                                ModelLayers.TROPICAL_FISH_LARGE
                        )
                ),
                0.2F
        );
    }

    @Override
    public ResourceLocation getTextureLocation(
            AmefyshEntity entity
    ) {
        return TEXTURE;
    }

    /*
     * Reproduce the tropical fish's swimming sway and its sideways
     * orientation when it is out of water.
     */
    @Override
    protected void setupRotations(
            AmefyshEntity entity,
            PoseStack poseStack,
            float bob,
            float bodyRotation,
            float partialTick,
            float scale
    ) {
        super.setupRotations(
                entity,
                poseStack,
                bob,
                bodyRotation,
                partialTick,
                scale
        );

        float swimmingRotation =
                4.3F * Mth.sin(0.6F * bob);

        poseStack.mulPose(
                Axis.YP.rotationDegrees(swimmingRotation)
        );

        if (!entity.isInWater()) {
            poseStack.translate(
                    0.2F,
                    0.1F,
                    0.0F
            );

            poseStack.mulPose(
                    Axis.ZP.rotationDegrees(90.0F)
            );
        }
    }
}