package name.lapidary.item;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import name.lapidary.inventory.MageBackpackContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;

public final class MageBackpackAccess {

    private static final String CHEST_GROUP =
            "chest";

    private static final String BACK_SLOT =
            "back";

    private MageBackpackAccess() {
    }

    public static Optional<EquippedBackpack>
    findEquipped(
            Player player
    ) {
        Optional<TrinketComponent> componentOptional =
                TrinketsApi.getTrinketComponent(
                        player
                );

        if (componentOptional.isEmpty()) {
            return Optional.empty();
        }

        Map<
                String,
                Map<String, TrinketInventory>
                > inventories =
                componentOptional.get()
                        .getInventory();

        Map<String, TrinketInventory>
                chestInventories =
                inventories.get(
                        CHEST_GROUP
                );

        if (chestInventories == null) {
            return Optional.empty();
        }

        TrinketInventory backInventory =
                chestInventories.get(
                        BACK_SLOT
                );

        if (backInventory == null) {
            return Optional.empty();
        }

        for (int slot = 0;
             slot < backInventory
                     .getContainerSize();
             slot++) {

            ItemStack stack =
                    backInventory.getItem(slot);

            if (stack.is(
                    ModItems.MAGE_BACKPACK
            )) {
                return Optional.of(
                        new EquippedBackpack(
                                stack,
                                backInventory,
                                slot
                        )
                );
            }
        }

        return Optional.empty();
    }

    public static void openEquipped(
            ServerPlayer player
    ) {
        Optional<EquippedBackpack>
                equippedOptional =
                findEquipped(player);

        if (equippedOptional.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable(
                            "message.lapidary.no_mage_backpack"
                    ),
                    true
            );

            return;
        }

        EquippedBackpack equipped =
                equippedOptional.get();

        MageBackpackContainer container =
                new MageBackpackContainer(
                        equipped.stack(),
                        equipped.inventory(),
                        equipped.slotIndex()
                );

        player.openMenu(
                new SimpleMenuProvider(
                        (
                                containerId,
                                playerInventory,
                                menuPlayer
                        ) ->
                                new ChestMenu(
                                        MenuType.GENERIC_9x2,
                                        containerId,
                                        playerInventory,
                                        container,
                                        MageBackpackItem
                                                .INVENTORY_ROWS
                                ),
                        Component.translatable(
                                "container.lapidary.mage_backpack"
                        )
                )
        );
    }

    public record EquippedBackpack(
            ItemStack stack,
            TrinketInventory inventory,
            int slotIndex
    ) {
    }
}