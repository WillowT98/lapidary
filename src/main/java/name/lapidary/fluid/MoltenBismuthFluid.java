package name.lapidary.fluid;

import name.lapidary.block.ModBlocks;
import name.lapidary.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

/**
 * The common behavior for both source and flowing molten bismuth.
 *
 * Molten bismuth:
 * - flows slowly like Overworld lava;
 * - loses two fluid levels per horizontal block;
 * - does not create new source blocks;
 * - is represented in the world by ModBlocks.MOLTEN_BISMUTH.
 *
 * Entity damage is handled by MoltenBismuthLiquidBlock rather than here.
 */
public abstract class MoltenBismuthFluid extends FlowingFluid {

    /**
     * The registered flowing-fluid variant.
     */
    @Override
    public Fluid getFlowing() {
        return ModFluids.FLOWING_MOLTEN_BISMUTH;
    }

    /**
     * The registered source-fluid variant.
     */
    @Override
    public Fluid getSource() {
        return ModFluids.MOLTEN_BISMUTH;
    }

    /**
     * Treat the source and flowing variants as the same fluid.
     */
    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == ModFluids.MOLTEN_BISMUTH
                || fluid == ModFluids.FLOWING_MOLTEN_BISMUTH;
    }

    /**
     * Item produced when a source block is collected.
     *
     * This currently returns your throwable bottle item.
     */
    @Override
    public Item getBucket() {
        return ModItems.MOLTEN_BISMUTH_BOTTLE;
    }

    /**
     * Molten bismuth is finite. Two adjacent sources cannot create
     * another source block as water does.
     */
    @Override
    protected boolean canConvertToSource(Level level) {
        return false;
    }

    /**
     * Called when the fluid flows into and destroys a replaceable block.
     *
     * This intentionally does not drop the destroyed block's resources,
     * which is closer to lava behavior than water behavior.
     */
    @Override
    protected void beforeDestroyingBlock(
            LevelAccessor level,
            BlockPos position,
            BlockState state
    ) {
        // Intentionally empty.
    }

    /**
     * Prevent other fluids from directly replacing molten bismuth.
     *
     * We can add explicit water/bismuth interaction behavior later.
     */
    @Override
    protected boolean canBeReplacedWith(
            FluidState state,
            BlockGetter level,
            BlockPos position,
            Fluid incomingFluid,
            Direction direction
    ) {
        return false;
    }

    /**
     * How far the fluid searches horizontally for a downward route.
     *
     * Two blocks matches Overworld lava's short flow search.
     */
    @Override
    protected int getSlopeFindDistance(LevelReader level) {
        return 2;
    }

    /**
     * How much fluid level is lost for each horizontal block traveled.
     *
     * A drop-off of two gives molten bismuth a short lava-like spread.
     */
    @Override
    protected int getDropOff(LevelReader level) {
        return 2;
    }

    /**
     * Number of game ticks between fluid updates.
     *
     * Thirty ticks is the normal slow Overworld-lava timing.
     */
    @Override
    public int getTickDelay(LevelReader level) {
        return 30;
    }

    /**
     * Fluids use high explosion resistance in vanilla.
     */
    @Override
    protected float getExplosionResistance() {
        return 100.0F;
    }

    /**
     * Converts a fluid state into the corresponding LiquidBlock state.
     */
    @Override
    protected BlockState createLegacyBlock(
            FluidState state
    ) {
        return ModBlocks.MOLTEN_BISMUTH
                .defaultBlockState()
                .setValue(
                        LiquidBlock.LEVEL,
                        getLegacyLevel(state)
                );
    }

    /**
     * The non-source, flowing variant.
     */
    public static final class Flowing
            extends MoltenBismuthFluid {

        @Override
        protected void createFluidStateDefinition(
                StateDefinition.Builder<Fluid, FluidState> builder
        ) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public boolean isSource(
                FluidState state
        ) {
            return false;
        }

        @Override
        public int getAmount(
                FluidState state
        ) {
            return state.getValue(LEVEL);
        }
    }

    /**
     * A full source block of molten bismuth.
     */
    public static final class Source
            extends MoltenBismuthFluid {

        @Override
        public boolean isSource(
                FluidState state
        ) {
            return true;
        }

        @Override
        public int getAmount(
                FluidState state
        ) {
            return 8;
        }
    }
}