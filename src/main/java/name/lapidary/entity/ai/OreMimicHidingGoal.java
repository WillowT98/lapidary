package name.lapidary.entity.ai;

import name.lapidary.entity.OreMimicEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public final class OreMimicHidingGoal extends Goal {

    private final OreMimicEntity mimic;

    /*
     * The exact outward-facing direction is captured when the goal starts.
     * This prevents ordinary mob head tracking from making it look around.
     */
    private float lockedYaw;

    public OreMimicHidingGoal(OreMimicEntity mimic) {
        this.mimic = mimic;

        /*
         * While this goal is active, no other goal may control movement,
         * looking, or jumping.
         */
        this.setFlags(
                EnumSet.of(
                        Flag.MOVE,
                        Flag.LOOK,
                        Flag.JUMP
                )
        );
    }

    @Override
    public boolean canUse() {
        return mimic.isHiding()
                && mimic.getTarget() == null;
    }

    @Override
    public boolean canContinueToUse() {
        return mimic.isHiding()
                && mimic.getTarget() == null;
    }

    @Override
    public void start() {
        lockedYaw = mimic.getYRot();

        mimic.getNavigation().stop();
        mimic.stopInPlace();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        mimic.getNavigation().stop();
        mimic.stopInPlace();

        /*
         * Preserve vertical motion so gravity still behaves normally,
         * but eliminate all horizontal drift.
         */
        Vec3 movement = mimic.getDeltaMovement();

        mimic.setDeltaMovement(
                0.0D,
                movement.y,
                0.0D
        );

        /*
         * Lock body, head, and camera-facing rotations together.
         */
        mimic.setYRot(lockedYaw);
        mimic.setYBodyRot(lockedYaw);
        mimic.setYHeadRot(lockedYaw);
        mimic.setXRot(0.0F);
    }
}