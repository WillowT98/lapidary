package name.lapidary.block;

import name.lapidary.screen.StainedGlassFabricatorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class StainedGlassFabricatorBlock
        extends CraftingTableBlock {

    private static final Component TITLE =
            Component.translatable(
                    "container.lapidary.stained_glass_fabricator"
            );

    public StainedGlassFabricatorBlock(
            BlockBehaviour.Properties properties
    ) {
        super(
                properties
        );
    }

    @Override
    protected MenuProvider getMenuProvider(
            BlockState state,
            Level level,
            BlockPos pos
    ) {
        return new SimpleMenuProvider(
                (
                        int syncId,
                        Inventory inventory,
                        Player player
                ) -> new StainedGlassFabricatorMenu(
                        syncId,
                        inventory,
                        ContainerLevelAccess.create(
                                level,
                                pos
                        )
                ),
                TITLE
        );
    }
}
