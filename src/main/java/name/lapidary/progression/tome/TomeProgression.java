package name.lapidary.progression.tome;

import name.lapidary.network.TomeOpenPayload;
import name.lapidary.network.TomeStatePayload;
import name.lapidary.progression.LapidaryInsight;
import name.lapidary.progression.ModAttachments;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public final class TomeProgression {

    private TomeProgression() {
    }

    public static long getPurchasedMask(
            ServerPlayer player
    ) {
        return player.getAttachedOrCreate(
                ModAttachments.TOME_PURCHASES
        );
    }

    public static boolean hasPurchased(
            ServerPlayer player,
            TomeNode node
    ) {
        return TomeTree.isOwned(
                getPurchasedMask(player),
                node
        );
    }

    /**
     * Sends the initial state and asks the client to open the Tome.
     */
    public static void openScreen(
            ServerPlayer player,
            BlockPos tablePosition
    ) {
        if (!ServerPlayNetworking.canSend(
                player,
                TomeOpenPayload.TYPE
        )) {
            return;
        }

        ServerPlayNetworking.send(
                player,
                new TomeOpenPayload(
                        tablePosition,
                        LapidaryInsight.get(player),
                        getPurchasedMask(player)
                )
        );
    }

    /**
     * Sends refreshed values to an already-open Tome screen.
     */
    public static void syncOpenScreen(
            ServerPlayer player
    ) {
        if (!ServerPlayNetworking.canSend(
                player,
                TomeStatePayload.TYPE
        )) {
            return;
        }

        ServerPlayNetworking.send(
                player,
                new TomeStatePayload(
                        LapidaryInsight.get(player),
                        getPurchasedMask(player)
                )
        );
    }

    /**
     * Server-authoritative purchase attempt.
     */
    public static boolean tryPurchase(
            ServerPlayer player,
            int nodeIndex
    ) {
        TomeNode node =
                TomeTree.getByIndex(nodeIndex);

        if (node == null || node.root()) {
            return false;
        }

        long purchasedMask =
                getPurchasedMask(player);

        /*
         * A node cannot be bought more than once.
         */
        if (TomeTree.isOwned(
                purchasedMask,
                node
        )) {
            return false;
        }

        /*
         * All prerequisites must be owned.
         */
        if (!TomeTree.prerequisitesMet(
                purchasedMask,
                node
        )) {
            return false;
        }

        int currentInsight =
                LapidaryInsight.get(player);

        if (currentInsight < node.cost()) {
            return false;
        }

        long newMask =
                TomeTree.addNode(
                        purchasedMask,
                        node
                );

        /*
         * Save ownership first. The subsequent Insight update also
         * synchronizes the new Insight total to the normal HUD.
         */
        player.setAttached(
                ModAttachments.TOME_PURCHASES,
                newMask
        );

        LapidaryInsight.add(
                player,
                -node.cost()
        );

        return true;
    }

    /**
     * Development helper for resetting dummy purchases.
     */
    public static void reset(
            ServerPlayer player
    ) {
        player.setAttached(
                ModAttachments.TOME_PURCHASES,
                0L
        );

        syncOpenScreen(player);
    }
}