package name.lapidary.screen;

import name.lapidary.block.entity.DisplayCaseBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;

public final class DisplayCaseMenu
        extends DisplayStorageMenu {

    public DisplayCaseMenu(
            int containerId,
            Inventory playerInventory
    ) {
        this(
                containerId,
                playerInventory,
                clientInventory(
                        DisplayCaseBlockEntity.SIZE
                )
        );
    }

    public DisplayCaseMenu(
            int containerId,
            Inventory playerInventory,
            Container inventory
    ) {
        super(
                ModMenus.DISPLAY_CASE,
                containerId,
                playerInventory,
                inventory,
                DisplayCaseBlockEntity.SIZE,
                9,
                3,
                8,
                18,
                85,
                167,
                64,
                stack -> true
        );
    }
}
