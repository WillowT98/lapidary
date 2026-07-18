package name.lapidary.tag;

import name.lapidary.Lapidary;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModItemTags {

    public static final TagKey<Item> SHOWS_INSIGHT_BAR =
            TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(
                            Lapidary.MOD_ID,
                            "shows_insight_bar"
                    )
            );
    public static final TagKey<Item> SABLE_FOOD =
            TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(
                            Lapidary.MOD_ID,
                            "sable_food"
                    )
            );
    private ModItemTags() {
    }
}