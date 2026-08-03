package name.lapidary.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

public abstract class WorkshopDisplayBlock
        extends BaseEntityBlock {

    public static final DirectionProperty FACING =
            BlockStateProperties.HORIZONTAL_FACING;

    protected WorkshopDisplayBlock(
            BlockBehaviour.Properties properties
    ) {
        super(properties);

        registerDefaultState(
                stateDefinition.any()
                        .setValue(
                                FACING,
                                Direction.SOUTH
                        )
        );
    }

    @Override
    protected RenderShape getRenderShape(
            BlockState state
    ) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(
            BlockPlaceContext context
    ) {
        return defaultBlockState()
                .setValue(
                        FACING,
                        context.getHorizontalDirection()
                                .getOpposite()
                );
    }

    @Override
    protected BlockState rotate(
            BlockState state,
            Rotation rotation
    ) {
        return state.setValue(
                FACING,
                rotation.rotate(
                        state.getValue(FACING)
                )
        );
    }

    @Override
    protected BlockState mirror(
            BlockState state,
            Mirror mirror
    ) {
        return state.rotate(
                mirror.getRotation(
                        state.getValue(FACING)
                )
        );
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<
                    net.minecraft.world.level.block.Block,
                    BlockState
                    > builder
    ) {
        builder.add(FACING);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            BlockHitResult hitResult
    ) {
        if (!level.isClientSide
                && level.getBlockEntity(position)
                instanceof MenuProvider provider) {

            player.openMenu(provider);
        }

        return InteractionResult.sidedSuccess(
                level.isClientSide
        );
    }

    @Override
    protected void onRemove(
            BlockState state,
            Level level,
            BlockPos position,
            BlockState newState,
            boolean movedByPiston
    ) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity =
                    level.getBlockEntity(position);

            if (blockEntity
                    instanceof net.minecraft.world.Container
                    container) {

                net.minecraft.world.Containers
                        .dropContents(
                                level,
                                position,
                                container
                        );

                level.updateNeighbourForOutputSignal(
                        position,
                        this
                );
            }
        }

        super.onRemove(
                state,
                level,
                position,
                newState,
                movedByPiston
        );
    }

    @Override
    protected boolean hasAnalogOutputSignal(
            BlockState state
    ) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(
            BlockState state,
            Level level,
            BlockPos position
    ) {
        if (level.getBlockEntity(position)
                instanceof net.minecraft.world.Container
                container) {

            return AbstractContainerMenu
                    .getRedstoneSignalFromContainer(
                            container
                    );
        }

        return 0;
    }
}
