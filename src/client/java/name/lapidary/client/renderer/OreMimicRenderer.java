package name.lapidary.client.renderer;

import name.lapidary.client.model.OreMimicModel;
import name.lapidary.entity.OreMimicEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class OreMimicRenderer extends MobRenderer<
        OreMimicEntity,
        OreMimicModel
        > {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "lapidary",
                    "textures/entity/ore_mimic.png"
            );

    public OreMimicRenderer(
            EntityRendererProvider.Context context
    ) {
        super(
                context,
                new OreMimicModel(
                        context.bakeLayer(
                                ModelLayers.PLAYER
                        )
                ),
                0.5F
        );
    }

    @Override
    public ResourceLocation getTextureLocation(
            OreMimicEntity entity
    ) {
        return TEXTURE;
    }
}