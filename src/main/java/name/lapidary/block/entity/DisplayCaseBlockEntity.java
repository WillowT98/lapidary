package name.lapidary.block.entity;

import name.lapidary.display.DisplayKind;
import name.lapidary.screen.DisplayCaseMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class DisplayCaseBlockEntity
        extends DisplayStorageBlockEntity {

    public static final int SIZE = 27;

    public DisplayCaseBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        super(
                ModBlockEntities.DISPLAY_CASE,
                position,
                state,
                SIZE,
                64
        );
    }

    @Override
    public DisplayKind getDisplayKind() {
        return DisplayKind.CASE;
    }

    @Override
    protected boolean acceptsItem(
            ItemStack stack
    ) {
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(
                "container.lapidary.display_case"
        );
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId,
            Inventory playerInventory,
            Player player
    ) {
        return new DisplayCaseMenu(
                containerId,
                playerInventory,
                this
        );
    }
}
