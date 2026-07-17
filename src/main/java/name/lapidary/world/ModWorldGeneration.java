package name.lapidary.world;

import name.lapidary.Lapidary;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

public final class ModWorldGeneration {

    /*
     * These are the five vanilla placed features responsible for naturally
     * generating gold ore in the Overworld and Nether.
     */
    private static final List<ResourceKey<PlacedFeature>> GOLD_ORE_FEATURES = List.of(
            vanillaPlacedFeature("ore_gold"),
            vanillaPlacedFeature("ore_gold_lower"),
            vanillaPlacedFeature("ore_gold_extra"),
            vanillaPlacedFeature("ore_gold_nether"),
            vanillaPlacedFeature("ore_gold_deltas")
    );

    private ModWorldGeneration() {
        // Prevent this utility class from being instantiated.
    }

    public static void initialize() {
        BiomeModifications.create(
                        ResourceLocation.fromNamespaceAndPath(
                                Lapidary.MOD_ID,
                                "remove_gold_ores"
                        )
                )
                .add(
                        ModificationPhase.REMOVALS,
                        BiomeSelectors.all(),
                        context -> {
                            for (ResourceKey<PlacedFeature> feature : GOLD_ORE_FEATURES) {
                                context.getGenerationSettings().removeFeature(feature);
                            }
                        }
                );
    }

    private static ResourceKey<PlacedFeature> vanillaPlacedFeature(String path) {
        return ResourceKey.create(
                Registries.PLACED_FEATURE,
                ResourceLocation.fromNamespaceAndPath("minecraft", path)
        );
    }
}