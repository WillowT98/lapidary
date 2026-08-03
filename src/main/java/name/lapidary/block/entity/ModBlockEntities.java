package name.lapidary.block.entity;

import name.lapidary.Lapidary;
import name.lapidary.block.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {

    public static final BlockEntityType<
            SableCacheBlockEntity
            > SABLE_CACHE =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Lapidary.id(
                            "sable_cache"
                    ),
                    BlockEntityType.Builder.of(
                            SableCacheBlockEntity::new,
                            ModBlocks.SABLE_CACHE
                    ).build(null)
            );

    public static final BlockEntityType<
            CanisterBlockEntity
            > CANISTER =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Lapidary.id(
                            "canister"
                    ),
                    BlockEntityType.Builder.of(
                            CanisterBlockEntity::new,
                            ModBlocks.CANISTER
                    ).build(null)
            );

    public static final BlockEntityType<
            ManaPercolatorBlockEntity
            > MANA_PERCOLATOR =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Lapidary.id(
                            "mana_percolator"
                    ),
                    BlockEntityType.Builder.of(
                            ManaPercolatorBlockEntity::new,
                            ModBlocks.MANA_PERCOLATOR
                    ).build(null)
            );

    public static final BlockEntityType<
            RingDisplayBlockEntity
            > RING_DISPLAY =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Lapidary.id(
                            "ring_display"
                    ),
                    BlockEntityType.Builder.of(
                            RingDisplayBlockEntity::new,
                            ModBlocks.RING_DISPLAY
                    ).build(null)
            );

    public static final BlockEntityType<
            AmuletDisplayBlockEntity
            > AMULET_DISPLAY =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Lapidary.id(
                            "amulet_display"
                    ),
                    BlockEntityType.Builder.of(
                            AmuletDisplayBlockEntity::new,
                            ModBlocks.AMULET_DISPLAY
                    ).build(null)
            );

    public static final BlockEntityType<
            DisplayCaseBlockEntity
            > DISPLAY_CASE =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Lapidary.id(
                            "display_case"
                    ),
                    BlockEntityType.Builder.of(
                            DisplayCaseBlockEntity::new,
                            ModBlocks.DISPLAY_CASE
                    ).build(null)
            );

    public static final BlockEntityType<
            CustomWindowControllerBlockEntity
            > CUSTOM_WINDOW_CONTROLLER =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Lapidary.id(
                            "custom_window_controller"
                    ),
                    BlockEntityType.Builder.of(
                            CustomWindowControllerBlockEntity::new,
                            ModBlocks.CUSTOM_WINDOW_CONTROLLER
                    ).build(null)
            );

    private ModBlockEntities() {
    }

    public static void initialize() {
        Lapidary.LOGGER.info(
                "Registering Lapidary block entities"
        );
    }
}
