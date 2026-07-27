package name.lapidary.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.TransparentBlock;

/** Blast-resistant glass which always drops itself through its loot table. */
public final class ReinforcedGlassBlock extends TransparentBlock {
    public static final MapCodec<ReinforcedGlassBlock> CODEC =
            simpleCodec(ReinforcedGlassBlock::new);

    public ReinforcedGlassBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends ReinforcedGlassBlock> codec() {
        return CODEC;
    }
}