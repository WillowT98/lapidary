package name.lapidary.entity;

import name.lapidary.Lapidary;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {

    /*
     * Standard standing player dimensions:
     *
     * Width:  0.6 blocks
     * Height: 1.8 blocks
     * Eyes:   1.62 blocks above the feet
     */
    public static final EntityType<OreMimicEntity> ORE_MIMIC =
            register(
                    "ore_mimic",
                    EntityType.Builder.of(
                                    OreMimicEntity::new,
                                    MobCategory.MONSTER
                            )
                            .sized(0.6F, 1.8F)
                            .eyeHeight(1.62F)
                            .clientTrackingRange(8)
            );
    public static final EntityType<SableEntity> SABLE =
            register(
                    "sable",
                    EntityType.Builder.of(
                                    SableEntity::new,
                                    MobCategory.CREATURE
                            )
                            .sized(0.6F, 0.7F)
                            .clientTrackingRange(8)
            );
    /*
     * Uses the vanilla cod body size and behavior.
     */
    public static final EntityType<GlowTroutEntity> GLOW_TROUT =
            register(
                    "glow_trout",
                    EntityType.Builder.of(
                                    GlowTroutEntity::new,
                                    MobCategory.WATER_AMBIENT
                            )
                            .sized(0.5F, 0.3F)
                            .clientTrackingRange(8)
            );

    /*
     * Uses the vanilla salmon body size and behavior.
     */
    public static final EntityType<BrightSalmonEntity> BRIGHT_SALMON =
            register(
                    "bright_salmon",
                    EntityType.Builder.of(
                                    BrightSalmonEntity::new,
                                    MobCategory.WATER_AMBIENT
                            )
                            .sized(0.7F, 0.4F)
                            .clientTrackingRange(8)
            );

    private ModEntities() {
    }

    private static <T extends Entity> EntityType<T> register(
            String name,
            EntityType.Builder<T> builder
    ) {
        ResourceLocation id =
                ResourceLocation.fromNamespaceAndPath(
                        Lapidary.MOD_ID,
                        name
                );

        EntityType<T> entityType =
                builder.build(id.toString());

        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                id,
                entityType
        );
    }

    public static void initialize() {
        Lapidary.LOGGER.info("Registering Lapidary entities");

        /*
         * Every living custom entity must have a set of default attributes
         * associated with its entity type.
         */
        FabricDefaultAttributeRegistry.register(
                ORE_MIMIC,
                OreMimicEntity.createAttributes()
        );
        FabricDefaultAttributeRegistry.register(
                SABLE,
                SableEntity.createAttributes()
        );
        FabricDefaultAttributeRegistry.register(
                GLOW_TROUT,
                GlowTroutEntity.createAttributes()
        );

        FabricDefaultAttributeRegistry.register(
                BRIGHT_SALMON,
                BrightSalmonEntity.createAttributes()
        );
    }
}