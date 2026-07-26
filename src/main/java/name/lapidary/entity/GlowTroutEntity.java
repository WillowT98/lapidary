package name.lapidary.entity;

import name.lapidary.entity.ai.GlowTroutFollowPlayerGoal;
import name.lapidary.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class GlowTroutEntity
        extends Cod {

    /*
     * Ten minutes:
     * 20 ticks per second × 60 seconds × 10 minutes.
     */
    private static final long BRUSH_COOLDOWN_TICKS =
            20L * 60L * 10L;

    private static final String NEXT_BRUSH_TIME_TAG =
            "NextBrushTime";

    /*
     * Following behavior.
     */
    private static final double FOLLOW_SPEED =
            1.1D;

    private static final double FOLLOW_START_DISTANCE =
            12.0D;

    private static final double FOLLOW_STOP_DISTANCE =
            2.5D;

    /*
     * Night Vision aura.
     */
    private static final double NIGHT_VISION_RADIUS =
            5.0D;

    private static final double NIGHT_VISION_RADIUS_SQUARED =
            NIGHT_VISION_RADIUS
                    * NIGHT_VISION_RADIUS;

    /*
     * Search for nearby players once per second rather than every
     * entity tick.
     */
    private static final int AURA_CHECK_INTERVAL =
            20;

    /*
     * Thirteen seconds. The effect is refreshed before falling into
     * Night Vision's flashing/fading period.
     */
    private static final int NIGHT_VISION_DURATION =
            20 * 13;

    private static final int NIGHT_VISION_REFRESH_THRESHOLD =
            20 * 11;

    private long nextBrushTime;

    public GlowTroutEntity(
            EntityType<? extends GlowTroutEntity> entityType,
            Level level
    ) {
        super(
                entityType,
                level
        );
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(
                        Attributes.MAX_HEALTH,
                        3.0D
                );
    }

    @Override
    protected void registerGoals() {
        /*
         * Keep the ordinary cod goals, including swimming and
         * schooling, then add player-following at a higher priority
         * than ordinary wandering.
         */
        super.registerGoals();

        this.goalSelector.addGoal(
                1,
                new GlowTroutFollowPlayerGoal(
                        this,
                        FOLLOW_SPEED,
                        FOLLOW_START_DISTANCE,
                        FOLLOW_STOP_DISTANCE
                )
        );
    }

    @Override
    public void tick() {
        super.tick();

        if (!(this.level()
                instanceof ServerLevel serverLevel)) {

            return;
        }

        /*
         * A glow trout flopping on land should neither follow players
         * nor provide Night Vision.
         */
        if (!this.isInWaterOrBubble()) {
            return;
        }

        /*
         * Include the entity ID so groups of trout distribute their
         * searches across different ticks instead of all scanning on
         * the same server tick.
         */
        if ((this.tickCount + this.getId())
                % AURA_CHECK_INTERVAL != 0) {

            return;
        }

        applyNightVisionAura(
                serverLevel
        );
    }

    private void applyNightVisionAura(
            ServerLevel serverLevel
    ) {
        for (Player player :
                serverLevel.getEntitiesOfClass(
                        Player.class,
                        this.getBoundingBox()
                                .inflate(
                                        NIGHT_VISION_RADIUS
                                ),
                        this::canReceiveNightVision
                )) {

            MobEffectInstance existingEffect =
                    player.getEffect(
                            MobEffects.NIGHT_VISION
                    );

            /*
             * Preserve a longer potion or command-granted instance
             * rather than continually replacing it.
             */
            if (existingEffect != null
                    && existingEffect.getDuration()
                    > NIGHT_VISION_REFRESH_THRESHOLD) {

                continue;
            }

            player.addEffect(
                    new MobEffectInstance(
                            MobEffects.NIGHT_VISION,
                            NIGHT_VISION_DURATION,
                            0,
                            true,
                            false,
                            true
                    ),
                    this
            );
        }
    }

    private boolean canReceiveNightVision(
            Player player
    ) {
        return player.isAlive()
                && !player.isSpectator()
                && player.isInWaterOrBubble()
                && this.distanceToSqr(player)
                <= NIGHT_VISION_RADIUS_SQUARED;
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(
                ModItems.GLOW_TROUT_BUCKET
        );
    }

    @Override
    protected InteractionResult mobInteract(
            Player player,
            InteractionHand hand
    ) {
        ItemStack heldStack =
                player.getItemInHand(hand);

        if (!heldStack.is(Items.BRUSH)) {
            return super.mobInteract(
                    player,
                    hand
            );
        }

        /*
         * Return success on the client so the hand swings, but perform
         * inventory, cooldown, and item-drop changes only on the server.
         */
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        long currentTime =
                this.level().getGameTime();

        /*
         * Consume the interaction without producing another item while
         * the fish is still on cooldown.
         */
        if (currentTime < this.nextBrushTime) {
            return InteractionResult.CONSUME;
        }

        this.nextBrushTime =
                currentTime
                        + BRUSH_COOLDOWN_TICKS;

        this.spawnAtLocation(
                Items.GLOW_LICHEN
        );

        EquipmentSlot brushSlot =
                hand == InteractionHand.MAIN_HAND
                        ? EquipmentSlot.MAINHAND
                        : EquipmentSlot.OFFHAND;

        heldStack.hurtAndBreak(
                1,
                player,
                brushSlot
        );

        return InteractionResult.CONSUME;
    }

    @Override
    public void addAdditionalSaveData(
            CompoundTag tag
    ) {
        super.addAdditionalSaveData(tag);

        tag.putLong(
                NEXT_BRUSH_TIME_TAG,
                this.nextBrushTime
        );
    }

    @Override
    public void readAdditionalSaveData(
            CompoundTag tag
    ) {
        super.readAdditionalSaveData(tag);

        this.nextBrushTime =
                tag.getLong(
                        NEXT_BRUSH_TIME_TAG
                );
    }
}