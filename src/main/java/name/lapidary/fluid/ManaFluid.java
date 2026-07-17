package name.lapidary.fluid;

import name.lapidary.block.ModBlocks;
import name.lapidary.item.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.WaterFluid;

public abstract class ManaFluid extends WaterFluid {

    /**
     * Returns the registered flowing form of mana.
     */
    @Override
    public Fluid getFlowing() {
        return ModFluids.FLOWING_MANA;
    }

    /**
     * Returns the registered source form of mana.
     */
    @Override
    public Fluid getSource() {
        return ModFluids.MANA;
    }

    /**
     * Determines what filled bucket is created when mana is collected.
     */
    @Override
    public Item getBucket() {
        return ModItems.MANA_BUCKET;
    }

    /**
     * Converts a fluid state into the corresponding in-world block state.
     */
    @Override
    public BlockState createLegacyBlock(FluidState state) {
        return ModBlocks.MANA.defaultBlockState()
                .setValue(
                        LiquidBlock.LEVEL,
                        getLegacyLevel(state)
                );
    }

    /**
     * Tells Minecraft that both registered forms belong to the same fluid.
     */
    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == ModFluids.MANA
                || fluid == ModFluids.FLOWING_MANA;
    }

    /**
     * Prevents the familiar two-source-block infinite-water behavior.
     *
     * This makes mana finite for now.
     */
    @Override
    protected boolean canConvertToSource(Level level) {
        return false;
    }

    /**
     * Represents non-source mana at flow levels 1 through 7.
     */
    public static final class Flowing extends ManaFluid {

        public Flowing() {
            registerDefaultState(
                    getStateDefinition()
                            .any()
                            .setValue(LEVEL, 7)
                            .setValue(FALLING, false)
            );
        }

        @Override
        protected void createFluidStateDefinition(
                StateDefinition.Builder<Fluid, FluidState> builder
        ) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }

    /**
     * Represents a full mana source block.
     */
    public static final class Source extends ManaFluid {

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }
}