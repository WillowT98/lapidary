package name.lapidary.entity.ai;

import name.lapidary.entity.SableEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;

public final class SableReturnToCacheGoal extends Goal {

    /*
     * Close enough to stand beside and access the chest.
     */
    private static final double DEPOSIT_DISTANCE_SQUARED =
            4.0D;

    private final SableEntity sable;
    private final double movementSpeed;

    private BlockPos approachPosition;
    private Path path;

    public SableReturnToCacheGoal(
            SableEntity sable,
            double movementSpeed
    ) {
        this.sable = sable;
        this.movementSpeed = movementSpeed;

        this.setFlags(
                EnumSet.of(
                        Flag.MOVE,
                        Flag.LOOK
                )
        );
    }

    @Override
    public boolean canUse() {
        if (!sable.isCarryingPebble()
                || !sable.hasValidCache()) {
            return false;
        }

        path = findPathToCache();

        return path != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (!sable.isCarryingPebble()
                || !sable.hasValidCache()) {
            return false;
        }

        return isCloseEnoughToDeposit()
                || !sable.getNavigation()
                .isDone();
    }

    @Override
    public void start() {
        if (path != null) {
            sable.getNavigation()
                    .moveTo(
                            path,
                            movementSpeed
                    );
        }
    }

    @Override
    public void stop() {
        sable.getNavigation().stop();

        path = null;
        approachPosition = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        BlockPos cachePos =
                sable.getCachePos();

        if (cachePos == null) {
            return;
        }

        sable.getLookControl()
                .setLookAt(
                        cachePos.getX() + 0.5D,
                        cachePos.getY() + 0.5D,
                        cachePos.getZ() + 0.5D
                );

        if (isCloseEnoughToDeposit()) {
            sable.getNavigation().stop();
            sable.depositCarriedPebble();
        }
    }

    private Path findPathToCache() {
        BlockPos cachePos =
                sable.getCachePos();

        if (cachePos == null) {
            return null;
        }

        Path closestPath = null;
        double closestDistanceSquared =
                Double.MAX_VALUE;

        /*
         * The chest replaces a ground block. The sable therefore stands
         * one block above an adjacent ground block.
         */
        for (Direction direction
                : Direction.Plane.HORIZONTAL) {

            BlockPos candidate =
                    cachePos.relative(direction)
                            .above();

            if (!isValidApproachPosition(
                    candidate
            )) {
                continue;
            }

            Path candidatePath =
                    sable.getNavigation()
                            .createPath(
                                    candidate,
                                    0
                            );

            if (candidatePath == null
                    || !candidatePath.canReach()) {
                continue;
            }

            double distanceSquared =
                    sable.distanceToSqr(
                            candidate.getX() + 0.5D,
                            candidate.getY(),
                            candidate.getZ() + 0.5D
                    );

            if (distanceSquared
                    < closestDistanceSquared) {
                closestPath = candidatePath;
                approachPosition =
                        candidate.immutable();

                closestDistanceSquared =
                        distanceSquared;
            }
        }

        return closestPath;
    }

    private boolean isValidApproachPosition(
            BlockPos feetPosition
    ) {
        /*
         * The sable is only 0.7 blocks tall, so it needs one empty block,
         * not the two-block clearance required by a humanoid.
         */
        if (!sable.level().isEmptyBlock(feetPosition)) {
            return false;
        }

        BlockPos floorPosition =
                feetPosition.below();

        BlockState floorState =
                sable.level().getBlockState(floorPosition);

        return floorState.isFaceSturdy(
                sable.level(),
                floorPosition,
                Direction.UP
        );
    }

    private boolean isCloseEnoughToDeposit() {
        BlockPos cachePos =
                sable.getCachePos();

        if (cachePos == null) {
            return false;
        }

        return sable.distanceToSqr(
                cachePos.getX() + 0.5D,
                cachePos.getY() + 0.5D,
                cachePos.getZ() + 0.5D
        ) <= DEPOSIT_DISTANCE_SQUARED;
    }
}