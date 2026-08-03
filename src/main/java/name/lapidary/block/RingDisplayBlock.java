package name.lapidary.block;

import com.mojang.serialization.MapCodec;
import name.lapidary.block.entity.RingDisplayBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class RingDisplayBlock
        extends WorkshopDisplayBlock {

    public static final MapCodec<RingDisplayBlock> CODEC =
            simpleCodec(RingDisplayBlock::new);

    private static final VoxelShape SHAPE =
            box(
                    2.0D, 0.0D, 2.0D,
                    14.0D, 16.0D, 14.0D
            );

    public RingDisplayBlock(
            BlockBehaviour.Properties properties
    ) {
        super(properties);
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
        return new RingDisplayBlockEntity(
                position,
                state
        );
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return SHAPE;
    }
}
