package name.lapidary.item;

import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class GoldPanItem extends Item {
    /*
     * Number of game ticks the player must hold right-click.
     * Increase this to make panning slower.
     */
    private static final int PANNING_DURATION = 80;

    private static final int MIN_FLAKES = 5;
    private static final int MAX_FLAKES = 15;

    public GoldPanItem(Properties properties) {
        super(properties);
    }

    /**
     * Called when the player right-clicks while holding the pan.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack pan = player.getItemInHand(hand);

        if (!canPan(level, player)) {
            return InteractionResultHolder.fail(pan);
        }

        /*
         * Begin Minecraft's normal held-item use process.
         */
        player.startUsingItem(hand);

        return InteractionResultHolder.consume(pan);
    }

    /**
     * Determines how long the player must continue using the item.
     */
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return PANNING_DURATION;
    }

    /**
     * Selects the player's visible item-use pose.
     *
     * BRUSH is the closest existing vanilla animation for panning.
     */
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BRUSH;
    }

    /**
     * Called when the full use duration has elapsed.
     */
    @Override
    public ItemStack finishUsingItem(
            ItemStack pan,
            Level level,
            LivingEntity user
    ) {
        if (!(user instanceof Player player)) {
            return pan;
        }

        /*
         * Check again at completion so the player cannot begin in a river,
         * walk away, and still receive the flakes.
         */
        if (!canPan(level, player)) {
            return pan;
        }

        /*
         * Item rewards and durability must be handled by the logical server.
         */
        if (!level.isClientSide) {
            int flakeCount =
                    MIN_FLAKES
                            + player.getRandom().nextInt(
                            MAX_FLAKES - MIN_FLAKES + 1
                    );

            ItemStack flakes =
                    new ItemStack(ModItems.GOLD_FLAKES, flakeCount);

            /*
             * Spawn one item entity containing the full stack of flakes.
             */
            player.spawnAtLocation(flakes);

            /*
             * Do not damage tools used by players in Creative mode.
             */
            if (!player.getAbilities().instabuild) {
                EquipmentSlot slot =
                        player.getUsedItemHand() == InteractionHand.MAIN_HAND
                                ? EquipmentSlot.MAINHAND
                                : EquipmentSlot.OFFHAND;

                pan.hurtAndBreak(1, player, slot);
            }
        }

        return pan;
    }

    /**
     * Checks all current requirements for panning.
     */
    private static boolean canPan(Level level, Player player) {
        /*
         * The player's body must currently be touching water.
         */
        if (!player.isInWater()) {
            return false;
        }

        /*
         * This accepts both ordinary rivers and other biomes included in
         * Minecraft's river biome tag.
         */
        if (!level.getBiome(player.blockPosition())
                .is(BiomeTags.IS_RIVER)) {
            return false;
        }

        /*
         * Perform a raycast that recognizes water as a target.
         */
        BlockHitResult hitResult = getPlayerPOVHitResult(
                level,
                player,
                ClipContext.Fluid.WATER
        );

        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        /*
         * Confirm that the block position hit by the ray actually contains
         * water, rather than merely being a solid block beyond the water.
         */
        return level.getFluidState(hitResult.getBlockPos())
                .is(FluidTags.WATER);
    }
}