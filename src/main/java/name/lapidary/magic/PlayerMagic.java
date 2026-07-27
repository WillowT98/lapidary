package name.lapidary.magic;

import name.lapidary.magic.spell.ModSpells;
import name.lapidary.magic.spell.SpellDefinition;
import name.lapidary.network.MagicStatePayload;
import name.lapidary.progression.ModAttachments;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

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
     * Testing pass: grant every registered ordinary spell.
     * Mage Light remains the only spell auto-prepared when all slots are empty.
     */
    public static boolean ensureStartingSpells(ServerPlayer player) {
        PlayerMagicData current = get(player);
        PlayerMagicData updated = current;
        boolean changed = false;

        for (SpellDefinition spell : ModSpells.values()) {
            if (!updated.knowsSpell(spell.id())) {
                updated = updated.withKnownSpell(spell.id());
                changed = true;
            }
        }

        boolean hasPreparedSpell = updated.preparedSpells().stream()
                .anyMatch(value -> value != null && !value.isBlank());
        if (!hasPreparedSpell) {
            updated = updated
                    .withPreparedSpell(0, ModSpells.MAGE_LIGHT.id())
                    .withSelectedSlot(0);
            changed = true;
        }

        if (changed) {
            player.setAttached(ModAttachments.PLAYER_MAGIC, updated);
        }
        return changed;
    }

    public static boolean learnSpell(ServerPlayer player, ResourceLocation spellId) {
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

    public static boolean learnRitual(ServerPlayer player, ResourceLocation ritualId) {
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

    public static boolean clearPreparedSlot(ServerPlayer player, int slot) {
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

    public static boolean selectSlot(ServerPlayer player, int slot) {
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
            ServerPlayNetworking.send(player, new MagicStatePayload(get(player)));
        }
    }
}
