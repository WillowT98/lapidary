package name.lapidary.client.renderer;

import name.lapidary.Lapidary;
import name.lapidary.entity.BrightSalmonEntity;
import net.minecraft.client.model.SalmonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BrightSalmonRenderer
        extends MobRenderer<BrightSalmonEntity, SalmonModel<BrightSalmonEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    Lapidary.MOD_ID,
                    "textures/entity/bright_salmon.png"
            );

    public BrightSalmonRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new SalmonModel<>(context.bakeLayer(ModelLayers.SALMON)),
                0.4F
        );
    }

    @Override
    public ResourceLocation getTextureLocation(BrightSalmonEntity entity) {
        return TEXTURE;
    }
}