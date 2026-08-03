package name.lapidary.screen;

import name.lapidary.block.entity.AmuletDisplayBlockEntity;
import name.lapidary.tag.ModItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;

public final class AmuletDisplayMenu
        extends DisplayStorageMenu {

    public AmuletDisplayMenu(
            int containerId,
            Inventory playerInventory
    ) {
        this(
                containerId,
                playerInventory,
                clientInventory(
                        AmuletDisplayBlockEntity.SIZE
                )
        );
    }

    public AmuletDisplayMenu(
            int containerId,
            Inventory playerInventory,
            Container inventory
    ) {
        super(
                ModMenus.AMULET_DISPLAY,
                containerId,
                playerInventory,
                inventory,
                AmuletDisplayBlockEntity.SIZE,
                8,
                1,
                17,
                25,
                63,
                145,
                1,
                stack ->
                        stack.is(
                                ModItemTags.AMULETS
                        )
        );
    }
}
