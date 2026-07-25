package name.lapidary.entity.projectile;

import name.lapidary.entity.ModEntities;
import name.lapidary.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class MageLightEntity
        extends ThrowableItemProjectile {

    /**
     * Approximately ordinary walking speed:
     * 0.20 blocks per tick, or four blocks per second.
     */
    public static final double TRAVEL_SPEED =
            0.20D;

    /**
     * A traveling light that never strikes anything disappears after
     * thirty seconds rather than traveling indefinitely.
     */
    public static final int MAX_FLIGHT_TICKS =
            20 * 30;

    /**
     * Once anchored, the light remains for two minutes.
     */
    public static final int ANCHORED_LIFETIME_TICKS =
            20 * 60 * 2;

    private static final String ANCHORED_KEY =
            "LapidaryAnchored";

    private static final String FLIGHT_TICKS_KEY =
            "LapidaryFlightTicks";

    private static final String EXPIRES_AT_KEY =
            "LapidaryExpiresAt";

    private boolean anchored;
    private int flightTicks;
    private long expiresAt;

    public MageLightEntity(
            EntityType<? extends MageLightEntity> entityType,
            Level level
    ) {
        super(
                entityType,
                level
        );

        setNoGravity(true);
    }

    public MageLightEntity(
            Level level,
            LivingEntity owner
    ) {
        super(
                ModEntities.MAGE_LIGHT,
                owner,
                level
        );

        setNoGravity(true);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.MAGE_LIGHT_ORB;
    }

    /**
     * Mage Lights pass through creatures and collide only with blocks.
     */
    @Override
    protected boolean canHitEntity(
            Entity entity
    ) {
        return false;
    }

    public void setTravelDirection(
            Vec3 direction
    ) {
        if (direction.lengthSqr()
                <= 1.0E-8D) {

            return;
        }

        setDeltaMovement(
                direction.normalize()
                        .scale(TRAVEL_SPEED)
        );
    }

    @Override
    protected void onHitBlock(
            BlockHitResult hitResult
    ) {
        super.onHitBlock(hitResult);

        if (!(level()
                instanceof ServerLevel serverLevel)
                || anchored) {

            return;
        }

        /*
         * Move the orb a short distance out from the impacted surface
         * so that its rendered quad does not disappear inside the block.
         */
        Vec3 surfaceNormal =
                new Vec3(
                        hitResult.getDirection()
                                .getStepX(),
                        hitResult.getDirection()
                                .getStepY(),
                        hitResult.getDirection()
                                .getStepZ()
                );

        Vec3 anchorPosition =
                hitResult.getLocation()
                        .add(
                                surfaceNormal.scale(
                                        0.08D
                                )
                        );

        setPos(
                anchorPosition.x,
                anchorPosition.y,
                anchorPosition.z
        );

        setDeltaMovement(
                Vec3.ZERO
        );

        anchored = true;

        expiresAt =
                serverLevel.getGameTime()
                        + ANCHORED_LIFETIME_TICKS;
    }

    @Override
    public void tick() {
        /*
         * Throwable projectiles normally experience a small amount of
         * drag. Restoring this magnitude each tick keeps Mage Light at
         * its intended constant travel speed.
         */
        if (!anchored) {
            Vec3 movement =
                    getDeltaMovement();

            if (movement.lengthSqr()
                    > 1.0E-8D) {

                setDeltaMovement(
                        movement.normalize()
                                .scale(TRAVEL_SPEED)
                );
            }
        } else {
            setDeltaMovement(
                    Vec3.ZERO
            );
        }

        super.tick();

        if (!(level()
                instanceof ServerLevel serverLevel)) {

            return;
        }

        if (anchored) {
            if (expiresAt <= 0L) {
                expiresAt =
                        serverLevel.getGameTime()
                                + ANCHORED_LIFETIME_TICKS;
            }

            if (serverLevel.getGameTime()
                    >= expiresAt) {

                discard();
            }

            return;
        }

        flightTicks++;

        if (flightTicks
                >= MAX_FLIGHT_TICKS) {

            discard();
        }
    }

    @Override
    public void addAdditionalSaveData(
            CompoundTag tag
    ) {
        super.addAdditionalSaveData(tag);

        tag.putBoolean(
                ANCHORED_KEY,
                anchored
        );

        tag.putInt(
                FLIGHT_TICKS_KEY,
                flightTicks
        );

        tag.putLong(
                EXPIRES_AT_KEY,
                expiresAt
        );
    }

    @Override
    public void readAdditionalSaveData(
            CompoundTag tag
    ) {
        super.readAdditionalSaveData(tag);

        anchored =
                tag.getBoolean(
                        ANCHORED_KEY
                );

        flightTicks =
                Math.max(
                        0,
                        tag.getInt(
                                FLIGHT_TICKS_KEY
                        )
                );

        expiresAt =
                tag.getLong(
                        EXPIRES_AT_KEY
                );

        setNoGravity(true);

        if (anchored) {
            setDeltaMovement(
                    Vec3.ZERO
            );
        }
    }
}