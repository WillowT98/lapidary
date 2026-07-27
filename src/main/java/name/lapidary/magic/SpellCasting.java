package name.lapidary.magic;

import name.lapidary.magic.focus.SpellcastingFocusHelper;
import name.lapidary.magic.spell.ModSpells;
import name.lapidary.magic.spell.SpellCastContext;
import name.lapidary.magic.spell.SpellCastingMode;
import name.lapidary.magic.spell.SpellDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class SpellCasting {
    private static final Map<UUID, Long> LAST_CHANNEL_PULSE = new HashMap<>();
    private static final long MIN_CHANNEL_INTERVAL = 4L;

    private SpellCasting() {
    }

    public static void castSelected(ServerPlayer player) {
        castSelected(player, false);
    }

    /**
     * @param channelPulse true only for repeated held-button pulses
     */
    public static void castSelected(
            ServerPlayer player,
            boolean channelPulse
    ) {
        if (player.isSpectator()) {
            return;
        }

        ItemStack focusStack = player.getMainHandItem();
        if (!SpellcastingFocusHelper.isFocus(focusStack)) {
            return;
        }

        PlayerMagic.ensureStartingSpells(player);
        PlayerMagicData magicData = PlayerMagic.get(player);
        Optional<ResourceLocation> selected = magicData.selectedSpell();
        if (selected.isEmpty()) {
            if (!channelPulse) {
                displayFailure(player, "message.lapidary.magic.no_selected_spell");
            }
            return;
        }

        Optional<SpellDefinition> definitionOptional = ModSpells.get(selected.get());
        if (definitionOptional.isEmpty()
                || !magicData.knowsSpell(selected.get())) {
            if (!channelPulse) {
                displayFailure(player, "message.lapidary.magic.unknown_spell");
            }
            return;
        }

        SpellDefinition spell = definitionOptional.get();
        if (channelPulse && spell.castingMode() != SpellCastingMode.CHANNELLED) {
            return;
        }

        if (channelPulse) {
            long now = player.serverLevel().getGameTime();
            long last = LAST_CHANNEL_PULSE.getOrDefault(player.getUUID(), Long.MIN_VALUE);
            if (now - last < MIN_CHANNEL_INTERVAL) {
                return;
            }
            LAST_CHANNEL_PULSE.put(player.getUUID(), now);
        }

        SpellCastContext context = new SpellCastContext(player, focusStack, spell);
        if (!spell.effect().canCast(context)) {
            if (!channelPulse) {
                displayFailure(player, "message.lapidary.magic.invalid_target");
            }
            return;
        }

        ManaAccess.Result manaResult = ManaAccess.tryConsume(player, spell.manaCost());
        if (manaResult != ManaAccess.Result.SUCCESS) {
            if (!channelPulse) {
                displayManaFailure(player, manaResult);
            }
            return;
        }

        spell.effect().cast(context);
    }

    private static void displayManaFailure(
            ServerPlayer player,
            ManaAccess.Result result
    ) {
        String translationKey = switch (result) {
            case NO_BACKPACK -> "message.lapidary.magic.no_backpack";
            case NO_CANISTER -> "message.lapidary.magic.no_canister";
            case WRONG_LIQUID -> "message.lapidary.magic.wrong_liquid";
            case NOT_ENOUGH_MANA -> "message.lapidary.magic.not_enough_mana";
            case SUCCESS -> null;
        };
        if (translationKey != null) {
            displayFailure(player, translationKey);
        }
    }

    private static void displayFailure(
            ServerPlayer player,
            String translationKey
    ) {
        player.displayClientMessage(Component.translatable(translationKey), true);
    }
}
