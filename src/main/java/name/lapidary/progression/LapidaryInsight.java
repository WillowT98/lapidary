package name.lapidary.progression;

import net.minecraft.server.level.ServerPlayer;

public final class LapidaryInsight {

    private LapidaryInsight() {
    }

    /**
     * Returns the player's current Lapidary Insight.
     *
     * A player who does not yet have a stored value is automatically
     * initialized to 0.
     */
    public static int get(ServerPlayer player) {
        return player.getAttachedOrCreate(
                ModAttachments.LAPIDARY_INSIGHT
        );
    }

    /**
     * Replaces the player's Insight with an exact value.
     *
     * Insight is never permitted to fall below zero.
     *
     * @return the player's new Insight total
     */
    public static int set(ServerPlayer player, int amount) {
        int safeAmount = Math.max(0, amount);

        player.setAttached(
                ModAttachments.LAPIDARY_INSIGHT,
                safeAmount
        );

        return safeAmount;
    }

    /**
     * Adds an amount to the player's existing Insight.
     *
     * The long calculation prevents integer overflow if an extremely
     * large amount is ever supplied.
     *
     * @return the player's new Insight total
     */
    public static int add(ServerPlayer player, int amount) {
        long calculatedTotal = (long) get(player) + amount;

        long clampedTotal = Math.max(
                0L,
                Math.min(Integer.MAX_VALUE, calculatedTotal)
        );

        return set(player, (int) clampedTotal);
    }
}