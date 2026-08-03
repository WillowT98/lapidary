package name.lapidary.block.entity;

import name.lapidary.display.DisplayKind;
import name.lapidary.screen.RingDisplayMenu;
import name.lapidary.tag.ModItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class RingDisplayBlockEntity
        extends DisplayStorageBlockEntity {

    public static final int SIZE = 16;

    public RingDisplayBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        super(
                ModBlockEntities.RING_DISPLAY,
                position,
                state,
                SIZE,
                1
        );
    }

    @Override
    public DisplayKind getDisplayKind() {
        return DisplayKind.RING;
    }

    @Override
    protected boolean acceptsItem(
            ItemStack stack
    ) {
        return stack.is(
                ModItemTags.RINGS
        );
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(
                "container.lapidary.ring_display"
        );
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId,
            Inventory playerInventory,
            Player player
    ) {
        return new RingDisplayMenu(
                containerId,
                playerInventory,
                this
        );
    }
}
