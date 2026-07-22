package name.lapidary.screen;

import name.lapidary.Lapidary;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {

    public static final MenuType<GemCutterMenu> GEM_CUTTER =
            Registry.register(
                    BuiltInRegistries.MENU,
                    ResourceLocation.fromNamespaceAndPath(
                            Lapidary.MOD_ID,
                            "gem_cutter"
                    ),
                    new MenuType<>(
                            GemCutterMenu::new,
                            FeatureFlags.DEFAULT_FLAGS
                    )
            );

    private ModMenus() {
    }

    public static void initialize() {
        Lapidary.LOGGER.info(
                "Registering Lapidary menus"
        );
    }
}