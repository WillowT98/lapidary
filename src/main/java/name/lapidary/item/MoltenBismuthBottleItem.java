package name.lapidary.item;

import name.lapidary.entity.projectile.ThrownMoltenBismuthEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class MoltenBismuthBottleItem extends Item {

    public MoltenBismuthBottleItem(
            Properties properties
    ) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack heldStack =
                player.getItemInHand(hand);

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.SPLASH_POTION_THROW,
                SoundSource.NEUTRAL,
                0.5F,
                0.9F + level.random.nextFloat() * 0.2F
        );

        if (!level.isClientSide) {
            ThrownMoltenBismuthEntity projectile =
                    new ThrownMoltenBismuthEntity(
                            level,
                            player
                    );

            projectile.setItem(
                    heldStack.copyWithCount(1)
            );

            projectile.shootFromRotation(
                    player,
                    player.getXRot(),
                    player.getYRot(),
                    -20.0F,
                    0.7F,
                    1.0F
            );

            level.addFreshEntity(projectile);
        }

        player.awardStat(
                Stats.ITEM_USED.get(this)
        );

        if (!player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(
                heldStack,
                level.isClientSide
        );
    }
}