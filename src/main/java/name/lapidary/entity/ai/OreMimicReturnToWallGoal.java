package name.lapidary.entity.ai;

import name.lapidary.entity.OreMimicEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;

public final class OreMimicReturnToWallGoal extends Goal {

    private static final Direction[] HORIZONTAL_DIRECTIONS = {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST
    };

    /*
     * Give up on one return attempt after ten seconds.
     */
    private static final int MAX_RETURN_TICKS = 200;

    /*
     * How many randomly selected locations are considered during
     * one search.
     *
     * The original value of 128 was acceptable when we were only
     * checking blocks. Now that candidates can require a pathfinding
     * calculation, we use a smaller number.
     */
    private static final int SEARCH_ATTEMPTS = 48;

    /*
     * Moves the entity slightly toward the wall after it arrives.
     *
     * The mimic is 0.6 blocks wide, meaning its collision box extends
     * 0.3 blocks from its center. This value presses it close to the
     * wall without placing its collision box inside the wall.
     */
    private static final double WALL_PRESS_OFFSET = 0.19D;

    /*
     * Squared distance at which the mimic is considered close enough
     * to settle into its hiding pose.
     */
    private static final double ARRIVAL_DISTANCE_SQUARED = 0.75D;

    private final OreMimicEntity mimic;
    private final double movementSpeed;
    private final int searchRadius;

    private BlockPos targetFeetPosition;
    private Direction targetWallDirection;

    /*
     * The important new field:
     *
     * We calculate a valid path while selecting the hiding spot,
     * store it, and then follow that exact path once.
     */
    private Path targetPath;

    private int elapsedTicks;
    private long nextSearchGameTime;

    public OreMimicReturnToWallGoal(
            OreMimicEntity mimic,
            double movementSpeed,
            int searchRadius
    ) {
        this.mimic = mimic;
        this.movementSpeed = movementSpeed;
        this.searchRadius = searchRadius;

        this.setFlags(
                EnumSet.of(
                        Flag.MOVE,
                        Flag.LOOK
                )
        );
    }

    @Override
    public boolean canUse() {
        /*
         * It should not seek a wall while fighting or already hidden.
         */
        if (mimic.getTarget() != null || mimic.isHiding()) {
            return false;
        }

        long currentGameTime = mimic.level().getGameTime();

        /*
         * Avoid repeatedly conducting a search when no suitable
         * location is available.
         */
        if (currentGameTime < nextSearchGameTime) {
            return false;
        }

        HideSpot hideSpot = findHidingSpot();

        if (hideSpot == null) {
            /*
             * Wait two seconds before trying another search.
             */
            nextSearchGameTime = currentGameTime + 40L;
            return false;
        }

        targetFeetPosition = hideSpot.feetPosition();
        targetWallDirection = hideSpot.wallDirection();
        targetPath = hideSpot.path();

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (mimic.getTarget() != null) {
            return false;
        }

        if (mimic.isHiding()) {
            return false;
        }

        if (targetFeetPosition == null
                || targetWallDirection == null) {
            return false;
        }

        if (elapsedTicks >= MAX_RETURN_TICKS) {
            return false;
        }

        /*
         * Stop if the blocks at the destination have changed and the
         * location is no longer suitable.
         */
        if (!isValidHidingSpot(
                targetFeetPosition,
                targetWallDirection
        )) {
            return false;
        }

        /*
         * Continue while:
         *
         * 1. The mimic is close enough to settle; or
         * 2. Minecraft is still actively following the path.
         *
         * If navigation ends before the mimic reaches the destination,
         * this returns false and the mimic later searches for another
         * hiding location.
         */
        return isCloseToTarget()
                || !mimic.getNavigation().isDone();
    }

    @Override
    public void start() {
        elapsedTicks = 0;

        /*
         * A null path is allowed when the mimic is already standing in
         * a valid hiding position. In that case, tick() will settle it
         * immediately.
         */
        if (targetPath != null) {
            mimic.getNavigation().moveTo(
                    targetPath,
                    movementSpeed
            );
        }
    }

    @Override
    public void stop() {
        mimic.getNavigation().stop();

        /*
         * If the attempt did not end in successful hiding, briefly
         * delay the next search.
         */
        if (!mimic.isHiding()) {
            nextSearchGameTime =
                    mimic.level().getGameTime() + 20L;
        }

        targetFeetPosition = null;
        targetWallDirection = null;
        targetPath = null;
        elapsedTicks = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        elapsedTicks++;

        /*
         * PathNavigation continues moving the entity independently.
         * This goal does not need to call moveTo() repeatedly.
         */
        if (isCloseToTarget()) {
            settleAgainstWall();
        }
    }

    /**
     * Checks whether the mimic has reached the selected air column.
     */
    private boolean isCloseToTarget() {
        if (targetFeetPosition == null) {
            return false;
        }

        double targetX =
                targetFeetPosition.getX() + 0.5D;

        double targetY =
                targetFeetPosition.getY();

        double targetZ =
                targetFeetPosition.getZ() + 0.5D;

        return mimic.distanceToSqr(
                targetX,
                targetY,
                targetZ
        ) <= ARRIVAL_DISTANCE_SQUARED;
    }

