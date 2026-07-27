package name.lapidary.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Temporary obsidian produced by Lava Walker. */
public final class FrostedObsidianBlock extends Block {
    public static final MapCodec<FrostedObsidianBlock> CODEC =
            simpleCodec(FrostedObsidianBlock::new);

    public FrostedObsidianBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends FrostedObsidianBlock> codec() {
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
            level.setBlockAndUpdate(
                    pos,
                    Blocks.LAVA.defaultBlockState()
            );
        }
    }
}
