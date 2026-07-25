package name.lapidary.magic.spell;

import name.lapidary.Lapidary;
import name.lapidary.fluid.CanisterFluidStorage;
import name.lapidary.item.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public final class ModSpells {

    private static final Map<
            ResourceLocation,
            SpellDefinition
            > SPELLS =
            new LinkedHashMap<>();

    /**
     * Every Mage Light currently costs exactly one full bucket
     * of mana.
     */
    public static final SpellDefinition MAGE_LIGHT =
            register(
                    "mage_light",
                    CanisterFluidStorage.BUCKET,
                    () -> new ItemStack(
                            ModItems.MAGE_LIGHT_ORB
                    ),
                    MageLightSpell::cast
            );

    private ModSpells() {
    }

    public static void initialize() {
        Lapidary.LOGGER.info(
                "Registered {} Lapidary spells",
                SPELLS.size()
        );
    }

    public static SpellDefinition register(
            String path,
            long manaCost,
            Supplier<ItemStack> iconSupplier,
            SpellEffect effect
    ) {
        return register(
                Lapidary.id(path),
                manaCost,
                iconSupplier,
                effect
        );
    }

    public static SpellDefinition register(
            ResourceLocation id,
            long manaCost,
            Supplier<ItemStack> iconSupplier,
            SpellEffect effect
    ) {
        if (SPELLS.containsKey(id)) {
            throw new IllegalArgumentException(
                    "Duplicate spell ID: " + id
            );
        }

        SpellDefinition definition =
                new SpellDefinition(
                        id,
                        manaCost,
                        iconSupplier,
                        effect
                );

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