package name.lapidary.magic.spell;

import name.lapidary.entity.projectile.MageLightEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Creates the traveling Mage Light entity.
 */
public final class MageLightSpell {

    private static final double SPAWN_OFFSET =
            0.55D;

    private MageLightSpell() {
    }

    public static void cast(
            SpellCastContext context
    ) {
        ServerPlayer caster =
                context.caster();

        if (!(caster.level()
                instanceof ServerLevel serverLevel)) {

            return;
        }

        Vec3 direction =
                caster.getLookAngle()
                        .normalize();

        Vec3 spawnPosition =
                caster.getEyePosition()
                        .add(
                                direction.scale(
                                        SPAWN_OFFSET
                                )
                        );

        MageLightEntity mageLight =
                new MageLightEntity(
                        serverLevel,
                        caster
                );

        mageLight.setPos(
                spawnPosition.x,
                spawnPosition.y,
                spawnPosition.z
        );

        mageLight.setTravelDirection(
                direction
        );

        mageLight.setYRot(
                caster.getYRot()
        );

        mageLight.setXRot(
                caster.getXRot()
        );

        serverLevel.addFreshEntity(
                mageLight
        );
    }
}