    /**
     * Precisely positions and rotates the mimic after it arrives.
     */
    private void settleAgainstWall() {
        if (targetFeetPosition == null
                || targetWallDirection == null) {
            return;
        }

        if (!isValidHidingSpot(
                targetFeetPosition,
                targetWallDirection
        )) {
            return;
        }

        /*
         * targetWallDirection points from the mimic toward the wall.
         */
        double settledX =
                targetFeetPosition.getX()
                        + 0.5D
                        + targetWallDirection.getStepX()
                        * WALL_PRESS_OFFSET;

        double settledY =
                targetFeetPosition.getY();

        double settledZ =
                targetFeetPosition.getZ()
                        + 0.5D
                        + targetWallDirection.getStepZ()
                        * WALL_PRESS_OFFSET;

        /*
         * Face away from the wall.
         */
        float outwardYaw =
                targetWallDirection
                        .getOpposite()
                        .toYRot();

        mimic.getNavigation().stop();
        mimic.stopInPlace();

        mimic.setPos(
                settledX,
                settledY,
                settledZ
        );

        mimic.setYRot(outwardYaw);
        mimic.setYBodyRot(outwardYaw);
        mimic.setYHeadRot(outwardYaw);
        mimic.setXRot(0.0F);

        mimic.setHiding(true);
    }

    /**
     * Searches nearby locations for a valid and reachable hiding spot.
     */
    private HideSpot findHidingSpot() {
        BlockPos origin = mimic.blockPosition();

        /*
         * First check whether it is already standing beside a valid wall.
         *
         * No path is needed in this case.
         */
        HideSpot currentPositionSpot =
                findWallAt(origin, null);

        if (currentPositionSpot != null) {
            return currentPositionSpot;
        }

        RandomSource random = mimic.getRandom();

        HideSpot closestSpot = null;
        double closestDistanceSquared =
                Double.MAX_VALUE;

        for (int attempt = 0;
             attempt < SEARCH_ATTEMPTS;
             attempt++) {

            int xOffset =
                    random.nextInt(
                            searchRadius * 2 + 1
                    ) - searchRadius;

            /*
             * Keep the vertical search narrower than the horizontal
             * search. Large vertical changes are less likely to produce
             * sensible cave paths.
             */
            int yOffset =
                    random.nextInt(7) - 3;

            int zOffset =
                    random.nextInt(
                            searchRadius * 2 + 1
                    ) - searchRadius;

            BlockPos candidate =
                    origin.offset(
                            xOffset,
                            yOffset,
                            zOffset
                    );

            Direction wallDirection =
                    findWallDirection(candidate);

            if (wallDirection == null) {
                continue;
            }

            double distanceSquared =
                    mimic.distanceToSqr(
                            candidate.getX() + 0.5D,
                            candidate.getY(),
                            candidate.getZ() + 0.5D
                    );

            /*
             * We are looking for the nearest reachable candidate.
             * There is no reason to run pathfinding for a candidate
             * farther away than one we have already accepted.
             */
            if (distanceSquared >= closestDistanceSquared) {
                continue;
            }

            /*
             * Ask Minecraft to calculate a path before selecting this
             * hiding location.
             */
            Path candidatePath =
                    mimic.getNavigation().createPath(
                            candidate,
                            0
                    );

            /*
             * A null path means no path was found.
             *
             * canReach() distinguishes a path that actually arrives at
             * the requested destination from a partial route that merely
             * approaches it.
             */
            if (candidatePath == null
                    || !candidatePath.canReach()) {
                continue;
            }

            closestSpot = new HideSpot(
                    candidate.immutable(),
                    wallDirection,
                    candidatePath
            );

            closestDistanceSquared = distanceSquared;
        }

        return closestSpot;
    }

    /**
     * Creates a hiding spot at a position when one of its sides has a
     * valid wall.
     */
    private HideSpot findWallAt(
            BlockPos feetPosition,
            Path path
    ) {
        Direction wallDirection =
                findWallDirection(feetPosition);

        if (wallDirection == null) {
            return null;
        }

        return new HideSpot(
                feetPosition.immutable(),
                wallDirection,
                path
        );
    }

    /**
     * Returns the first horizontal direction containing a valid wall.
     */
    private Direction findWallDirection(
            BlockPos feetPosition
    ) {
        for (Direction direction
                : HORIZONTAL_DIRECTIONS) {

            if (isValidHidingSpot(
                    feetPosition,
                    direction
            )) {
                return direction;
            }
        }

        return null;
    }

    /**
     * A valid hiding position requires:
     *
     * 1. Empty space for the feet.
     * 2. Empty space for the head.
     * 3. A sturdy floor.
     * 4. A sturdy wall beside both body blocks.
     */
    private boolean isValidHidingSpot(
            BlockPos feetPosition,
            Direction wallDirection
    ) {
        Level level = mimic.level();

        BlockPos headPosition =
                feetPosition.above();

        if (!level.isEmptyBlock(feetPosition)
                || !level.isEmptyBlock(headPosition)) {
            return false;
        }

        BlockPos floorPosition =
                feetPosition.below();

        BlockState floorState =
                level.getBlockState(floorPosition);

        if (!floorState.isFaceSturdy(
                level,
                floorPosition,
                Direction.UP
        )) {
            return false;
        }

        BlockPos lowerWallPosition =
                feetPosition.relative(
                        wallDirection
                );

        BlockPos upperWallPosition =
                headPosition.relative(
                        wallDirection
                );

        Direction wallFaceTowardMimic =
                wallDirection.getOpposite();

        BlockState lowerWallState =
                level.getBlockState(
                        lowerWallPosition
                );

        BlockState upperWallState =
                level.getBlockState(
                        upperWallPosition
                );

        return lowerWallState.isFaceSturdy(
                level,
                lowerWallPosition,
                wallFaceTowardMimic
        ) && upperWallState.isFaceSturdy(
                level,
                upperWallPosition,
                wallFaceTowardMimic
        );
    }

    /**
     * Describes both the hiding location and the path Minecraft found
     * to reach it.
     *
     * The path may be null only when the mimic is already standing at
     * the hiding location.
     */
    private record HideSpot(
            BlockPos feetPosition,
            Direction wallDirection,
            Path path
    ) {
    }
}