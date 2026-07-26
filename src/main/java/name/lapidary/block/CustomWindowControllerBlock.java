package name.lapidary.block;

import com.mojang.serialization.MapCodec;
import name.lapidary.block.entity.CustomWindowControllerBlockEntity;
import name.lapidary.item.CustomStainedGlassItem;
import name.lapidary.window.WindowStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class CustomWindowControllerBlock
        extends BaseEntityBlock {

    public static final MapCodec<CustomWindowControllerBlock>
            CODEC =
            simpleCodec(
                    CustomWindowControllerBlock::new
            );

    public static final DirectionProperty FACING =
            HorizontalDirectionalBlock.FACING;

    private static final VoxelShape NORTH_SOUTH_SHAPE =
            box(
                    0.0D,
                    0.0D,
                    7.0D,
                    16.0D,
                    16.0D,
                    9.0D
            );

    private static final VoxelShape EAST_WEST_SHAPE =
            box(
                    7.0D,
                    0.0D,
                    0.0D,
                    9.0D,
                    16.0D,
                    16.0D
            );

    public CustomWindowControllerBlock(
            BlockBehaviour.Properties properties
    ) {
        super(
                properties
        );

        registerDefaultState(
                stateDefinition
                        .any()
                        .setValue(
                                FACING,
                                Direction.NORTH
                        )
        );
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock>
    codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        return new CustomWindowControllerBlockEntity(
                position,
                state
        );
    }

    @Override
    protected RenderShape getRenderShape(
            BlockState state
    ) {
        /*
         * The controller's block-entity renderer draws the complete
         * multiblock. The JSON model supplies only a particle texture.
         */
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return shapeFor(
                state.getValue(FACING)
        );
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return shapeFor(
                state.getValue(FACING)
        );
    }

    @Override
    public BlockState playerWillDestroy(
            Level level,
            BlockPos position,
            BlockState state,
            Player player
    ) {
        if (!level.isClientSide) {
            WindowStructure.dismantle(
                    level,
                    position,
                    state,
                    !player.getAbilities()
                            .instabuild
            );
        }

        return super.playerWillDestroy(
                level,
                position,
                state,
                player
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
        if (state.getBlock()
                != newState.getBlock()
                && !level.isClientSide
                && !WindowStructure
                .isDismantling()) {

            WindowStructure.dismantle(
                    level,
                    position,
                    state,
                    true
            );
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
    public ItemStack getCloneItemStack(
            LevelReader level,
            BlockPos position,
            BlockState state
    ) {
        return WindowStructure.findDesign(
                level,
                position,
                state
        ).map(
                CustomStainedGlassItem::create
        ).orElse(
                ItemStack.EMPTY
        );
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<
                    net.minecraft.world.level.block.Block,
                    BlockState
                    > builder
    ) {
        builder.add(
                FACING
        );
    }

    private static VoxelShape shapeFor(
            Direction facing
    ) {
        return facing.getAxis()
                == Direction.Axis.Z
                ? NORTH_SOUTH_SHAPE
                : EAST_WEST_SHAPE;
    }
}
