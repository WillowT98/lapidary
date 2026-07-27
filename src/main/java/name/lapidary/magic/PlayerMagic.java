package name.lapidary.magic;

import name.lapidary.magic.spell.ModSpells;
import name.lapidary.network.MagicStatePayload;
import name.lapidary.progression.ModAttachments;
import name.lapidary.progression.tome.TomeTree;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class PlayerMagic {
    private PlayerMagic() {
    }

    public static PlayerMagicData get(Player player) {
        return player.getAttachedOrCreate(ModAttachments.PLAYER_MAGIC);
    }

    public static void set(ServerPlayer player, PlayerMagicData data) {
        player.setAttached(ModAttachments.PLAYER_MAGIC, data);
        sync(player);
    }

    /**
     * Replaces the old testing-pass auto-grant with authoritative Tome
     * ownership reconciliation. Mage Light is the universal starting spell
     * and is restored here if older data or a progression reset removed it.
     */
    public static boolean ensureStartingSpells(ServerPlayer player) {
        List<String> purchasedNodeIds = player.getAttachedOrCreate(
                ModAttachments.TOME_PURCHASED_NODES
        );

        List<ResourceLocation> earnedSpellIds = new ArrayList<>();
        for (String nodeId : purchasedNodeIds) {
            ResourceLocation reward = TomeTree.getSpellReward(nodeId);
            if (reward != null) {
                earnedSpellIds.add(reward);
            }
        }

        PlayerMagicData current = get(player);
        PlayerMagicData updated = reconciledData(
                current,
                TomeTree.getManagedSpellIds(),
                earnedSpellIds
        );

        if (updated.equals(current)) {
            return false;
        }

        /*
         * Do not call set() here: set() calls sync(), and sync() calls this
         * method. Write the attachment directly and let the current sync
         * continue normally.
         */
        player.setAttached(ModAttachments.PLAYER_MAGIC, updated);
        return true;
    }

    public static boolean learnSpell(
            ServerPlayer player,
            ResourceLocation spellId
    ) {
        if (!ModSpells.contains(spellId)) {
            return false;
        }

        PlayerMagicData current = get(player);
        if (current.knowsSpell(spellId)) {
            return false;
        }

        set(player, current.withKnownSpell(spellId));
        return true;
    }

    /**
     * Reconciles the set of Tome-controlled spells with the player's actual
     * Tome purchases.
     *
     * This removes the old testing-pass grants, learns all purchased rewards,
     * and clears prepared slots containing a spell that is no longer owned.
     * Spells not managed by the Tome are preserved.
     */
    public static boolean reconcileManagedSpells(
            ServerPlayer player,
            Collection<ResourceLocation> managedSpellIds,
            Collection<ResourceLocation> earnedSpellIds
    ) {
        PlayerMagicData current = get(player);
        PlayerMagicData updated = reconciledData(
                current,
                managedSpellIds,
                earnedSpellIds
        );

        if (updated.equals(current)) {
            return false;
        }

        set(player, updated);
        return true;
    }

    private static PlayerMagicData reconciledData(
            PlayerMagicData current,
            Collection<ResourceLocation> managedSpellIds,
            Collection<ResourceLocation> earnedSpellIds
    ) {
        Set<String> managed = new LinkedHashSet<>();
        for (ResourceLocation spellId : managedSpellIds) {
            if (spellId != null) {
                managed.add(spellId.toString());
            }
        }

        Set<String> earned = new LinkedHashSet<>();
        for (ResourceLocation spellId : earnedSpellIds) {
            if (spellId != null
                    && managed.contains(spellId.toString())
                    && ModSpells.contains(spellId)) {
                earned.add(spellId.toString());
            }
        }

        /*
         * Mage Light is universal rather than Tome-controlled. A linked set
         * prevents duplicates while preserving the player's existing order.
         */
        String mageLightId = ModSpells.MAGE_LIGHT.id().toString();
        Set<String> known = new LinkedHashSet<>();
        for (String knownSpell : current.knownSpells()) {
            if (!managed.contains(knownSpell)
                    || knownSpell.equals(mageLightId)) {
                known.add(knownSpell);
            }
        }
        known.add(mageLightId);
        known.addAll(earned);

        boolean mageLightWasKnown =
                current.knownSpells().contains(mageLightId);

        List<String> prepared = new ArrayList<>(current.preparedSpells());
        for (int slot = 0; slot < prepared.size(); slot++) {
            String preparedSpell = prepared.get(slot);
            if (managed.contains(preparedSpell)
                    && !earned.contains(preparedSpell)) {
                prepared.set(slot, PlayerMagicData.EMPTY_SLOT);
            }
        }

        int selectedSlot = current.selectedSlot();
        boolean hasPreparedSpell = prepared.stream()
                .anyMatch(value -> value != null && !value.isBlank());

        /*
         * Preserve the original starter behavior: the first time Mage Light
         * is granted, prepare it in slot zero only when the player has not
         * already configured any prepared spell.
         */
        if (!mageLightWasKnown && !hasPreparedSpell) {
            prepared.set(0, mageLightId);
            selectedSlot = 0;
        }

        return new PlayerMagicData(
                List.copyOf(known),
                current.knownRituals(),
                List.copyOf(prepared),
                selectedSlot
        );
    }

    public static boolean learnRitual(
            ServerPlayer player,
            ResourceLocation ritualId
    ) {
        PlayerMagicData current = get(player);
        if (current.knowsRitual(ritualId)) {
            return false;
        }

        set(player, current.withKnownRitual(ritualId));
        return true;
    }

    public static boolean prepareSpell(
            ServerPlayer player,
            int slot,
            ResourceLocation spellId
    ) {
        if (!PlayerMagicData.isValidSlot(slot)
                || !ModSpells.contains(spellId)) {
            return false;
        }

        PlayerMagicData current = get(player);
        if (!current.knowsSpell(spellId)) {
            return false;
        }

        PlayerMagicData updated = current.withPreparedSpell(slot, spellId);
        if (updated.equals(current)) {
            return false;
        }

        set(player, updated);
        return true;
    }

    public static boolean clearPreparedSlot(
            ServerPlayer player,
            int slot
    ) {
        if (!PlayerMagicData.isValidSlot(slot)) {
            return false;
        }

        PlayerMagicData current = get(player);
        PlayerMagicData updated = current.withoutPreparedSpell(slot);
        if (updated.equals(current)) {
            return false;
        }

        set(player, updated);
        return true;
    }

    public static boolean swapPreparedSlots(
            ServerPlayer player,
            int firstSlot,
            int secondSlot
    ) {
        if (!PlayerMagicData.isValidSlot(firstSlot)
                || !PlayerMagicData.isValidSlot(secondSlot)) {
            return false;
        }

        PlayerMagicData current = get(player);
        PlayerMagicData updated = current.withSwappedSlots(firstSlot, secondSlot);
        if (updated.equals(current)) {
            return false;
        }

        set(player, updated);
        return true;
    }

    public static boolean selectSlot(
            ServerPlayer player,
            int slot
    ) {
        if (!PlayerMagicData.isValidSlot(slot)) {
            return false;
        }

        PlayerMagicData current = get(player);
        PlayerMagicData updated = current.withSelectedSlot(slot);
        if (updated.equals(current)) {
            return false;
        }

        set(player, updated);
        return true;
    }

    public static void sync(ServerPlayer player) {
        ensureStartingSpells(player);

        if (ServerPlayNetworking.canSend(player, MagicStatePayload.TYPE)) {
            ServerPlayNetworking.send(
                    player,
                    new MagicStatePayload(get(player))
            );
        }
    }
}
