package name.lapidary.magic.spell;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * Metadata shared by every registered staff spell.
 *
 * Actual casting behavior will be added later. For now, a spell only
 * needs an ID and a translated display name.
 */
public record SpellDefinition(
        ResourceLocation id
) {

    public SpellDefinition {
        Objects.requireNonNull(
                id,
                "Spell ID cannot be null"
        );
    }

    public String translationKey() {
        return "spell."
                + id.getNamespace()
                + "."
                + id.getPath()
                .replace('/', '.');
    }

    public Component displayName() {
        return Component.translatable(
                translationKey()
        );
    }
}