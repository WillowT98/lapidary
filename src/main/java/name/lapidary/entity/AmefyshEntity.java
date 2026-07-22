package name.lapidary.entity;

import name.lapidary.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class AmefyshEntity extends TropicalFish {

    /*
     * The amefysh searches this many blocks outward in each direction.
     *
     * Radius 6 means a 13 × 13 × 13 cube centered on the fish.
     */
    private static final int AMETHYST_GROWTH_RADIUS = 6;

    /*
     * One hundred ticks is five seconds.
     *
     * The aura performs one extra vanilla amethyst growth attempt
     * every five seconds.
     */
    private static final int AMETHYST_GROWTH_INTERVAL_TICKS = 100;

    /*
     * Number of nearby budding-amethyst blocks that receive an
     * extra random tick during each aura pulse.
     *
     * Start with one for balance. Increasing this directly makes
     * the amefysh more powerful.
     */
    private static final int EXTRA_GROWTH_ATTEMPTS = 1;

    public AmefyshEntity(
            EntityType<? extends AmefyshEntity> entityType,
            Level level
    ) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 3.0D);
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(ModItems.AMEFYSH_BUCKET);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        /*
         * Block growth must happen only on the logical server.
         */
        if (this.level().isClientSide) {
            return;
        }

        /*
         * Do not scan the nearby area every game tick.
         */
        if (this.tickCount % AMETHYST_GROWTH_INTERVAL_TICKS != 0) {
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        accelerateNearbyAmethyst(serverLevel);
    }

    private void accelerateNearbyAmethyst(
            ServerLevel serverLevel
    ) {
        BlockPos center = this.blockPosition();

        BlockPos minimum = center.offset(
                -AMETHYST_GROWTH_RADIUS,
                -AMETHYST_GROWTH_RADIUS,
                -AMETHYST_GROWTH_RADIUS
        );

        BlockPos maximum = center.offset(
                AMETHYST_GROWTH_RADIUS,
                AMETHYST_GROWTH_RADIUS,
                AMETHYST_GROWTH_RADIUS
        );

        List<BlockPos> nearbyBuddingAmethyst =
                new ArrayList<>();

        /*
         * Only budding amethyst generates and advances amethyst buds.
         * Existing buds and clusters are handled by the budding block's
         * normal random-tick logic.
         */
        for (BlockPos position :
                BlockPos.betweenClosed(minimum, maximum)) {

            if (serverLevel
                    .getBlockState(position)
                    .is(Blocks.BUDDING_AMETHYST)) {

                /*
                 * betweenClosed reuses mutable positions internally,
                 * so store an immutable copy.
                 */
                nearbyBuddingAmethyst.add(
                        position.immutable()
                );
            }
        }

        if (nearbyBuddingAmethyst.isEmpty()) {
            return;
        }

        for (int attempt = 0;
             attempt < EXTRA_GROWTH_ATTEMPTS;
             attempt++) {

            BlockPos growthPosition =
                    nearbyBuddingAmethyst.get(
                            this.getRandom().nextInt(
                                    nearbyBuddingAmethyst.size()
                            )
                    );

            BlockState growthState =
                    serverLevel.getBlockState(growthPosition);

            /*
             * Recheck the block because another operation could have
             * changed it after the scan.
             */
            if (!growthState.is(Blocks.BUDDING_AMETHYST)) {
                continue;
            }

            /*
             * Run the exact vanilla random-tick behavior rather than
             * manually placing buds. This preserves vanilla direction
             * selection, valid-space checks, waterlogging, and growth
             * stages.
             */
            growthState.randomTick(
                    serverLevel,
                    growthPosition,
                    this.getRandom()
            );
        }
    }
}