package name.lapidary.client.renderer;

import name.lapidary.Lapidary;
import name.lapidary.entity.GlowTroutEntity;
import net.minecraft.client.model.CodModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GlowTroutRenderer
        extends MobRenderer<GlowTroutEntity, CodModel<GlowTroutEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    Lapidary.MOD_ID,
                    "textures/entity/glow_trout.png"
            );

    public GlowTroutRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new CodModel<>(context.bakeLayer(ModelLayers.COD)),
                0.3F
        );
    }

    @Override
    public ResourceLocation getTextureLocation(GlowTroutEntity entity) {
        return TEXTURE;
    }
}
