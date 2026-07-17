package name.lapidary.item;

import name.lapidary.Lapidary;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public final class ModItems {
    public static final Item SEA_GLASS = register(
            "sea_glass",
            new Item(new Item.Properties())
    );
    public static final Item FULGURITE = register(
            "fulgurite",
            new Item(new Item.Properties())
    );
    public static final Item PURE_LAPIS = register(
            "pure_lapis",
            new Item(new Item.Properties())
    );
    private ModItems() {
        // Prevent this utility class from being instantiated.
    }

    private static Item register(String name, Item item) {
        ResourceLocation id =
                ResourceLocation.fromNamespaceAndPath(Lapidary.MOD_ID, name);

        return Registry.register(BuiltInRegistries.ITEM, id, item);
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register(entries -> entries.accept(SEA_GLASS));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register(entries -> entries.accept(FULGURITE));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register(entries -> entries.accept(PURE_LAPIS));
    }
}