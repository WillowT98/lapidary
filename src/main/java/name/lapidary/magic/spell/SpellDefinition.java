package name.lapidary.magic.spell;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Registered metadata and behavior for a staff spell.
 */
public record SpellDefinition(
        ResourceLocation id,
        long manaCost,
        Supplier<ItemStack> iconSupplier,
        SpellEffect effect
) {

    public SpellDefinition {
        Objects.requireNonNull(
                id,
                "Spell ID cannot be null"
        );

        Objects.requireNonNull(
                iconSupplier,
                "Spell icon supplier cannot be null"
        );

        Objects.requireNonNull(
                effect,
                "Spell effect cannot be null"
        );

        if (manaCost < 0L) {
            throw new IllegalArgumentException(
                    "Spell mana cost cannot be negative: "
                            + manaCost
            );
        }
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

    /**
     * Returns a fresh stack for GUI rendering.
     */
    public ItemStack iconStack() {
        ItemStack stack =
                iconSupplier.get();

        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return stack.copy();
    }
}