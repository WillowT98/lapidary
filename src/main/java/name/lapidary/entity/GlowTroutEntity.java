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
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class GlowTroutEntity extends Cod {

    /*
     * Ten minutes:
     * 20 ticks per second × 60 seconds × 10 minutes.
     *
     * Replace this value with the Sable's cooldown if you want
     * both systems to refresh at exactly the same rate.
     */
    private static final long BRUSH_COOLDOWN_TICKS =
            20L * 60L * 10L;

    private static final String NEXT_BRUSH_TIME_TAG =
            "NextBrushTime";

    private long nextBrushTime;

    public GlowTroutEntity(
            EntityType<? extends GlowTroutEntity> entityType,
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
                ModItems.GLOW_TROUT_BUCKET
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

        /*
         * Return success on the client so the hand swings, but perform
         * inventory, cooldown, and item-drop changes only on the server.
         */
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        long currentTime =
                this.level().getGameTime();

        /*
         * Consume the interaction without producing another item while
         * the fish is still on cooldown.
         */
        if (currentTime < this.nextBrushTime) {
            return InteractionResult.CONSUME;
        }

        this.nextBrushTime =
                currentTime + BRUSH_COOLDOWN_TICKS;

        this.spawnAtLocation(
                Items.GLOW_LICHEN
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