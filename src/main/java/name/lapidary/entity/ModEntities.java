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
    }
}