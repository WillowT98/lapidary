package name.lapidary.progression.tome;

import name.lapidary.network.TomeOpenPayload;
import name.lapidary.network.TomeStatePayload;
import name.lapidary.progression.LapidaryInsight;
import name.lapidary.progression.ModAttachments;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public final class TomeProgression {

    private TomeProgression() {
    }

    public static List<String> getPurchasedNodeIds(
            ServerPlayer player
    ) {
        migrateLegacyPurchases(player);

        return List.copyOf(
                player.getAttachedOrCreate(
                        ModAttachments
                                .TOME_PURCHASED_NODES
                )
        );
    }

    public static boolean hasPurchased(
            ServerPlayer player,
            TomeNode node
    ) {
        return TomeTree.isOwned(
                getPurchasedNodeIds(player),
                node
        );
    }

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

        List<String> purchasedNodeIds =
                getPurchasedNodeIds(player);

        ServerPlayNetworking.send(
                player,
                new TomeOpenPayload(
                        tablePosition,
                        LapidaryInsight.get(player),
                        purchasedNodeIds
                )
        );
    }

    public static void syncOpenScreen(
            ServerPlayer player
    ) {
        if (!ServerPlayNetworking.canSend(
                player,
                TomeStatePayload.TYPE
        )) {
            return;
        }

        List<String> purchasedNodeIds =
                getPurchasedNodeIds(player);

        ServerPlayNetworking.send(
                player,
                new TomeStatePayload(
                        LapidaryInsight.get(player),
                        purchasedNodeIds
                )
        );
    }

    /**
     * Server-authoritative purchase operation.
     */
    public static boolean tryPurchase(
            ServerPlayer player,
            String nodeId
    ) {
        TomeNode node =
                TomeTree.getNode(nodeId);

        if (node == null || node.root()) {
            return false;
        }

        TomePage page =
                TomeTree.getPage(
                        node.pageId()
                );

        if (page == null) {
            return false;
        }

        List<String> purchasedNodeIds =
                getPurchasedNodeIds(player);

        /*
         * Do not trust the client to access only visible pages.
         */
        if (!TomeTree.isPageUnlocked(
                purchasedNodeIds,
                page
        )) {
            return false;
        }

        if (TomeTree.isOwned(
                purchasedNodeIds,
                node
        )) {
            return false;
        }

        if (!TomeTree.prerequisitesMet(
                purchasedNodeIds,
                node
        )) {
            return false;
        }

        int currentInsight =
                LapidaryInsight.get(player);

        if (currentInsight < node.cost()) {
            return false;
        }

        List<String> newPurchasedNodeIds =
                new ArrayList<>(
                        purchasedNodeIds
                );

        newPurchasedNodeIds.add(
                node.id()
        );

        player.setAttached(
                ModAttachments.TOME_PURCHASED_NODES,
                List.copyOf(
                        newPurchasedNodeIds
                )
        );

        LapidaryInsight.add(
                player,
                -node.cost()
        );

        return true;
    }

    /**
     * Clears every purchase and refunds all currently defined nodes.
     */
    public static ResetResult resetAndRefund(
            ServerPlayer player
    ) {
        List<String> purchasedNodeIds =
                getPurchasedNodeIds(player);

        int nodesReset = 0;
        int refundValue = 0;

        for (String nodeId : purchasedNodeIds) {
            TomeNode node =
                    TomeTree.getNode(nodeId);

            /*
             * Unknown IDs are still cleared, but cannot be assigned
             * a refund because their definitions no longer exist.
             */
            if (node == null || node.root()) {
                continue;
            }

            nodesReset++;
            refundValue += node.cost();
        }

        int previousInsight =
                LapidaryInsight.get(player);

        player.setAttached(
                ModAttachments.TOME_PURCHASED_NODES,
                List.of()
        );

        int newInsightTotal =
                LapidaryInsight.add(
                        player,
                        refundValue
                );

        int actualRefund =
                newInsightTotal
                        - previousInsight;

        syncOpenScreen(player);

        return new ResetResult(
                nodesReset,
                actualRefund,
                newInsightTotal
        );
    }

    /**
     * Refunds purchases from the old six-node prototype once, then
     * clears its legacy bitmask.
     */
    private static void migrateLegacyPurchases(
            ServerPlayer player
    ) {
        long legacyMask =
                player.getAttachedOrCreate(
                        ModAttachments.TOME_PURCHASES
                );

        if (legacyMask == 0L) {
            return;
        }

        int refund = 0;

        /*
         * Old prototype costs:
         *
         * index 1: 5
         * index 2: 10
         * index 3: 20
         * index 4: 5
         * index 5: 10
         * index 6: 20
         */
        int[] legacyCosts = {
                0,
                5,
                10,
                20,
                5,
                10,
                20
        };

        for (int index = 1;
             index < legacyCosts.length;
             index++) {

            long bit =
                    1L << index;

            if ((legacyMask & bit) != 0L) {
                refund += legacyCosts[index];
            }
        }

        player.setAttached(
                ModAttachments.TOME_PURCHASES,
                0L
        );

        if (refund > 0) {
            LapidaryInsight.add(
                    player,
                    refund
            );
        }
    }

    public record ResetResult(
            int nodesReset,
            int insightRefunded,
            int newInsightTotal
    ) {
    }
}