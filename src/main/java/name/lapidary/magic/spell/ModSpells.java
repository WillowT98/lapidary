package name.lapidary.magic.spell;

import name.lapidary.Lapidary;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ModSpells {

    private static final Map<
            ResourceLocation,
            SpellDefinition
            > SPELLS =
            new LinkedHashMap<>();

    /*
     * No spells are registered yet.
     *
     * Later, declarations will look like:
     *
     * public static final SpellDefinition LIGHT =
     *         register("light");
     */

    private ModSpells() {
    }

    public static void initialize() {
        /*
         * Referencing this class causes future static spell fields
         * to be initialized before networking or player data tries
         * to resolve them.
         */
        Lapidary.LOGGER.info(
                "Registered {} Lapidary spells",
                SPELLS.size()
        );
    }

    public static SpellDefinition register(
            String path
    ) {
        return register(
                Lapidary.id(path)
        );
    }

    public static SpellDefinition register(
            ResourceLocation id
    ) {
        if (SPELLS.containsKey(id)) {
            throw new IllegalArgumentException(
                    "Duplicate spell ID: " + id
            );
        }

        SpellDefinition definition =
                new SpellDefinition(id);

        SPELLS.put(
                id,
                definition
        );

        return definition;
    }

    public static Optional<SpellDefinition> get(
            ResourceLocation id
    ) {
        return Optional.ofNullable(
                SPELLS.get(id)
        );
    }

    public static boolean contains(
            ResourceLocation id
    ) {
        return SPELLS.containsKey(id);
    }

    public static List<SpellDefinition> values() {
        return List.copyOf(
                SPELLS.values()
        );
    }
}