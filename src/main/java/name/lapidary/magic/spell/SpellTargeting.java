package name.lapidary.magic.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

public final class SpellTargeting {
    private SpellTargeting() {
    }

    public static Optional<BlockHitResult> block(
            ServerPlayer caster,
            double range,
            boolean includeFluids
    ) {
        HitResult hit = caster.pick(range, 0.0F, includeFluids);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return Optional.empty();
        }
        return Optional.of((BlockHitResult) hit);
    }

    public static Optional<BlockPos> placementPosition(
            ServerPlayer caster,
            double range
    ) {
        return block(caster, range, false)
                .map(hit -> hit.getBlockPos().relative(hit.getDirection()));
    }

    public static <T extends Entity> Optional<T> entity(
            ServerPlayer caster,
            double range,
            Class<T> type,
            Predicate<T> predicate
    ) {
        Vec3 start = caster.getEyePosition();
        Vec3 direction = caster.getLookAngle().normalize();
        Vec3 end = start.add(direction.scale(range));
        AABB search = caster.getBoundingBox()
                .expandTowards(direction.scale(range))
                .inflate(1.0D);

        return caster.serverLevel()
                .getEntitiesOfClass(type, search, entity ->
                        entity != caster
                                && entity.isAlive()
                                && predicate.test(entity)
                                && caster.hasLineOfSight(entity)
                                && entity.getBoundingBox()
                                .inflate(entity.getPickRadius())
                                .clip(start, end)
                                .isPresent())
                .stream()
                .min(Comparator.comparingDouble(caster::distanceToSqr));
    }
}
