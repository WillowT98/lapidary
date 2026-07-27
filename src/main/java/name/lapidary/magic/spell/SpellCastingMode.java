package name.lapidary.magic.spell;

/**
 * Determines how the staff input system invokes a spell.
 */
public enum SpellCastingMode {
    /** Cast exactly once when the attack button is pressed. */
    INSTANT,

    /** Cast once immediately and again in mana-costed pulses while held. */
    CHANNELLED
}
