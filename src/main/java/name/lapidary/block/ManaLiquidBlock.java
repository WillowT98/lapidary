package name.lapidary.block;

import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;

public final class ManaLiquidBlock extends LiquidBlock {

    public ManaLiquidBlock(
            FlowingFluid fluid,
            BlockBehaviour.Properties properties
    ) {
        super(fluid, properties);
    }
}