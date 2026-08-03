package name.lapidary.client.screen;

import name.lapidary.screen.DisplayCaseMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class DisplayCaseScreen
        extends DisplayStorageScreen<DisplayCaseMenu> {

    public DisplayCaseScreen(
            DisplayCaseMenu menu,
            Inventory playerInventory,
            Component title
    ) {
        super(
                menu,
                playerInventory,
                title
        );
    }
}
