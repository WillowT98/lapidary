package name.lapidary.entity;

import name.lapidary.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class BrightSalmonEntity extends Salmon {

    private static final long BRUSH_COOLDOWN_TICKS =
            20L * 60L * 10L;

    private static final String NEXT_BRUSH_TIME_TAG =
            "NextBrushTime";

    private long nextBrushTime;

    public BrightSalmonEntity(
            EntityType<? extends BrightSalmonEntity> entityType,
            Level level
    ) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 3.0D);
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(
                ModItems.BRIGHT_SALMON_BUCKET
        );
    }

    @Override
    protected InteractionResult mobInteract(
            Player player,
            InteractionHand hand
    ) {
        ItemStack heldStack =
                player.getItemInHand(hand);

        if (!heldStack.is(Items.BRUSH)) {
            return super.mobInteract(player, hand);
        }

        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        long currentTime =
                this.level().getGameTime();

        if (currentTime < this.nextBrushTime) {
            return InteractionResult.CONSUME;
        }

        this.nextBrushTime =
                currentTime + BRUSH_COOLDOWN_TICKS;

        this.spawnAtLocation(
                Items.GLOWSTONE_DUST
        );

        EquipmentSlot brushSlot =
                hand == InteractionHand.MAIN_HAND
                        ? EquipmentSlot.MAINHAND
                        : EquipmentSlot.OFFHAND;

        heldStack.hurtAndBreak(
                1,
                player,
                brushSlot
        );

        return InteractionResult.CONSUME;
    }

    @Override
    public void addAdditionalSaveData(
            CompoundTag tag
    ) {
        super.addAdditionalSaveData(tag);

        tag.putLong(
                NEXT_BRUSH_TIME_TAG,
                this.nextBrushTime
        );
    }

    @Override
    public void readAdditionalSaveData(
            CompoundTag tag
    ) {
        super.readAdditionalSaveData(tag);

        this.nextBrushTime =
                tag.getLong(NEXT_BRUSH_TIME_TAG);
    }
}