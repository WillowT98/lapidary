package name.lapidary.magic.spell;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * Server-authoritative information available to a spell effect.
 */
public record SpellCastContext(
        ServerPlayer caster,
        ItemStack focusStack,
        SpellDefinition spell
) {

    public SpellCastContext {
        Objects.requireNonNull(
                caster,
                "Spell caster cannot be null"
        );

        Objects.requireNonNull(
                focusStack,
                "Spellcasting focus cannot be null"
        );

        Objects.requireNonNull(
                spell,
                "Spell definition cannot be null"
        );
    }
}