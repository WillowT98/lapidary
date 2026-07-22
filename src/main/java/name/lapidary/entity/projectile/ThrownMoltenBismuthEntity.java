package name.lapidary.entity.projectile;

import name.lapidary.block.ModBlocks;
import name.lapidary.entity.ModEntities;
import name.lapidary.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class ThrownMoltenBismuthEntity
        extends ThrowableItemProjectile {

    public ThrownMoltenBismuthEntity(
            EntityType<? extends ThrownMoltenBismuthEntity> entityType,
            Level level
    ) {
        super(entityType, level);
    }

    public ThrownMoltenBismuthEntity(
            Level level,
            LivingEntity owner
    ) {
        super(
                ModEntities.THROWN_MOLTEN_BISMUTH,
                owner,
                level
        );
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.MOLTEN_BISMUTH_BOTTLE;
    }

    @Override
    protected void onHit(
            HitResult hitResult
    ) {
        super.onHit(hitResult);

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos placementPosition =
                findPlacementPosition(
                        serverLevel,
                        hitResult
                );

        if (placementPosition != null) {
            serverLevel.setBlockAndUpdate(
                    placementPosition,
                    ModBlocks.BISMUTH_BLOCK
                            .defaultBlockState()
            );
        }

        /*
         * Add glass-breaking sound and particles here.
         */

        this.discard();
    }

    private BlockPos findPlacementPosition(
            ServerLevel level,
            HitResult hitResult
    ) {
        if (hitResult instanceof BlockHitResult blockHit) {
            BlockPos struckPosition =
                    blockHit.getBlockPos();

            BlockState struckState =
                    level.getBlockState(struckPosition);

            if (struckState.canBeReplaced()) {
                return struckPosition;
            }

            BlockPos adjacentPosition =
                    struckPosition.relative(
                            blockHit.getDirection()
                    );

            if (level
                    .getBlockState(adjacentPosition)
                    .canBeReplaced()) {

                return adjacentPosition;
            }

            return null;
        }

        if (hitResult instanceof EntityHitResult) {
            BlockPos currentPosition =
                    this.blockPosition();

            if (level
                    .getBlockState(currentPosition)
                    .canBeReplaced()) {

                return currentPosition;
            }

            BlockPos abovePosition =
                    currentPosition.above();

            if (level
                    .getBlockState(abovePosition)
                    .canBeReplaced()) {

                return abovePosition;
            }
        }

        return null;
    }
}