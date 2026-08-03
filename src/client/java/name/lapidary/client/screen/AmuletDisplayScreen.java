package name.lapidary.client.screen;

import name.lapidary.screen.AmuletDisplayMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class AmuletDisplayScreen
        extends DisplayStorageScreen<AmuletDisplayMenu> {

    public AmuletDisplayScreen(
            AmuletDisplayMenu menu,
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
