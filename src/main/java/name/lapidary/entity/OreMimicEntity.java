package name.lapidary.entity;

import name.lapidary.entity.ai.OreMimicHidingGoal;
import name.lapidary.entity.ai.OreMimicReturnToWallGoal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.minecraft.world.phys.AABB;

public final class OreMimicEntity extends Monster {
    private static final double EXTRA_ATTACK_REACH = 1.25D;

    @Override
    protected AABB getAttackBoundingBox() {
        return super.getAttackBoundingBox().inflate(
                EXTRA_ATTACK_REACH,
                0.5D,
                EXTRA_ATTACK_REACH
        );
    }

    private static final String HIDING_NBT_KEY =
            "LapidaryHiding";

    /*
     * SynchedEntityData sends the hiding state from the logical server
     * to clients that are tracking this entity.
     *
     * The renderer will use this value to determine whether to play
     * normal humanoid animation or hold the perfectly still pose.
     */
    private static final EntityDataAccessor<Boolean> DATA_HIDING =
            SynchedEntityData.defineId(
                    OreMimicEntity.class,
                    EntityDataSerializers.BOOLEAN
            );

    public OreMimicEntity(
            EntityType<? extends OreMimicEntity> entityType,
            Level level
    ) {
        super(entityType, level);
    }

    /**
     * Defines the ore mimic's initial combat and movement statistics.
     *
     * These values are deliberately easy to adjust later.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.20D)
                .add(Attributes.ATTACK_KNOCKBACK, 3.5D);
    }

    /**
     * Registers the entity's AI goals.
     *
     * Lower priority numbers take precedence over higher numbers when
     * two goals require the same controls.
     */
    @Override
    protected void registerGoals() {
        /*
         * Priority 0:
         * Do not let it simply drown when it falls into water.
         */
        this.goalSelector.addGoal(
                0,
                new FloatGoal(this)
        );

        /*
         * Priority 1:
         * While hiding, lock all movement and looking.
         */
        this.goalSelector.addGoal(
                1,
                new OreMimicHidingGoal(this)
        );

        /*
         * Priority 2:
         * Pursue and attack its current target.
         */
        this.goalSelector.addGoal(
                2,
                new MeleeAttackGoal(
                        this,
                        1.1D,
                        true
                )
        );

        /*
         * Priority 3:
         * When it has no target and is not already hiding,
         * search for a wall and return to concealment.
         */
        this.goalSelector.addGoal(
                3,
                new OreMimicReturnToWallGoal(
                        this,
                        1.0D,
                        10
                )
        );

        /*
         * Retaliate when attacked.
         */
        this.targetSelector.addGoal(
                1,
                new HurtByTargetGoal(this)
        );

        /*
         * Detect nearby players who are valid attack targets.
         */
        this.targetSelector.addGoal(
                2,
                new NearestAttackableTargetGoal<>(
                        this,
                        Player.class,
                        true
                )
        );
    }

    /**
     * Defines the initial synchronized values for a newly created entity.
     */
    @Override
    protected void defineSynchedData(
            SynchedEntityData.Builder builder
    ) {
        super.defineSynchedData(builder);

        builder.define(
                DATA_HIDING,
                false
        );
    }

    public boolean isHiding() {
        return this.entityData.get(DATA_HIDING);
    }

    public void setHiding(boolean hiding) {
        this.entityData.set(
                DATA_HIDING,
                hiding
        );

        if (hiding) {
            this.getNavigation().stop();
            this.stopInPlace();
        }
    }

    /**
     * Minecraft's target goals call this method when they acquire or
     * discard a target.
     *
     * Acquiring any target immediately wakes the mimic.
     */
    @Override
    public void setTarget(LivingEntity target) {
        super.setTarget(target);

        if (target != null) {
            setHiding(false);
        }
    }

    /**
     * Other entities cannot casually shove a hidden mimic away from
     * its chosen wall.
     */
    @Override
    public boolean isPushable() {
        return !isHiding() && super.isPushable();
    }

    /**
     * Provides an additional safety lock on hidden movement.
     *
     * The hiding goal already stops navigation, but this prevents small
     * residual horizontal velocity from making it drift.
     */
    @Override
    public void aiStep() {
        super.aiStep();

        if (isHiding()) {
            this.getNavigation().stop();

            Vec3 movement = this.getDeltaMovement();

            this.setDeltaMovement(
                    0.0D,
                    movement.y,
                    0.0D
            );

            this.setYHeadRot(this.getYRot());
            this.setYBodyRot(this.getYRot());
        }
    }

    /**
     * Saves the hiding state with the entity.
     */
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putBoolean(
                HIDING_NBT_KEY,
                isHiding()
        );
    }

    /**
     * Restores the hiding state when the entity is loaded.
     */
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        setHiding(
                tag.getBoolean(HIDING_NBT_KEY)
        );
    }
}