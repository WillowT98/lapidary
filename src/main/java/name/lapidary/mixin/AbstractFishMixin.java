package name.lapidary.mixin;

import name.lapidary.entity.ModEntities;
import name.lapidary.tag.ModFluidTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractFish;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFish.class)
public abstract class AbstractFishMixin {

    /*
     * Check four times per second rather than every tick.
     *
     * This is still fast enough to make the transformation appear
     * immediate while avoiding unnecessary fluid checks.
     */
    @Inject(method = "aiStep", at = @At("TAIL"))
    private void lapidary$transformInMana(CallbackInfo ci) {
        AbstractFish fish = (AbstractFish) (Object) this;

        if (fish.level().isClientSide) {
            return;
        }

        if (fish.tickCount % 5 != 0) {
            return;
        }

        if (!fish.level()
                .getFluidState(fish.blockPosition())
                .is(ModFluidTags.MANA)) {
            return;
        }

        /*
         * Checking the exact entity type prevents the custom fish,
         * which inherit from Cod and Salmon, from transforming again.
         */
        if (fish.getType() == EntityType.COD) {
            lapidary$replaceFish(
                    fish,
                    ModEntities.GLOW_TROUT
            );
        } else if (fish.getType() == EntityType.SALMON) {
            lapidary$replaceFish(
                    fish,
                    ModEntities.BRIGHT_SALMON
            );
        } else if (fish.getType() == EntityType.TROPICAL_FISH) {
            lapidary$replaceFish(
                    fish,
                    ModEntities.AMEFYSH
            );
        }
    }

    @Unique
    private static <T extends AbstractFish> void lapidary$replaceFish(
            AbstractFish original,
            EntityType<T> replacementType
    ) {
        if (!(original.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        T replacement = replacementType.create(serverLevel);

        if (replacement == null) {
            return;
        }

        /*
         * Preserve the original fish's location and motion.
         */
        replacement.moveTo(
                original.getX(),
                original.getY(),
                original.getZ(),
                original.getYRot(),
                original.getXRot()
        );

        replacement.setDeltaMovement(
                original.getDeltaMovement()
        );

        /*
         * Preserve relevant player-facing information.
         */
        replacement.setCustomName(
                original.getCustomName()
        );

        replacement.setCustomNameVisible(
                original.isCustomNameVisible()
        );

        replacement.setHealth(
                Math.min(
                        original.getHealth(),
                        replacement.getMaxHealth()
                )
        );

        /*
         * Fish placed from buckets should remain persistent after
         * transforming.
         */
        replacement.setFromBucket(
                original.fromBucket()
        );

        serverLevel.addFreshEntity(replacement);
        original.discard();
    }
}