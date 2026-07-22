package name.lapidary.tag;

import name.lapidary.Lapidary;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public final class ModFluidTags {

    public static final TagKey<Fluid> MANA =
            TagKey.create(
                    Registries.FLUID,
                    ResourceLocation.fromNamespaceAndPath(
                            Lapidary.MOD_ID,
                            "mana"
                    )
            );

    private ModFluidTags() {
    }
}