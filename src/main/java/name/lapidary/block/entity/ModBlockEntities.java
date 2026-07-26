package name.lapidary.block.entity;

import name.lapidary.Lapidary;
import name.lapidary.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Registry;

public final class ModBlockEntities {

    public static final BlockEntityType<SableCacheBlockEntity>
            SABLE_CACHE =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath(
                            Lapidary.MOD_ID,
                            "sable_cache"
                    ),
                    BlockEntityType.Builder.of(
                            SableCacheBlockEntity::new,
                            ModBlocks.SABLE_CACHE
                    ).build(null)
            );
    public static final BlockEntityType<CanisterBlockEntity>
            CANISTER =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath(
                            Lapidary.MOD_ID,
                            "canister"
                    ),
                    BlockEntityType.Builder
                            .of(
                                    CanisterBlockEntity::new,
                                    ModBlocks.CANISTER
                            )
                            .build(null)
            );
    public static final BlockEntityType<
            CustomWindowControllerBlockEntity
            > CUSTOM_WINDOW_CONTROLLER =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath(
                            Lapidary.MOD_ID,
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