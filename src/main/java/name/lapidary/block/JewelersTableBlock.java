package name.lapidary.block;

import name.lapidary.screen.JewelersTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class JewelersTableBlock
        extends CraftingTableBlock {

    private static final Component CONTAINER_TITLE =
            Component.translatable(
                    "container.lapidary.jewelers_table"
            );

    public JewelersTableBlock(
            BlockBehaviour.Properties properties
    ) {
        super(properties);
    }

    @Override
    protected MenuProvider getMenuProvider(
            BlockState state,
            Level level,
            BlockPos position
    ) {
        return new SimpleMenuProvider(
                (containerId, inventory, player) ->
                        new JewelersTableMenu(
                                containerId,
                                inventory,
                                ContainerLevelAccess.create(
                                        level,
                                        position
                                )
                        ),
                CONTAINER_TITLE
        );
    }
}