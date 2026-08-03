package name.lapidary.screen;

import name.lapidary.Lapidary;
import name.lapidary.inventory.MageBackpackMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {

    public static final MenuType<JewelersTableMenu>
            JEWELERS_TABLE =
            Registry.register(
                    BuiltInRegistries.MENU,
                    Lapidary.id(
                            "jewelers_table"
                    ),
                    new MenuType<>(
                            JewelersTableMenu::new,
                            FeatureFlags.DEFAULT_FLAGS
                    )
            );

    public static final MenuType<GemCutterMenu>
            GEM_CUTTER =
            Registry.register(
                    BuiltInRegistries.MENU,
                    Lapidary.id(
                            "gem_cutter"
                    ),
                    new MenuType<>(
                            GemCutterMenu::new,
                            FeatureFlags.DEFAULT_FLAGS
                    )
            );

    public static final MenuType<RingDisplayMenu>
            RING_DISPLAY =
            Registry.register(
                    BuiltInRegistries.MENU,
                    Lapidary.id(
                            "ring_display"
                    ),
                    new MenuType<>(
                            RingDisplayMenu::new,
                            FeatureFlags.DEFAULT_FLAGS
                    )
            );

    public static final MenuType<AmuletDisplayMenu>
            AMULET_DISPLAY =
            Registry.register(
                    BuiltInRegistries.MENU,
                    Lapidary.id(
                            "amulet_display"
                    ),
                    new MenuType<>(
                            AmuletDisplayMenu::new,
                            FeatureFlags.DEFAULT_FLAGS
                    )
            );

    public static final MenuType<DisplayCaseMenu>
            DISPLAY_CASE =
            Registry.register(
                    BuiltInRegistries.MENU,
                    Lapidary.id(
                            "display_case"
                    ),
                    new MenuType<>(
                            DisplayCaseMenu::new,
                            FeatureFlags.DEFAULT_FLAGS
                    )
            );

    public static final MenuType<MageBackpackMenu>
            MAGE_BACKPACK =
            Registry.register(
                    BuiltInRegistries.MENU,
                    Lapidary.id(
                            "mage_backpack"
                    ),
                    new MenuType<>(
                            MageBackpackMenu::new,
                            FeatureFlags.DEFAULT_FLAGS
                    )
            );

    public static final MenuType<StainedGlassFabricatorMenu>
            STAINED_GLASS_FABRICATOR =
            Registry.register(
                    BuiltInRegistries.MENU,
                    Lapidary.id(
                            "stained_glass_fabricator"
                    ),
                    new MenuType<>(
                            StainedGlassFabricatorMenu::new,
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
