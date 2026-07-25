package name.lapidary.magic;

import name.lapidary.magic.spell.ModSpells;
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
        return player.getAttachedOrCreate(
                ModAttachments.PLAYER_MAGIC
        );
    }

    public static void set(
            ServerPlayer player,
            PlayerMagicData data
    ) {
        player.setAttached(
                ModAttachments.PLAYER_MAGIC,
                data
        );

        sync(player);
    }

    public static boolean learnSpell(
            ServerPlayer player,
            ResourceLocation spellId
    ) {
        /*
         * Knowledge cannot contain arbitrary or misspelled IDs.
         */
        if (!ModSpells.contains(spellId)) {
            return false;
        }

        PlayerMagicData current =
                get(player);

        if (current.knowsSpell(spellId)) {
            return false;
        }

        set(
                player,
                current.withKnownSpell(spellId)
        );

        return true;
    }

    public static boolean learnRitual(
            ServerPlayer player,
            ResourceLocation ritualId
    ) {
        PlayerMagicData current = get(player);

        if (current.knowsRitual(ritualId)) {
            return false;
        }

        set(
                player,
                current.withKnownRitual(ritualId)
        );

        return true;
    }

    public static boolean prepareSpell(
            ServerPlayer player,
            int slot,
            ResourceLocation spellId
    ) {
        if (!PlayerMagicData.isValidSlot(slot)) {
            return false;
        }

        /*
         * Both conditions are required:
         *
         * 1. This spell still exists in the registry.
         * 2. This player has actually learned it.
         */
        if (!ModSpells.contains(spellId)) {
            return false;
        }

        PlayerMagicData current =
                get(player);

        if (!current.knowsSpell(spellId)) {
            return false;
        }

        set(
                player,
                current.withPreparedSpell(
                        slot,
                        spellId
                )
        );

        return true;
    }

    public static boolean clearPreparedSlot(
            ServerPlayer player,
            int slot
    ) {
        if (!PlayerMagicData.isValidSlot(slot)) {
            return false;
        }

        set(
                player,
                get(player).withoutPreparedSpell(slot)
        );

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

        set(
                player,
                get(player).withSwappedSlots(
                        firstSlot,
                        secondSlot
                )
        );

        return true;
    }

    public static boolean selectSlot(
            ServerPlayer player,
            int slot
    ) {
        if (!PlayerMagicData.isValidSlot(slot)) {
            return false;
        }

        set(
                player,
                get(player).withSelectedSlot(slot)
        );

        return true;
    }

    public static void sync(ServerPlayer player) {
        if (!ServerPlayNetworking.canSend(
                player,
                MagicStatePayload.TYPE
        )) {
            return;
        }

        ServerPlayNetworking.send(
                player,
                new MagicStatePayload(get(player))
        );
    }
}