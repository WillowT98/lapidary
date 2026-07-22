package name.lapidary.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class MoltenBismuthLiquidBlock
        extends LiquidBlock {

    public MoltenBismuthLiquidBlock(
            FlowingFluid fluid,
            BlockBehaviour.Properties properties
    ) {
        super(fluid, properties);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos position,
            BlockPos neighborPosition
    ) {
        FluidState moltenState =
                state.getFluidState();

        FluidState neighborFluid =
                neighborState.getFluidState();

        /*
         * Only source blocks cool into solid bismuth.
         * Flowing molten bismuth remains flowing.
         */
        if (moltenState.isSource()
                && isVanillaWater(neighborFluid)) {

            return ModBlocks.BISMUTH_BLOCK
                    .defaultBlockState();
        }

        return super.updateShape(
                state,
                direction,
                neighborState,
                level,
                position,
                neighborPosition
        );
    }

    private static boolean isVanillaWater(
            FluidState fluidState
    ) {
        return fluidState.getType() == Fluids.WATER
                || fluidState.getType() == Fluids.FLOWING_WATER;
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

        if (level.isClientSide) {
            return;
        }

        if (!state.getFluidState().isSource()) {
            return;
        }

        if (touchesVanillaWater(level, position)) {
            level.setBlock(
                    position,
                    ModBlocks.BISMUTH_BLOCK
                            .defaultBlockState(),
                    Block.UPDATE_ALL
            );
        }
    }

    private static boolean touchesVanillaWater(
            BlockGetter level,
            BlockPos position
    ) {
        for (Direction direction : Direction.values()) {
            FluidState neighboringFluid =
                    level.getFluidState(
                            position.relative(direction)
                    );

            if (isVanillaWater(neighboringFluid)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void entityInside(
            BlockState state,
            Level level,
            BlockPos position,
            Entity entity
    ) {
        if (level.isClientSide || entity.fireImmune()) {
            return;
        }

        /*
         * Lava uses the lava damage source and ignites entities.
         * Damage is called repeatedly while the entity remains inside.
         */
        entity.igniteForSeconds(15.0F);

        entity.hurt(
                level.damageSources().lava(),
                4.0F
        );
    }
}