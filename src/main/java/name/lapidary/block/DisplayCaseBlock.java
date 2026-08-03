package name.lapidary.block;

import com.mojang.serialization.MapCodec;
import name.lapidary.block.entity.DisplayCaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class DisplayCaseBlock
        extends WorkshopDisplayBlock {

    public static final MapCodec<DisplayCaseBlock> CODEC =
            simpleCodec(DisplayCaseBlock::new);

    public DisplayCaseBlock(
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
        return new DisplayCaseBlockEntity(
                position,
                state
        );
    }
}
