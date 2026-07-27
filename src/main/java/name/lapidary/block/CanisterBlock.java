package name.lapidary.block;

import com.mojang.serialization.MapCodec;
import name.lapidary.block.entity.CanisterBlockEntity;
import name.lapidary.fluid.CanisterFluidStorage;
import name.lapidary.fluid.CanisterLiquid;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Locale;

public final class CanisterBlock
        extends BaseEntityBlock {

    public static final MapCodec<CanisterBlock> CODEC =
            simpleCodec(
                    CanisterBlock::new
            );

    /*
     * Includes the frame and the protruding capacity marks.
     */
    private static final VoxelShape SHAPE =
            box(
                    3.5D,
                    1.0D,
                    4.0D,
                    12.0D,
                    16.0D,
                    12.0D
            );

    public CanisterBlock(
            BlockBehaviour.Properties properties
    ) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock>
    codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        return new CanisterBlockEntity(
                position,
                state
        );
    }

    @Override
    protected RenderShape getRenderShape(
            BlockState state
    ) {
        /*
         * Keep rendering the existing JSON model. The block-entity
         * renderer only adds the internal liquid.
         */
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack heldStack,
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        CanisterLiquid bucketLiquid =
                CanisterLiquid
                        .fromFilledBucket(
                                heldStack
                        );

        boolean holdingEmptyBucket =
                heldStack.is(
                        Items.BUCKET
                );

        /*
         * Let unrelated items perform their ordinary interactions.
         */
        if (bucketLiquid == null
                && !holdingEmptyBucket) {

            return ItemInteractionResult
                    .PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        /*
         * The client acknowledges the interaction immediately.
         * Actual storage changes occur only on the server.
         */
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        if (!(level.getBlockEntity(position)
                instanceof CanisterBlockEntity
                canister)) {

            return ItemInteractionResult.FAIL;
        }

        if (bucketLiquid != null) {
            return insertBucket(
                    level,
                    position,
                    player,
                    hand,
                    heldStack,
                    canister,
                    bucketLiquid
            );
        }

        return removeBucket(
                level,
                position,
                player,
                hand,
                heldStack,
                canister
        );
    }

    private static ItemInteractionResult insertBucket(
            Level level,
            BlockPos position,
            Player player,
            InteractionHand hand,
            ItemStack heldStack,
            CanisterBlockEntity canister,
            CanisterLiquid insertedLiquid
    ) {
        CanisterFluidStorage storage =
                canister.getStorage();

        /*
         * This may be a full bucket or only the remaining fraction of
         * capacity when the canister is nearly full.
         */
        long insertable =
                storage.insert(
                        insertedLiquid,
                        CanisterFluidStorage.BUCKET,
                        true
                );

        /*
         * Reject only when none of the bucket can be accepted.
         */
        if (insertable <= 0L) {
            if (!storage.isEmpty()
                    && !storage.contains(insertedLiquid)) {

                player.displayClientMessage(
                        Component.translatable(
                                "message.lapidary.canister"
                                        + ".different_liquid"
                        ),
                        true
                );
            } else {
                player.displayClientMessage(
                        Component.translatable(
                                "message.lapidary.canister.full"
                        ),
                        true
                );
            }

            return ItemInteractionResult.FAIL;
        }

        /*
         * Insert as much of the bucket as will fit. Any excess is lost,
         * but the filled bucket is still consumed as requested.
         */
        storage.insert(
                insertedLiquid,
                insertable,
                false
        );

        /*
         * Creative players retain the filled bucket.
         */
        if (!player.getAbilities().instabuild) {
            player.setItemInHand(
                    hand,
                    new ItemStack(Items.BUCKET)
            );
        }

        SoundEvent sound =
                insertedLiquid.usesLavaSounds()
                        ? SoundEvents.BUCKET_EMPTY_LAVA
                        : SoundEvents.BUCKET_EMPTY;

        level.playSound(
                null,
                position,
                sound,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );

        level.gameEvent(
                player,
                GameEvent.FLUID_PLACE,
                position
        );

        return ItemInteractionResult.SUCCESS;
    }

    private static ItemInteractionResult removeBucket(
            Level level,
            BlockPos position,
            Player player,
            InteractionHand hand,
            ItemStack heldStack,
            CanisterBlockEntity canister
    ) {
        CanisterFluidStorage storage =
                canister.getStorage();

        CanisterFluidStorage.Transfer preview =
                storage.extractAny(
                        CanisterFluidStorage.BUCKET,
                        true
                );

        /*
         * Partial amounts inserted by future machines cannot yet be
         * removed using a bucket until at least one full bucket exists.
         */
        if (preview.amount()
                < CanisterFluidStorage.BUCKET) {

            player.displayClientMessage(
                    Component.translatable(
                            storage.isEmpty()
                                    ? "message.lapidary"
                                    + ".canister.empty"
                                    : "message.lapidary"
                                    + ".canister"
                                    + ".not_enough"
                    ),
                    true
            );

            return ItemInteractionResult.FAIL;
        }

        CanisterFluidStorage.Transfer extracted =
                storage.extractAny(
                        CanisterFluidStorage.BUCKET,
                        false
                );

        ItemStack filledBucket =
                new ItemStack(
                        extracted.liquid()
                                .filledBucketItem()
                );

        /*
         * Handles bucket stacks, full inventories, and Creative mode
         * using vanilla's ordinary filled-container behavior.
         */
        ItemStack replacement =
                ItemUtils.createFilledResult(
                        heldStack,
                        player,
                        filledBucket
                );

        player.setItemInHand(
                hand,
                replacement
        );

        SoundEvent sound =
                extracted.liquid()
                        .usesLavaSounds()
                        ? SoundEvents.BUCKET_FILL_LAVA
                        : SoundEvents.BUCKET_FILL;

        level.playSound(
                null,
                position,
                sound,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );

        level.gameEvent(
                player,
                GameEvent.FLUID_PICKUP,
                position
        );

        return ItemInteractionResult.SUCCESS;
    }

    /**
     * Empty-hand interaction reports the contents.
     */
    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            BlockHitResult hitResult
    ) {
        if (!level.isClientSide
                && level.getBlockEntity(position)
                instanceof CanisterBlockEntity
                canister) {

            CanisterFluidStorage storage =
                    canister.getStorage();

            if (storage.isEmpty()) {
                player.displayClientMessage(
                        Component.translatable(
                                "message.lapidary.canister.empty"
                        ),
                        true
                );
            } else {
                player.displayClientMessage(
                        contentsComponent(
                                storage.getLiquid(),
                                storage.getAmount()
                        ),
                        true
                );
            }
        }

        return InteractionResult.sidedSuccess(
                level.isClientSide
        );
    }
    @Override
    protected void tick(
            BlockState state,
            ServerLevel level,
            BlockPos position,
            RandomSource random
    ) {
        transferDownward(
                level,
                position
        );
    }
    /**
     * Attempts to move as much liquid as possible from this canister
     * into the canister immediately beneath it.
     */
    private static void transferDownward(
            ServerLevel level,
            BlockPos upperPosition
    ) {
        if (!(level.getBlockEntity(
                upperPosition
        ) instanceof CanisterBlockEntity
                upperCanister)) {

            return;
        }

        BlockPos lowerPosition =
                upperPosition.below();

        if (!(level.getBlockEntity(
                lowerPosition
        ) instanceof CanisterBlockEntity
                lowerCanister)) {

            return;
        }

        CanisterFluidStorage upperStorage =
                upperCanister.getStorage();

        CanisterFluidStorage lowerStorage =
                lowerCanister.getStorage();

        if (upperStorage.isEmpty()
                || lowerStorage.isFull()) {

            return;
        }

        CanisterLiquid upperLiquid =
                upperStorage.getLiquid();

        /*
         * An empty lower canister can accept the upper liquid.
         * A nonempty lower canister must contain the same liquid.
         */
        if (!lowerStorage.isEmpty()
                && !lowerStorage.contains(
                upperLiquid
        )) {

            return;
        }

        upperStorage.transferTo(
                lowerStorage,
                upperStorage.getAmount()
        );
    }

    @Override
    protected void onPlace(
            BlockState state,
            Level level,
            BlockPos position,
            BlockState oldState,
            boolean movedByPiston
    ) {
        super.onPlace(
                state,
                level,
                position,
                oldState,
                movedByPiston
        );

        if (level.isClientSide) {
            return;
        }

        /*
         * If this canister was placed above another, try draining this
         * canister downward.
         */
        level.scheduleTick(
                position,
                this,
                1
        );

        /*
         * If this canister was placed beneath an existing canister,
         * schedule that upper canister too.
         */
        BlockPos abovePosition =
                position.above();

        if (level.getBlockState(
                abovePosition
        ).is(this)) {

            level.scheduleTick(
                    abovePosition,
                    this,
                    1
            );
        }
    }



    /**
     * Always creates the ordinary canister item, adding block-entity
     * data when contents are present.
     */
    @Override
    protected List<ItemStack> getDrops(
            BlockState state,
            LootParams.Builder builder
    ) {
        ItemStack droppedCanister =
                new ItemStack(this);

        BlockEntity blockEntity =
                builder.getOptionalParameter(
                        LootContextParams.BLOCK_ENTITY
                );

        if (blockEntity
                instanceof CanisterBlockEntity
                canister
                && !canister.getStorage()
                .isEmpty()) {

            /*
             * Stores the block entity's custom NBT in the item's
             * BLOCK_ENTITY_DATA component. BlockItem restores that
             * data when the canister is placed again.
             */
            canister.saveToItem(
                    droppedCanister,
                    builder.getLevel()
                            .registryAccess()
            );
        }

        return List.of(
                droppedCanister
        );
    }

    /**
     * Displays preserved contents while the canister is an item.
     */
    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(
                stack,
                context,
                tooltip,
                flag
        );

        CustomData blockEntityData =
                stack.get(
                        DataComponents.BLOCK_ENTITY_DATA
                );

        if (blockEntityData == null
                || blockEntityData.isEmpty()) {

            return;
        }

        CanisterLiquid liquid =
                CanisterLiquid.byId(
                        blockEntityData
                                .copyTag()
                                .getString(
                                        CanisterBlockEntity
                                                .LIQUID_KEY
                                )
                );

        long amount =
                blockEntityData
                        .copyTag()
                        .getLong(
                                CanisterBlockEntity
                                        .AMOUNT_KEY
                        );

        if (liquid == null
                || amount <= 0L) {

            return;
        }

        tooltip.add(
                contentsComponent(
                        liquid,
                        amount
                ).copy()
                        .withStyle(
                                ChatFormatting.GRAY
                        )
        );
    }

    private static Component contentsComponent(
            CanisterLiquid liquid,
            long amount
    ) {
        return Component.translatable(
                "message.lapidary.canister.contents",
                liquid.displayName(),
                formatBucketAmount(amount)
        );
    }

    private static String formatBucketAmount(
            long amount
    ) {
        double buckets =
                (double) amount
                        / CanisterFluidStorage.BUCKET;

        if (amount
                % CanisterFluidStorage.BUCKET
                == 0L) {

            return Long.toString(
                    amount
                            / CanisterFluidStorage.BUCKET
            );
        }

        return String.format(
                Locale.ROOT,
                "%.2f",
                buckets
        );
    }
}