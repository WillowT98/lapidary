package name.lapidary.block;

import com.mojang.serialization.MapCodec;
import name.lapidary.block.entity.CanisterBlockEntity;
import name.lapidary.block.entity.ManaPercolatorBlockEntity;
import name.lapidary.block.entity.ModBlockEntities;
import name.lapidary.fluid.CanisterFluidStorage;
import name.lapidary.fluid.CanisterItemContents;
import name.lapidary.fluid.CanisterLiquid;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
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
 * Menu-free mana processor and controller for a three-block structure.
 *
 * The controller owns the gem and one-bucket chamber. Canisters are real
 * neighboring canister blocks:
 *
 * - input: one block in front of the horizontal nozzle;
 * - output: one block above the top nozzle.
 */
public final class ManaPercolatorBlock extends BaseEntityBlock {

    public static final MapCodec<ManaPercolatorBlock> CODEC =
            simpleCodec(ManaPercolatorBlock::new);

    public static final DirectionProperty FACING =
            BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE =
            box(
                    0.0D, 0.0D, 0.0D,
                    16.0D, 16.0D, 16.0D
            );

    public ManaPercolatorBlock(
            BlockBehaviour.Properties properties
    ) {
        super(properties);

        registerDefaultState(
                stateDefinition.any()
                        .setValue(FACING, Direction.SOUTH)
        );
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        return new ManaPercolatorBlockEntity(position, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
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
    protected RenderShape getRenderShape(BlockState state) {
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
                rotation.rotate(state.getValue(FACING))
        );
    }

    @Override
    protected BlockState mirror(
            BlockState state,
            Mirror mirror
    ) {
        return state.rotate(
                mirror.getRotation(state.getValue(FACING))
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
                instanceof ManaPercolatorBlockEntity percolator)) {

            return ItemInteractionResult.FAIL;
        }

        if (heldStack.is(ModBlocks.CANISTER.asItem())) {
            return handleCanisterPlacement(
                    heldStack,
                    state,
                    level,
                    position,
                    player,
                    hitResult
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
                            new ItemStack(ModItems.MANA_BUCKET)
                    );

            player.setItemInHand(hand, replacement);

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

            playMountSound(level, position, false);

            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult
                .PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private static ItemInteractionResult handleCanisterPlacement(
            ItemStack heldStack,
            BlockState percolatorState,
            Level level,
            BlockPos percolatorPosition,
            Player player,
            BlockHitResult hitResult
    ) {
        MountTarget target =
                getMountTarget(
                        percolatorState,
                        hitResult.getDirection()
                );

        if (target == MountTarget.CHAMBER) {
            return ItemInteractionResult.FAIL;
        }

        CanisterItemContents.Contents contents =
                CanisterItemContents.read(heldStack);

        if (!isValidForTarget(target, contents)) {
            return ItemInteractionResult.FAIL;
        }

        Direction percolatorFacing =
                percolatorState.getValue(FACING);

        BlockPos canisterPosition =
                target == MountTarget.OUTPUT
                        ? percolatorPosition.above()
                        : percolatorPosition.relative(
                                percolatorFacing
                        );

        if (!level.isEmptyBlock(canisterPosition)) {
            return ItemInteractionResult.FAIL;
        }

        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        Direction canisterAttachment =
                target == MountTarget.OUTPUT
                        ? Direction.DOWN
                        : percolatorFacing.getOpposite();

        BlockState canisterState =
                ModBlocks.CANISTER.defaultBlockState()
                        .setValue(
                                CanisterBlock.FACING,
                                canisterAttachment
                        );

        boolean placed =
                level.setBlock(
                        canisterPosition,
                        canisterState,
                        Block.UPDATE_ALL
                );

        if (!placed
                || !(level.getBlockEntity(canisterPosition)
                instanceof CanisterBlockEntity canister)) {

            return ItemInteractionResult.FAIL;
        }

        if (!contents.isEmpty()) {
            canister.getStorage().insert(
                    contents.liquid(),
                    contents.amount(),
                    false
            );
        }

        if (!player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        playMountSound(
                level,
                canisterPosition,
                false
        );

        level.gameEvent(
                player,
                GameEvent.BLOCK_PLACE,
                canisterPosition
        );

        return ItemInteractionResult.SUCCESS;
    }

    private static boolean isValidForTarget(
            MountTarget target,
            CanisterItemContents.Contents contents
    ) {
        if (target == MountTarget.INPUT) {
            return contents.liquid() == CanisterLiquid.WATER
                    && contents.amount()
                    >= CanisterFluidStorage.BUCKET;
        }

        if (target == MountTarget.OUTPUT) {
            return (contents.isEmpty()
                    || contents.liquid() == CanisterLiquid.MANA)
                    && CanisterFluidStorage.CAPACITY
                    - contents.amount()
                    >= CanisterFluidStorage.BUCKET;
        }

        return false;
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
                instanceof ManaPercolatorBlockEntity percolator)) {

            return InteractionResult.PASS;
        }

        /*
         * Empty-hand clicks aimed at either nozzle do not reach into the
         * chamber and accidentally remove the gem. Real canisters are
         * interacted with directly at their own block positions.
         */
        if (getMountTarget(
                state,
                hitResult.getDirection()
        ) != MountTarget.CHAMBER) {

            return InteractionResult.sidedSuccess(
                    level.isClientSide
            );
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack removedGem =
                percolator.removeGem();

        if (!removedGem.isEmpty()) {
            giveOrDrop(player, removedGem);
            playMountSound(level, position, true);
        }

        return InteractionResult.SUCCESS;
    }

    private static void giveOrDrop(
            Player player,
            ItemStack stack
    ) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
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

        if (clickedFace == state.getValue(FACING)) {
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
                instanceof ManaPercolatorBlockEntity percolator)
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
                            + 0.15D
                            + random.nextDouble()
                            * 0.70D;

            double y =
                    position.getY()
                            + 0.20D
                            + random.nextDouble()
                            * 0.45D;

            double z =
                    position.getZ()
                            + 0.15D
                            + random.nextDouble()
                            * 0.70D;

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
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(position)
                    instanceof ManaPercolatorBlockEntity percolator) {

                percolator.dropStoredItems();
            }

            /*
             * The horizontal input state exists specifically because it
             * is attached to this controller. Drop it safely if the
             * controller disappears. The upright output canister remains
             * as an ordinary independent canister block.
             */
            Direction facing =
                    state.getValue(FACING);

            BlockPos inputPosition =
                    position.relative(facing);

            BlockState inputState =
                    level.getBlockState(inputPosition);

            if (inputState.is(ModBlocks.CANISTER)
                    && inputState.getValue(CanisterBlock.FACING)
                    == facing.getOpposite()) {

                level.destroyBlock(
                        inputPosition,
                        true
                );
            }
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
