package name.lapidary.magic;

import name.lapidary.magic.focus.SpellcastingFocusHelper;
import name.lapidary.magic.spell.ModSpells;
import name.lapidary.magic.spell.SpellCastContext;
import name.lapidary.magic.spell.SpellDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class SpellCasting {

    private SpellCasting() {
    }

    /**
     * Attempts to cast the spell currently selected in the player's
     * prepared-spell loadout.
     *
     * This method is always server-authoritative.
     */
    public static void castSelected(
            ServerPlayer player
    ) {
        if (player.isSpectator()) {
            return;
        }

        ItemStack focusStack =
                player.getMainHandItem();

        if (!SpellcastingFocusHelper.isFocus(
                focusStack
        )) {
            return;
        }

        PlayerMagic.ensureStartingSpells(
                player
        );

        PlayerMagicData magicData =
                PlayerMagic.get(player);

        Optional<ResourceLocation>
                selectedSpellOptional =
                magicData.selectedSpell();

        if (selectedSpellOptional.isEmpty()) {
            displayFailure(
                    player,
                    "message.lapidary.magic.no_selected_spell"
            );

            return;
        }

        ResourceLocation spellId =
                selectedSpellOptional.get();

        Optional<SpellDefinition>
                definitionOptional =
                ModSpells.get(spellId);

        if (definitionOptional.isEmpty()
                || !magicData.knowsSpell(
                spellId
        )) {
            displayFailure(
                    player,
                    "message.lapidary.magic.unknown_spell"
            );

            return;
        }

        SpellDefinition spell =
                definitionOptional.get();

        SpellCastContext context =
                new SpellCastContext(
                        player,
                        focusStack,
                        spell
                );

        if (!spell.effect()
                .canCast(context)) {

            displayFailure(
                    player,
                    "message.lapidary.magic.invalid_target"
            );

            return;
        }

        ManaAccess.Result manaResult =
                ManaAccess.tryConsume(
                        player,
                        spell.manaCost()
                );

        if (manaResult
                != ManaAccess.Result.SUCCESS) {

            displayManaFailure(
                    player,
                    manaResult
            );

            return;
        }

        spell.effect().cast(
                context
        );
    }

    private static void displayManaFailure(
            ServerPlayer player,
            ManaAccess.Result result
    ) {
        String translationKey =
                switch (result) {
                    case NO_BACKPACK ->
                            "message.lapidary.magic.no_backpack";

                    case NO_CANISTER ->
                            "message.lapidary.magic.no_canister";

                    case WRONG_LIQUID ->
                            "message.lapidary.magic.wrong_liquid";

                    case NOT_ENOUGH_MANA ->
                            "message.lapidary.magic.not_enough_mana";

                    case SUCCESS ->
                            null;
                };

        if (translationKey != null) {
            displayFailure(
                    player,
                    translationKey
            );
        }
    }

    private static void displayFailure(
            ServerPlayer player,
            String translationKey
    ) {
        player.displayClientMessage(
                Component.translatable(
                        translationKey
                ),
                true
        );
    }
}