package name.lapidary.progression;

import name.lapidary.network.InsightSyncPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class LapidaryInsight {

    /*
     * The provisional total number of unique discoveries.
     *
     * Ten gems multiplied by ten cuts gives 100 possible combinations,
     * assuming every combination grants exactly one Insight.
     */
    public static final int MAX_INSIGHT = 100;

    private LapidaryInsight() {
    }

    /**
     * Returns the player's current Insight, clamped to the valid range.
     */
    public static int get(ServerPlayer player) {
        int storedValue = player.getAttachedOrCreate(
                ModAttachments.LAPIDARY_INSIGHT
        );

        return clamp(storedValue);
    }

    /**
     * Replaces the player's Insight and immediately sends the new value
     * to that player's client.
     *
     * @return the player's new Insight total
     */
    public static int set(ServerPlayer player, int amount) {
        int safeAmount = clamp(amount);

        player.setAttached(
                ModAttachments.LAPIDARY_INSIGHT,
                safeAmount
        );

        sync(player);

        return safeAmount;
    }

    /**
     * Adds Insight without allowing the total to go below zero or above
     * the maximum.
     *
     * @return the player's new Insight total
     */
    public static int add(ServerPlayer player, int amount) {
        long calculatedTotal = (long) get(player) + amount;

        int safeTotal = (int) Math.max(
                0L,
                Math.min(MAX_INSIGHT, calculatedTotal)
        );

        return set(player, safeTotal);
    }

    /**
     * Sends the authoritative server value to this player's client.
     */
    public static void sync(ServerPlayer player) {
        if (!ServerPlayNetworking.canSend(
                player,
                InsightSyncPayload.TYPE
        )) {
            return;
        }

        ServerPlayNetworking.send(
                player,
                new InsightSyncPayload(get(player))
        );
    }

    private static int clamp(int amount) {
        return Math.max(
                0,
                Math.min(MAX_INSIGHT, amount)
        );
    }
}