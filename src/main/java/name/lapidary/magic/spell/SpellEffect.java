package name.lapidary.magic.spell;

/**
 * Executable behavior belonging to a registered staff spell.
 *
 * Spell-specific validation happens before mana is consumed.
 */
@FunctionalInterface
public interface SpellEffect {

    /**
     * Performs the spell after the general casting system has
     * validated the player and consumed the spell's mana cost.
     */
    void cast(SpellCastContext context);

    /**
     * Returns whether this particular spell may currently be cast.
     *
     * Most straightforward spells can use the default implementation.
     * Teleports, summons, and other target-sensitive spells can
     * override this later.
     */
    default boolean canCast(SpellCastContext context) {
        return true;
    }
}