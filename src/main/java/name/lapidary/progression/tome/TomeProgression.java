package name.lapidary.progression.tome;

import name.lapidary.magic.PlayerMagic;
import name.lapidary.network.TomeOpenPayload;
import name.lapidary.network.TomeStatePayload;
import name.lapidary.progression.LapidaryInsight;
import name.lapidary.progression.ModAttachments;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class TomeProgression {
    /**
     * Costs of obsolete spell-tree nodes. This includes the prototype nodes
     * and the brief Mage Light purchase node, since Mage Light is universal.
     * School-unlock nodes are deliberately absent because their IDs and
     * meaning are unchanged.
     */
    private static final Map<String, Integer> PLACEHOLDER_NODE_COSTS =
            Map.ofEntries(
                    Map.entry("summoning/lesser_calling", 5),
                    Map.entry("summoning/greater_calling", 5),
                    Map.entry("summoning/convergence", 20),

                    Map.entry("command/compel_one", 5),
                    Map.entry("command/compel_two", 10),
                    Map.entry("command/compel_three", 20),

                    Map.entry("nature/verdancy", 5),
                    Map.entry("nature/husbandry", 10),
                    Map.entry("nature/wild_communion", 15),

                    Map.entry("transmutation/material_study", 5),
                    Map.entry("transmutation/living_study", 5),
                    Map.entry("transmutation/perfect_exchange", 20),

                    Map.entry("warding/minor_ward", 5),
                    Map.entry("warding/fortified_ward", 10),
                    Map.entry("warding/greater_ward", 20),

                    Map.entry("divination/second_sight", 5),
                    Map.entry("divination/far_sight", 10),
                    Map.entry("divination/pattern_sight", 15),
                    Map.entry("divination/mage_light", 5),

                    Map.entry("illusion/glamour", 5),
                    Map.entry("illusion/phantasm", 10),
                    Map.entry("illusion/misdirection", 15),
                    Map.entry("illusion/living_mirage", 20),

                    Map.entry("passage/short_step", 5),
                    Map.entry("passage/waymark", 10),
                    Map.entry("passage/threshold", 10),
                    Map.entry("passage/crossing", 25),

                    Map.entry("griefing/fracture", 5),
                    Map.entry("griefing/ruin", 10),
                    Map.entry("griefing/desolation", 20),

                    Map.entry("necromancy/mortal_memory", 5),
                    Map.entry("necromancy/raise_lesser_dead", 15),
                    Map.entry("necromancy/bind_spirit", 15)
            );

    private TomeProgression() {
    }

    public static List<String> getPurchasedNodeIds(ServerPlayer player) {
        migrateLegacyPurchases(player);
        migratePlaceholderTreePurchases(player);

        return List.copyOf(
                player.getAttachedOrCreate(
                        ModAttachments.TOME_PURCHASED_NODES
                )
        );
    }

    public static boolean hasPurchased(
            ServerPlayer player,
            TomeNode node
    ) {
        return TomeTree.isOwned(getPurchasedNodeIds(player), node);
    }

    public static void openScreen(
            ServerPlayer player,
            BlockPos tablePosition
    ) {
        List<String> purchasedNodeIds = getPurchasedNodeIds(player);

        /*
         * This also removes every spell granted by the old testing pass unless
         * its new Tome node has actually been purchased.
         */
        boolean magicChanged = reconcileSpellKnowledge(
                player,
                purchasedNodeIds
        );
        if (!magicChanged) {
            PlayerMagic.sync(player);
        }

        if (!ServerPlayNetworking.canSend(player, TomeOpenPayload.TYPE)) {
            return;
        }

        ServerPlayNetworking.send(
                player,
                new TomeOpenPayload(
                        tablePosition,
                        LapidaryInsight.get(player),
                        purchasedNodeIds
                )
        );
    }

    public static void syncOpenScreen(ServerPlayer player) {
        if (!ServerPlayNetworking.canSend(player, TomeStatePayload.TYPE)) {
            return;
        }

        List<String> purchasedNodeIds = getPurchasedNodeIds(player);
        ServerPlayNetworking.send(
                player,
                new TomeStatePayload(
                        LapidaryInsight.get(player),
                        purchasedNodeIds
                )
        );
    }

    /** Server-authoritative purchase operation. */
    public static boolean tryPurchase(
            ServerPlayer player,
            String nodeId
    ) {
        TomeNode node = TomeTree.getNode(nodeId);
        if (node == null || node.root()) {
            return false;
        }

        TomePage page = TomeTree.getPage(node.pageId());
        if (page == null) {
            return false;
        }

        List<String> purchasedNodeIds = getPurchasedNodeIds(player);

        /* Do not trust the client to access only visible pages. */
        if (!TomeTree.isPageUnlocked(purchasedNodeIds, page)) {
            return false;
        }

        if (TomeTree.isOwned(purchasedNodeIds, node)) {
            return false;
        }

        if (!TomeTree.prerequisitesMet(purchasedNodeIds, node)) {
            return false;
        }

        int currentInsight = LapidaryInsight.get(player);
        if (currentInsight < node.cost()) {
            return false;
        }

        List<String> newPurchasedNodeIds =
                new ArrayList<>(purchasedNodeIds);
        newPurchasedNodeIds.add(node.id());

        player.setAttached(
                ModAttachments.TOME_PURCHASED_NODES,
                List.copyOf(newPurchasedNodeIds)
        );

        LapidaryInsight.add(player, -node.cost());

        ResourceLocation spellReward = TomeTree.getSpellReward(node);
        if (spellReward != null) {
            PlayerMagic.learnSpell(player, spellReward);
        }

        return true;
    }

    /** Clears every purchase, removes its spells, and refunds its Insight. */
    public static ResetResult resetAndRefund(ServerPlayer player) {
        List<String> purchasedNodeIds = getPurchasedNodeIds(player);

        int nodesReset = 0;
        int refundValue = 0;

        for (String nodeId : purchasedNodeIds) {
            TomeNode node = TomeTree.getNode(nodeId);

            /*
             * Unknown IDs are still cleared, but cannot be assigned a refund
             * because their definitions no longer exist.
             */
            if (node == null || node.root()) {
                continue;
            }

            nodesReset++;
            refundValue += node.cost();
        }

        int previousInsight = LapidaryInsight.get(player);
        player.setAttached(
                ModAttachments.TOME_PURCHASED_NODES,
                List.of()
        );

        int newInsightTotal = LapidaryInsight.add(player, refundValue);
        int actualRefund = newInsightTotal - previousInsight;

        boolean magicChanged = reconcileSpellKnowledge(player, List.of());
        if (!magicChanged) {
            PlayerMagic.sync(player);
        }

        syncOpenScreen(player);

        return new ResetResult(
                nodesReset,
                actualRefund,
                newInsightTotal
        );
    }

    private static boolean reconcileSpellKnowledge(
            ServerPlayer player,
            List<String> purchasedNodeIds
    ) {
        List<ResourceLocation> earnedSpells = new ArrayList<>();

        for (String nodeId : purchasedNodeIds) {
            ResourceLocation reward = TomeTree.getSpellReward(nodeId);
            if (reward != null) {
                earnedSpells.add(reward);
            }
        }

        return PlayerMagic.reconcileManagedSpells(
                player,
                TomeTree.getManagedSpellIds(),
                earnedSpells
        );
    }

    /**
     * Refunds purchases from the old six-node prototype once, then clears its
     * legacy bitmask.
     */
    private static void migrateLegacyPurchases(ServerPlayer player) {
        long legacyMask = player.getAttachedOrCreate(
                ModAttachments.TOME_PURCHASES
        );
        if (legacyMask == 0L) {
            return;
        }

        int[] legacyCosts = {
                0,
                5,
                10,
                20,
                5,
                10,
                20
        };

        int refund = 0;
        for (int index = 1; index < legacyCosts.length; index++) {
            long bit = 1L << index;
            if ((legacyMask & bit) != 0L) {
                refund += legacyCosts[index];
            }
        }

        player.setAttached(ModAttachments.TOME_PURCHASES, 0L);
        if (refund > 0) {
            LapidaryInsight.add(player, refund);
        }
    }

    /**
     * Removes the superseded placeholder nodes while preserving purchased
     * school unlocks. Their old Insight costs are refunded exactly once.
     */
    private static void migratePlaceholderTreePurchases(
            ServerPlayer player
    ) {
        List<String> existing = player.getAttachedOrCreate(
                ModAttachments.TOME_PURCHASED_NODES
        );

        if (existing.isEmpty()) {
            return;
        }

        List<String> migrated = new ArrayList<>();
        int refund = 0;
        boolean changed = false;

        for (String nodeId : existing) {
            Integer oldCost = PLACEHOLDER_NODE_COSTS.get(nodeId);
            if (oldCost == null) {
                migrated.add(nodeId);
            } else {
                refund += oldCost;
                changed = true;
            }
        }

        if (!changed) {
            return;
        }

        player.setAttached(
                ModAttachments.TOME_PURCHASED_NODES,
                List.copyOf(migrated)
        );

        if (refund > 0) {
            LapidaryInsight.add(player, refund);
        }
    }

    public record ResetResult(
            int nodesReset,
            int insightRefunded,
            int newInsightTotal
    ) {
    }
}
