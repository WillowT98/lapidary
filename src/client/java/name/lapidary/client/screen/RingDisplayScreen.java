package name.lapidary.client.screen;

import name.lapidary.screen.RingDisplayMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class RingDisplayScreen
        extends DisplayStorageScreen<RingDisplayMenu> {

    public RingDisplayScreen(
            RingDisplayMenu menu,
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
