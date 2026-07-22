package name.lapidary.block;

import name.lapidary.screen.GemCutterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StonecutterBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class GemCutterBlock extends StonecutterBlock {

    private static final Component CONTAINER_TITLE =
            Component.translatable(
                    "container.lapidary.gem_cutter"
            );

    public GemCutterBlock(
            BlockBehaviour.Properties properties
    ) {
        super(properties);
    }

    /*
     * StonecutterBlock already handles right-clicking and facing.
     * We only replace the menu that it opens.
     */
    @Override
    protected MenuProvider getMenuProvider(
            BlockState state,
            Level level,
            BlockPos position
    ) {
        return new SimpleMenuProvider(
                (containerId, playerInventory, player) ->
                        new GemCutterMenu(
                                containerId,
                                playerInventory,
                                ContainerLevelAccess.create(
                                        level,
                                        position
                                )
                        ),
                CONTAINER_TITLE
        );
    }
}