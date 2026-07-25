package name.lapidary.magic;

import name.lapidary.fluid.CanisterItemContents;
import name.lapidary.fluid.CanisterLiquid;
import name.lapidary.inventory.MageBackpackContainer;
import name.lapidary.item.MageBackpackAccess;
import name.lapidary.item.MageBackpackItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class ManaAccess {

    private ManaAccess() {
    }

    /**
     * Attempts to consume exactly the requested amount of mana from
     * the canister mounted in the player's equipped Mage Backpack.
     */
    public static Result tryConsume(
            ServerPlayer player,
            long requestedAmount
    ) {
        if (requestedAmount <= 0L) {
            return Result.SUCCESS;
        }

        Optional<MageBackpackAccess.EquippedBackpack>
                equippedOptional =
                MageBackpackAccess.findEquipped(
                        player
                );

        if (equippedOptional.isEmpty()) {
            return Result.NO_BACKPACK;
        }

        MageBackpackAccess.EquippedBackpack equipped =
                equippedOptional.get();

        MageBackpackContainer container =
                new MageBackpackContainer(
                        equipped.stack(),
                        equipped.inventory(),
                        equipped.slotIndex()
                );

        ItemStack mountedCanister =
                container.getItem(
                        MageBackpackItem
                                .CANISTER_SLOT_INDEX
                );

        if (mountedCanister.isEmpty()) {
            return Result.NO_CANISTER;
        }

        CanisterItemContents.Contents contents =
                CanisterItemContents.read(
                        mountedCanister
                );

        if (contents.isEmpty()) {
            return Result.NOT_ENOUGH_MANA;
        }

        if (contents.liquid()
                != CanisterLiquid.MANA) {

            return Result.WRONG_LIQUID;
        }

        if (contents.amount()
                < requestedAmount) {

            return Result.NOT_ENOUGH_MANA;
        }

        /*
         * Mutate a copy and only put it back into the backpack after
         * the entire extraction succeeds.
         */
        ItemStack updatedCanister =
                mountedCanister.copy();

        boolean extracted =
                CanisterItemContents.tryExtract(
                        updatedCanister,
                        CanisterLiquid.MANA,
                        requestedAmount
                );

        if (!extracted) {
            return Result.NOT_ENOUGH_MANA;
        }

        container.setItem(
                MageBackpackItem
                        .CANISTER_SLOT_INDEX,
                updatedCanister
        );

        return Result.SUCCESS;
    }

    public enum Result {
        SUCCESS,
        NO_BACKPACK,
        NO_CANISTER,
        WRONG_LIQUID,
        NOT_ENOUGH_MANA
    }
}