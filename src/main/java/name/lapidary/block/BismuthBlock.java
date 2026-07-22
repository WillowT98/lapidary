package name.lapidary.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class BismuthBlock extends Block {

    /*
     * One second between ignition and melting.
     */
    private static final int MELT_DELAY_TICKS = 20;

    public BismuthBlock(
            BlockBehaviour.Properties properties
    ) {
        super(properties);
    }

    @Override
    protected void onPlace(
            BlockState state,
            Level level,
            BlockPos position,
            BlockState previousState,
            boolean movedByPiston
    ) {
        super.onPlace(
                state,
                level,
                position,
                previousState,
                movedByPiston
        );

        scheduleMeltingIfBurning(
                level,
                position
        );
    }

    @Override
    protected void neighborChanged(
            BlockState state,
            Level level,
            BlockPos position,
            Block changedBlock,
            BlockPos changedPosition,
            boolean movedByPiston
    ) {
        super.neighborChanged(
                state,
                level,
                position,
                changedBlock,
                changedPosition,
                movedByPiston
        );

        scheduleMeltingIfBurning(
                level,
                position
        );
    }

    @Override
    protected void tick(
            BlockState state,
            ServerLevel level,
            BlockPos position,
            RandomSource random
    ) {
        if (!hasAdjacentFire(level, position)) {
            return;
        }

        /*
         * LiquidBlock's default level zero state represents a source.
         */
        level.setBlock(
                position,
                ModBlocks.MOLTEN_BISMUTH
                        .defaultBlockState(),
                Block.UPDATE_ALL
        );
    }

    private void scheduleMeltingIfBurning(
            Level level,
            BlockPos position
    ) {
        if (level.isClientSide
                || !hasAdjacentFire(level, position)) {
            return;
        }

        level.scheduleTick(
                position,
                this,
                MELT_DELAY_TICKS
        );
    }

    private static boolean hasAdjacentFire(
            BlockGetter level,
            BlockPos position
    ) {
        for (Direction direction : Direction.values()) {
            BlockState neighboringState =
                    level.getBlockState(
                            position.relative(direction)
                    );

            if (neighboringState.getBlock()
                    instanceof BaseFireBlock) {
                return true;
            }
        }

        return false;
    }
}