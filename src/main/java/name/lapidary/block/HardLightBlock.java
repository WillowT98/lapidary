package name.lapidary.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;

/** A temporary solid construct created by the Hard Light spell. */
public final class HardLightBlock extends TransparentBlock {
    public static final MapCodec<HardLightBlock> CODEC =
            simpleCodec(HardLightBlock::new);

    public HardLightBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends HardLightBlock> codec() {
        return CODEC;
    }

    @Override
    protected void tick(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random
    ) {
        if (level.getBlockState(pos).is(this)) {
            level.removeBlock(pos, false);
        }
    }
}