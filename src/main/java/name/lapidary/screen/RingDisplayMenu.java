package name.lapidary.screen;

import name.lapidary.block.entity.RingDisplayBlockEntity;
import name.lapidary.tag.ModItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;

public final class RingDisplayMenu
        extends DisplayStorageMenu {

    public RingDisplayMenu(
            int containerId,
            Inventory playerInventory
    ) {
        this(
                containerId,
                playerInventory,
                clientInventory(
                        RingDisplayBlockEntity.SIZE
                )
        );
    }

    public RingDisplayMenu(
            int containerId,
            Inventory playerInventory,
            Container inventory
    ) {
        super(
                ModMenus.RING_DISPLAY,
                containerId,
                playerInventory,
                inventory,
                RingDisplayBlockEntity.SIZE,
                4,
                4,
                52,
                18,
                104,
                186,
                1,
                stack ->
                        stack.is(
                                ModItemTags.RINGS
                        )
        );
    }
}
