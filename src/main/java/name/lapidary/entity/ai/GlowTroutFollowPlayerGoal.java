package name.lapidary.entity.ai;

import name.lapidary.entity.GlowTroutEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public final class GlowTroutFollowPlayerGoal
        extends Goal {

    private static final int PATH_RECALCULATION_INTERVAL =
            10;

    private final GlowTroutEntity glowTrout;
    private final double movementSpeed;
    private final double startDistance;
    private final double continueDistance;
    private final double stopDistanceSquared;

    private Player followedPlayer;
    private int pathRecalculationTicks;

    public GlowTroutFollowPlayerGoal(
            GlowTroutEntity glowTrout,
            double movementSpeed,
            double startDistance,
            double stopDistance
    ) {
        this.glowTrout =
                glowTrout;

        this.movementSpeed =
                movementSpeed;

        this.startDistance =
                startDistance;

        /*
         * A slightly larger continuation distance prevents the goal
         * from repeatedly starting and stopping at exactly 12 blocks.
         */
        this.continueDistance =
                startDistance + 4.0D;

        this.stopDistanceSquared =
                stopDistance * stopDistance;

        this.setFlags(
                EnumSet.of(
                        Flag.MOVE,
                        Flag.LOOK
                )
        );
    }

    @Override
    public boolean canUse() {
        if (!glowTrout.isInWaterOrBubble()) {
            return false;
        }

        followedPlayer =
                findNearestSwimmingPlayer(
                        startDistance
                );

        return followedPlayer != null;
    }

    @Override
    public boolean canContinueToUse() {
        return isValidPlayer(
                followedPlayer
        ) && glowTrout.isInWaterOrBubble()
                && glowTrout.distanceToSqr(
                followedPlayer
        ) <= continueDistance
                * continueDistance;
    }

    @Override
    public void start() {
        pathRecalculationTicks =
                0;
    }

    @Override
    public void stop() {
        followedPlayer =
                null;

        pathRecalculationTicks =
                0;

        glowTrout.getNavigation()
                .stop();
    }

    @Override
    public void tick() {
        if (followedPlayer == null) {
            return;
        }

        glowTrout.getLookControl()
                .setLookAt(
                        followedPlayer,
                        10.0F,
                        glowTrout.getMaxHeadXRot()
                );

        double distanceSquared =
                glowTrout.distanceToSqr(
                        followedPlayer
                );

        /*
         * Once close, remain near the player without attempting to
         * occupy exactly the same position.
         */
        if (distanceSquared
                <= stopDistanceSquared) {

            glowTrout.getNavigation()
                    .stop();

            return;
        }

        pathRecalculationTicks--;

        if (pathRecalculationTicks > 0) {
            return;
        }

        pathRecalculationTicks =
                PATH_RECALCULATION_INTERVAL;

        glowTrout.getNavigation()
                .moveTo(
                        followedPlayer,
                        movementSpeed
                );
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private Player findNearestSwimmingPlayer(
            double range
    ) {
        List<Player> candidates =
                glowTrout.level()
                        .getEntitiesOfClass(
                                Player.class,
                                glowTrout
                                        .getBoundingBox()
                                        .inflate(range),
                                this::isValidPlayer
                        );

        return candidates.stream()
                .min(
                        Comparator.comparingDouble(
                                glowTrout::distanceToSqr
                        )
                )
                .orElse(null);
    }

    private boolean isValidPlayer(
            Player player
    ) {
        return player != null
                && player.isAlive()
                && !player.isSpectator()
                && player.isInWaterOrBubble();
    }
}