package name.lapidary.block;

import name.lapidary.block.entity.ModBlockEntities;
import name.lapidary.block.entity.SableCacheBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

public final class SableCacheBlock extends ChestBlock {

    public SableCacheBlock(
            BlockBehaviour.Properties properties
    ) {
        /*
         * Keep using the custom cache block entity, which holds
         * the inventory and sable-specific information.
         */
        super(
                properties,
                () -> ModBlockEntities.SABLE_CACHE
        );
    }

    /*
     * ChestBlock normally uses ENTITYBLOCK_ANIMATED, which causes
     * Minecraft to render a chest model through a block-entity
     * renderer. Returning MODEL makes Minecraft use
     * assets/lapidary/models/block/sable_cache.json instead.
     */
    @Override
    protected RenderShape getRenderShape(
            BlockState state
    ) {
        return RenderShape.MODEL;
    }

    /*
     * Prevent a newly placed cache from connecting to another cache.
     */
    @Override
    public BlockState getStateForPlacement(
            BlockPlaceContext context
    ) {
        BlockState placedState =
                super.getStateForPlacement(context);

        if (placedState == null) {
            return null;
        }

        return placedState.setValue(
                TYPE,
                ChestType.SINGLE
        );
    }

    /*
     * Ensure neighbor updates can never turn the cache into the
     * left or right half of a double chest.
     *
     * Calling super preserves ChestBlock's waterlogging behavior.
     */
    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos position,
            BlockPos neighborPosition
    ) {
        BlockState updatedState =
                super.updateShape(
                        state,
                        direction,
                        neighborState,
                        level,
                        position,
                        neighborPosition
                );

        return updatedState.setValue(
                TYPE,
                ChestType.SINGLE
        );
    }

    @Override
    public SableCacheBlockEntity newBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        return new SableCacheBlockEntity(
                position,
                state
        );
    }

    @Override
    public void playerDestroy(
            Level level,
            Player player,
            BlockPos position,
            BlockState state,
            BlockEntity blockEntity,
            ItemStack tool
    ) {
        /*
         * Notify the sable while the block-entity information is still
         * available.
         */
        if (!level.isClientSide
                && blockEntity
                instanceof SableCacheBlockEntity cache) {

            cache.onBrokenBy(player);
        }

        super.playerDestroy(
                level,
                player,
                position,
                state,
                blockEntity,
                tool
        );
    }
}