package name.lapidary.block;

import com.mojang.serialization.MapCodec;
import name.lapidary.block.entity.ManaPercolatorBlockEntity;
import name.lapidary.block.entity.ModBlockEntities;
import name.lapidary.item.ModItems;
import name.lapidary.tag.ModItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Direct-interaction mana processor with no menu.
 *
 * Temporary targeting while no final model exists:
 * - top face: output canister mount;
 * - the block's visual-left face: water canister mount;
 * - every other face: chamber and gem interaction.
 */
public final class ManaPercolatorBlock
        extends BaseEntityBlock {

    public static final MapCodec<ManaPercolatorBlock> CODEC =
            simpleCodec(
                    ManaPercolatorBlock::new
            );

    public static final DirectionProperty FACING =
            BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE =
            box(
                    0.0D,
                    0.0D,
                    0.0D,
                    16.0D,
                    16.0D,
                    16.0D
            );

    public ManaPercolatorBlock(
            BlockBehaviour.Properties properties
    ) {
        super(properties);

        registerDefaultState(
                stateDefinition.any()
                        .setValue(
                                FACING,
                                Direction.SOUTH
                        )
        );
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
        return new ManaPercolatorBlockEntity(
                position,
                state
        );
    }

    @Nullable
    @Override
    public <T extends BlockEntity>
    BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        if (level.isClientSide) {
            return null;
        }

        return createTickerHelper(
                type,
                ModBlockEntities.MANA_PERCOLATOR,
                ManaPercolatorBlockEntity::serverTick
        );
    }

    @Override
    protected RenderShape getRenderShape(
            BlockState state
    ) {
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
    public BlockState getStateForPlacement(
            BlockPlaceContext context
    ) {
        return defaultBlockState()
                .setValue(
                        FACING,
                        context.getHorizontalDirection()
                                .getOpposite()
                );
    }

    @Override
    protected BlockState rotate(
            BlockState state,
            Rotation rotation
    ) {
        return state.setValue(
                FACING,
                rotation.rotate(
                        state.getValue(FACING)
                )
        );
    }

    @Override
    protected BlockState mirror(
            BlockState state,
            Mirror mirror
    ) {
        return state.rotate(
                mirror.getRotation(
                        state.getValue(FACING)
                )
        );
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<
                    net.minecraft.world.level.block.Block,
                    BlockState
                    > builder
    ) {
        builder.add(FACING);
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
        if (!(level.getBlockEntity(position)
                instanceof ManaPercolatorBlockEntity
                percolator)) {

            return ItemInteractionResult.FAIL;
        }

        if (heldStack.is(ModBlocks.CANISTER.asItem())) {
            return handleCanisterInsertion(
                    heldStack,
                    state,
                    level,
                    position,
                    player,
                    hitResult,
                    percolator
            );
        }

        if (heldStack.is(Items.WATER_BUCKET)) {
            if (!percolator.canInsertWaterBucket()) {
                return ItemInteractionResult.FAIL;
            }

            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }

            percolator.insertWaterBucket();

            if (!player.getAbilities().instabuild) {
                player.setItemInHand(
                        hand,
                        new ItemStack(Items.BUCKET)
                );
            }

            level.playSound(
                    null,
                    position,
                    SoundEvents.BUCKET_EMPTY,
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

        if (heldStack.is(Items.BUCKET)) {
            if (!percolator.hasFinishedMana()) {
                return ItemInteractionResult.FAIL;
            }

            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }

            percolator.removeFinishedMana();

            ItemStack replacement =
                    ItemUtils.createFilledResult(
                            heldStack,
                            player,
                            new ItemStack(
                                    ModItems.MANA_BUCKET
                            )
                    );

            player.setItemInHand(
                    hand,
                    replacement
            );

            level.playSound(
                    null,
                    position,
                    SoundEvents.BUCKET_FILL,
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

        if (heldStack.is(ModItemTags.GEMS)) {
            if (!percolator.canInsertGem(heldStack)) {
                return ItemInteractionResult.FAIL;
            }

            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }

            percolator.insertGem(heldStack);

            if (!player.getAbilities().instabuild) {
                heldStack.shrink(1);
            }

            playMountSound(
                    level,
                    position,
                    false
            );

            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult
                .PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private static ItemInteractionResult handleCanisterInsertion(
            ItemStack heldStack,
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            BlockHitResult hitResult,
            ManaPercolatorBlockEntity percolator
    ) {
        MountTarget target =
                getMountTarget(
                        state,
                        hitResult.getDirection()
                );

        if (target == MountTarget.CHAMBER) {
            return ItemInteractionResult.FAIL;
        }

        boolean canMount =
                target == MountTarget.OUTPUT
                        ? percolator.canMountOutputCanister(
                                heldStack
                        )
                        : percolator.canMountInputCanister(
                                heldStack
                        );

        if (!canMount) {
            return ItemInteractionResult.FAIL;
        }

        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        boolean mounted =
                target == MountTarget.OUTPUT
                        ? percolator.mountOutputCanister(
                                heldStack
                        )
                        : percolator.mountInputCanister(
                                heldStack
                        );

        if (!mounted) {
            return ItemInteractionResult.FAIL;
        }

        if (!player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        playMountSound(
                level,
                position,
                false
        );

        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            BlockHitResult hitResult
    ) {
        if (!(level.getBlockEntity(position)
                instanceof ManaPercolatorBlockEntity
                percolator)) {

            return InteractionResult.PASS;
        }

        MountTarget target =
                getMountTarget(
                        state,
                        hitResult.getDirection()
                );

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack removedStack =
                switch (target) {
                    case INPUT ->
                            percolator.removeInputCanister();

                    case OUTPUT ->
                            percolator.removeOutputCanister();

                    case CHAMBER ->
                            percolator.removeGem();
                };

        if (removedStack.isEmpty()) {
            /*
             * A click aimed at a mount never falls through and removes
             * the chamber gem by accident.
             */
            return InteractionResult.SUCCESS;
        }

        giveOrDrop(
                player,
                removedStack
        );

        playMountSound(
                level,
                position,
                true
        );

        return InteractionResult.SUCCESS;
    }

    private static void giveOrDrop(
            Player player,
            ItemStack stack
    ) {
        if (!player.getInventory().add(stack)) {
            player.drop(
                    stack,
                    false
            );
        }
    }

    private static void playMountSound(
            Level level,
            BlockPos position,
            boolean removing
    ) {
        level.playSound(
                null,
                position,
                removing
                        ? SoundEvents.ITEM_FRAME_REMOVE_ITEM
                        : SoundEvents.ITEM_FRAME_ADD_ITEM,
                SoundSource.BLOCKS,
                0.7F,
                1.0F
        );
    }

    private static MountTarget getMountTarget(
            BlockState state,
            Direction clickedFace
    ) {
        if (clickedFace == Direction.UP) {
            return MountTarget.OUTPUT;
        }

        Direction visualLeft =
                state.getValue(FACING)
                        .getCounterClockWise();

        if (clickedFace == visualLeft) {
            return MountTarget.INPUT;
        }

        return MountTarget.CHAMBER;
    }

    @Override
    public void animateTick(
            BlockState state,
            Level level,
            BlockPos position,
            RandomSource random
    ) {
        if (!(level.getBlockEntity(position)
                instanceof ManaPercolatorBlockEntity
                percolator)
                || !percolator.isProcessing()) {

            return;
        }

        int particleCount =
                1 + random.nextInt(2);

        for (int index = 0;
             index < particleCount;
             index++) {

            double x =
                    position.getX()
                            + 0.28D
                            + random.nextDouble()
                            * 0.44D;

            double y =
                    position.getY()
                            + 0.36D
                            + random.nextDouble()
                            * 0.30D;

            double z =
                    position.getZ()
                            + 0.28D
                            + random.nextDouble()
                            * 0.44D;

            level.addParticle(
                    ParticleTypes.BUBBLE_COLUMN_UP,
                    x,
                    y,
                    z,
                    0.0D,
                    0.035D,
                    0.0D
            );
        }
    }

    @Override
    protected void onRemove(
            BlockState state,
            Level level,
            BlockPos position,
            BlockState newState,
            boolean movedByPiston
    ) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(position)
                instanceof ManaPercolatorBlockEntity
                percolator) {

            percolator.dropStoredItems();
        }

        super.onRemove(
                state,
                level,
                position,
                newState,
                movedByPiston
        );
    }

    private enum MountTarget {
        INPUT,
        OUTPUT,
        CHAMBER
    }
}
