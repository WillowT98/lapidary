package name.lapidary.block.entity;

import name.lapidary.display.DisplayKind;
import name.lapidary.screen.AmuletDisplayMenu;
import name.lapidary.tag.ModItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class AmuletDisplayBlockEntity
        extends DisplayStorageBlockEntity {

    public static final int SIZE = 8;

    public AmuletDisplayBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        super(
                ModBlockEntities.AMULET_DISPLAY,
                position,
                state,
                SIZE,
                1
        );
    }

    @Override
    public DisplayKind getDisplayKind() {
        return DisplayKind.AMULET;
    }

    @Override
    protected boolean acceptsItem(
            ItemStack stack
    ) {
        return stack.is(
                ModItemTags.AMULETS
        );
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(
                "container.lapidary.amulet_display"
        );
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId,
            Inventory playerInventory,
            Player player
    ) {
        return new AmuletDisplayMenu(
                containerId,
                playerInventory,
                this
        );
    }
}
