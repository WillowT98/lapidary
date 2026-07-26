package name.lapidary.window;

import name.lapidary.block.CustomWindowControllerBlock;
import name.lapidary.block.CustomWindowSegmentBlock;
import name.lapidary.block.ModBlocks;
import name.lapidary.block.entity.CustomWindowControllerBlockEntity;
import name.lapidary.item.CustomStainedGlassItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class WindowStructure {

    /*
     * Removing one cell causes neighbor/state removal callbacks for every
     * other cell. This thread-local guard prevents those callbacks from
     * recursively dismantling the same structure.
     *
     * Minecraft performs world mutation on one server thread, but a
     * thread-local guard also keeps integrated-client and server work
     * isolated from each other.
     */
    private static final ThreadLocal<Boolean>
            DISMANTLING =
            ThreadLocal.withInitial(
                    () -> false
            );

    private WindowStructure() {
    }

    public static boolean isDismantling() {
        return DISMANTLING.get();
    }

    /**
     * The direction that appears to the viewer as the right-hand side of
     * a window whose front points in {@code facing}.
     */
    public static Direction rightDirection(
            Direction facing
    ) {
        return switch (facing) {
            case NORTH -> Direction.WEST;
            case SOUTH -> Direction.EAST;
            case EAST -> Direction.NORTH;
            case WEST -> Direction.SOUTH;
            default -> Direction.EAST;
        };
    }

    public static BlockPos positionFor(
            BlockPos controllerPosition,
            Direction facing,
            int horizontalOffset,
            int verticalOffset
    ) {
        return controllerPosition
                .relative(
                        rightDirection(facing),
                        horizontalOffset
                )
                .above(
                        verticalOffset
                );
    }

    public static BlockPos controllerPosition(
            BlockPos anyPosition,
            BlockState state
    ) {
        if (state.is(
                ModBlocks.CUSTOM_WINDOW_CONTROLLER
        )) {
            return anyPosition;
        }

        if (!state.is(
                ModBlocks.CUSTOM_WINDOW_SEGMENT
        )) {
            return null;
        }

        Direction facing =
                state.getValue(
                        CustomWindowSegmentBlock.FACING
                );

        int horizontalOffset =
                state.getValue(
                        CustomWindowSegmentBlock.OFFSET_X
                );

        int verticalOffset =
                state.getValue(
                        CustomWindowSegmentBlock.OFFSET_Y
                );

        return anyPosition
                .relative(
                        rightDirection(facing)
                                .getOpposite(),
                        horizontalOffset
                )
                .below(
                        verticalOffset
                );
    }

    public static Optional<WindowDesign> findDesign(
            BlockGetter level,
            BlockPos anyPosition,
            BlockState state
    ) {
        BlockPos controllerPosition =
                controllerPosition(
                        anyPosition,
                        state
                );

        if (controllerPosition == null) {
            return Optional.empty();
        }

        if (level.getBlockEntity(
                controllerPosition
        ) instanceof CustomWindowControllerBlockEntity
                controller) {

            return controller.getDesign();
        }

        return Optional.empty();
    }

    public static boolean canPlace(
            Level level,
            Player player,
            BlockPlaceContext placementContext,
            BlockPos controllerPosition,
            Direction facing,
            WindowDesign design
    ) {
        for (int y = 0;
             y < design.blockHeight();
             y++) {

            for (int x = 0;
                 x < design.blockWidth();
                 x++) {

                BlockPos position =
                        positionFor(
                                controllerPosition,
                                facing,
                                x,
                                y
                        );

                if (position.getY()
                        < level.getMinBuildHeight()
                        || position.getY()
                        >= level.getMaxBuildHeight()) {

                    return false;
                }

                if (!level.getWorldBorder()
                        .isWithinBounds(position)
                        || !level.hasChunkAt(position)) {

                    return false;
                }

                if (player != null
                        && !level.mayInteract(
                        player,
                        position
                )) {
                    return false;
                }

                BlockPlaceContext cellContext =
                        BlockPlaceContext.at(
                                placementContext,
                                position,
                                facing
                        );

                if (!level.getBlockState(position)
                        .canBeReplaced(
                                cellContext
                        )) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Places every cell only after the complete footprint has been
     * validated. A failed mutation rolls back all cells already placed.
     */
    public static boolean place(
            Level level,
            Player player,
            BlockPlaceContext placementContext,
            BlockPos controllerPosition,
            Direction facing,
            WindowDesign design
    ) {
        if (!canPlace(
                level,
                player,
                placementContext,
                controllerPosition,
                facing,
                design
        )) {
            return false;
        }

        List<BlockPos> placedPositions =
                new ArrayList<>();

        DISMANTLING.set(true);

        try {
            BlockState controllerState =
                    ModBlocks.CUSTOM_WINDOW_CONTROLLER
                            .defaultBlockState()
                            .setValue(
                                    CustomWindowControllerBlock.FACING,
                                    facing
                            );

            if (!level.setBlock(
                    controllerPosition,
                    controllerState,
                    Block.UPDATE_ALL
            )) {
                return false;
            }

            placedPositions.add(
                    controllerPosition
            );

            if (!(level.getBlockEntity(
                    controllerPosition
            ) instanceof CustomWindowControllerBlockEntity
                    controller)) {

                rollback(
                        level,
                        placedPositions
                );

                return false;
            }

            controller.setDesign(
                    design
            );

            for (int y = 0;
                 y < design.blockHeight();
                 y++) {

                for (int x = 0;
                     x < design.blockWidth();
                     x++) {

                    if (x == 0
                            && y == 0) {
                        continue;
                    }

                    BlockPos position =
                            positionFor(
                                    controllerPosition,
                                    facing,
                                    x,
                                    y
                            );

                    BlockState segmentState =
                            ModBlocks.CUSTOM_WINDOW_SEGMENT
                                    .defaultBlockState()
                                    .setValue(
                                            CustomWindowSegmentBlock.FACING,
                                            facing
                                    )
                                    .setValue(
                                            CustomWindowSegmentBlock.OFFSET_X,
                                            x
                                    )
                                    .setValue(
                                            CustomWindowSegmentBlock.OFFSET_Y,
                                            y
                                    );

                    if (!level.setBlock(
                            position,
                            segmentState,
                            Block.UPDATE_ALL
                    )) {
                        rollback(
                                level,
                                placedPositions
                        );

                        return false;
                    }

                    placedPositions.add(
                            position
                    );
                }
            }

            return true;
        } finally {
            DISMANTLING.set(false);
        }
    }

    /**
     * Removes the complete window and optionally returns one design item.
     */
    public static void dismantle(
            Level level,
            BlockPos anyPosition,
            boolean dropItem
    ) {
        dismantle(
                level,
                anyPosition,
                level.getBlockState(
                        anyPosition
                ),
                dropItem
        );
    }

    /**
     * Variant used from removal callbacks, where the level may already
     * expose the replacement state at {@code anyPosition}. The supplied
     * state is the window state that is actually being removed.
     */
    public static void dismantle(
            Level level,
            BlockPos anyPosition,
            BlockState initialState,
            boolean dropItem
    ) {
        if (level.isClientSide
                || isDismantling()) {
            return;
        }

        BlockPos controllerPosition =
                controllerPosition(
                        anyPosition,
                        initialState
                );

        if (controllerPosition == null) {
            return;
        }

        BlockState controllerState =
                level.getBlockState(
                        controllerPosition
                );

        Direction facing =
                controllerState.is(
                        ModBlocks.CUSTOM_WINDOW_CONTROLLER
                )
                        ? controllerState.getValue(
                        CustomWindowControllerBlock.FACING
                )
                        : initialState.getValue(
                        CustomWindowSegmentBlock.FACING
                );

        WindowDesign design =
                findDesign(
                        level,
                        anyPosition,
                        initialState
                ).orElse(null);

        DISMANTLING.set(true);

        try {
            if (design != null) {
                for (int y = 0;
                     y < design.blockHeight();
                     y++) {

                    for (int x = 0;
                         x < design.blockWidth();
                         x++) {

                        removeExpectedCell(
                                level,
                                controllerPosition,
                                facing,
                                x,
                                y
                        );
                    }
                }
            } else {
                /*
                 * A damaged/corrupt structure may have lost its block
                 * entity. Sweep the maximum possible footprint, but only
                 * remove cells that resolve back to this controller.
                 */
                for (int y = 0;
                     y < WindowDesign.MAX_BLOCK_SIZE;
                     y++) {

                    for (int x = 0;
                         x < WindowDesign.MAX_BLOCK_SIZE;
                         x++) {

                        removeExpectedCell(
                                level,
                                controllerPosition,
                                facing,
                                x,
                                y
                        );
                    }
                }
            }
        } finally {
            DISMANTLING.set(false);
        }

        if (dropItem
                && design != null) {

            Block.popResource(
                    level,
                    controllerPosition,
                    CustomStainedGlassItem.create(
                            design
                    )
            );
        }
    }

    private static void removeExpectedCell(
            Level level,
            BlockPos controllerPosition,
            Direction facing,
            int horizontalOffset,
            int verticalOffset
    ) {
        BlockPos position =
                positionFor(
                        controllerPosition,
                        facing,
                        horizontalOffset,
                        verticalOffset
                );

        BlockState state =
                level.getBlockState(
                        position
                );

        if (horizontalOffset == 0
                && verticalOffset == 0) {

            if (state.is(
                    ModBlocks.CUSTOM_WINDOW_CONTROLLER
            )) {
                level.removeBlock(
                        position,
                        false
                );
            }

            return;
        }

        if (!state.is(
                ModBlocks.CUSTOM_WINDOW_SEGMENT
        )) {
            return;
        }

        BlockPos resolvedController =
                controllerPosition(
                        position,
                        state
                );

        if (controllerPosition.equals(
                resolvedController
        )) {
            level.removeBlock(
                    position,
                    false
            );
        }
    }

    private static void rollback(
            Level level,
            List<BlockPos> positions
    ) {
        for (int index =
                positions.size() - 1;
             index >= 0;
             index--) {

            level.removeBlock(
                    positions.get(index),
                    false
            );
        }
    }
}
