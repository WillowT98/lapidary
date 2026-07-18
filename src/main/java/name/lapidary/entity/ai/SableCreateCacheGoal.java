package name.lapidary.entity.ai;

import name.lapidary.entity.SableEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;

public final class SableCreateCacheGoal extends Goal {

    /*
     * The sable will spend at most ten seconds attempting to reach its
     * selected location before using a nearby fallback.
     */
    private static final int MAX_TRAVEL_TICKS = 200;

    private final SableEntity sable;
    private final double movementSpeed;
    private final int searchRadius;

    private BlockPos preferredCachePosition;
    private Path path;

    private int travelTicks;
    private boolean finished;
    private long nextAttemptGameTime;

    public SableCreateCacheGoal(
            SableEntity sable,
            double movementSpeed,
            int searchRadius
    ) {
        this.sable = sable;
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
         * Only wild adult sables without caches create new caches.
         */
        if (sable.isTamed()
                || sable.isBaby()
                || sable.hasValidCache()) {
            return false;
        }

        if (!(sable.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        long gameTime = serverLevel.getGameTime();

        if (gameTime < nextAttemptGameTime) {
            return false;
        }

        /*
         * First choose the ideal location. This search does not require
         * the location to be reachable.
         */
        preferredCachePosition =
                sable.findPreferredCacheSite(
                        serverLevel,
                        searchRadius
                );

        if (preferredCachePosition == null) {
            nextAttemptGameTime = gameTime + 40L;
            return false;
        }

        /*
         * First try to find a complete route to a block beside the
         * intended cache.
         */
        path = findPathToAdjacentPosition(
                preferredCachePosition
        );

        /*
         * If no adjacent route reaches the destination, ask Minecraft
         * for a path toward the cache itself.
         *
         * Even when that path cannot reach the target completely,
         * Minecraft may return a partial path that gets the sable as
         * close as possible.
         */
        if (path == null) {
            path = sable.getNavigation()
                    .createPath(
                            preferredCachePosition.above(),
                            1
                    );
        }

        /*
         * When no path exists at all, the goal may still run if the
         * sable can create a cache in a block it can already touch.
         */
        if (path == null
                && sable.findTouchableCacheSite(
                serverLevel,
                preferredCachePosition
        ) == null) {

            nextAttemptGameTime = gameTime + 40L;
            preferredCachePosition = null;
            return false;
        }

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !finished
                && !sable.isTamed()
                && !sable.hasValidCache();
    }

    @Override
    public void start() {
        travelTicks = 0;
        finished = false;

        if (path != null) {
            sable.getNavigation()
                    .moveTo(
                            path,
                            movementSpeed
                    );
        }
    }

    @Override
    public void tick() {
        travelTicks++;

        if (!(sable.level() instanceof ServerLevel serverLevel)) {
            finished = true;
            return;
        }

        /*
         * When the sable comes within touching distance of its preferred
         * site, use that location immediately.
         */
        if (preferredCachePosition != null
                && sable.canTouchCachePosition(
                preferredCachePosition
        )
                && sable.canCreateCacheAt(
                serverLevel,
                preferredCachePosition
        )) {

            sable.createCacheAt(
                    serverLevel,
                    preferredCachePosition
            );

            sable.getNavigation().stop();
            finished = true;
            return;
        }

        /*
         * A path may end because:
         *
         * - the sable reached the general area;
         * - the path was only partial;
         * - something changed in the terrain;
         * - it became stuck.
         *
         * In any of those cases, use a valid block the sable can now
         * physically touch.
         */
        if (path == null
                || sable.getNavigation().isDone()
                || travelTicks >= MAX_TRAVEL_TICKS) {

            BlockPos fallback =
                    sable.findTouchableCacheSite(
                            serverLevel,
                            preferredCachePosition
                    );

            if (fallback != null) {
                sable.createCacheAt(
                        serverLevel,
                        fallback
                );
            }

            sable.getNavigation().stop();
            finished = true;
        }
    }

    @Override
    public void stop() {
        sable.getNavigation().stop();

        if (!sable.hasValidCache()) {
            nextAttemptGameTime =
                    sable.level().getGameTime() + 40L;
        }

        preferredCachePosition = null;
        path = null;
        travelTicks = 0;
        finished = false;
    }

    /**
     * Finds a complete path to one of the four blocks beside the intended
     * cache position.
     */
    private Path findPathToAdjacentPosition(
            BlockPos cachePosition
    ) {
        Path closestPath = null;
        double closestDistanceSquared =
                Double.MAX_VALUE;

        for (Direction direction
                : Direction.Plane.HORIZONTAL) {

            /*
             * The cache replaces a ground block. The sable stands in the
             * air block above neighboring ground.
             */
            BlockPos feetPosition =
                    cachePosition
                            .relative(direction)
                            .above();

            if (!isValidStandingPosition(
                    feetPosition
            )) {
                continue;
            }

            Path candidatePath =
                    sable.getNavigation()
                            .createPath(
                                    feetPosition,
                                    0
                            );

            if (candidatePath == null
                    || !candidatePath.canReach()) {
                continue;
            }

            double distanceSquared =
                    sable.distanceToSqr(
                            feetPosition.getX() + 0.5D,
                            feetPosition.getY(),
                            feetPosition.getZ() + 0.5D
                    );

            if (distanceSquared
                    < closestDistanceSquared) {

                closestPath = candidatePath;
                closestDistanceSquared =
                        distanceSquared;
            }
        }

        return closestPath;
    }

    private boolean isValidStandingPosition(
            BlockPos feetPosition
    ) {
        /*
         * The sable is less than one block tall.
         */
        if (!sable.level().isEmptyBlock(
                feetPosition
        )) {
            return false;
        }

        BlockPos floorPosition =
                feetPosition.below();

        BlockState floorState =
                sable.level()
                        .getBlockState(
                                floorPosition
                        );

        return floorState.isFaceSturdy(
                sable.level(),
                floorPosition,
                Direction.UP
        );
    }
}