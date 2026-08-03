package name.lapidary.tag;

import name.lapidary.Lapidary;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModItemTags {

    public static final TagKey<Item> GEMS =
            create("gems");

    public static final TagKey<Item> RINGS =
            create("rings");

    public static final TagKey<Item> AMULETS =
            create("amulets");

    public static final TagKey<Item> SHOWS_INSIGHT_BAR =
            create(
                    "shows_insight_bar"
            );

    public static final TagKey<Item> SABLE_FOOD =
            create(
                    "sable_food"
            );

    public static final TagKey<Item> FELINE_FOODS =
            create(
                    "feline_foods"
            );

    public static final TagKey<Item> MOTH_FOODS =
            create(
                    "moth_foods"
            );

    public static final TagKey<Item> FAIRY_FOODS =
            create(
                    "fairy_foods"
            );

    public static final TagKey<Item> HEAVY_ARMOR =
            create(
                    "heavy_armor"
            );

    private static TagKey<Item> create(
            String path
    ) {
        return TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(
                        Lapidary.MOD_ID,
                        path
                )
        );
    }

    private ModItemTags() {
    }
}
