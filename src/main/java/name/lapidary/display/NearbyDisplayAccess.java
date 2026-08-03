package name.lapidary.display;

import name.lapidary.block.entity.DisplayStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Server-side discovery and transaction helpers for Lapidary workshop displays.
 *
 * Searches happen only when a menu opens or a crafting action occurs. Nothing
 * in this class adds a per-tick scan.
 */
public final class NearbyDisplayAccess {

    public static final int SEARCH_RADIUS = 8;

    private NearbyDisplayAccess() {
    }

    public static Optional<Source> findSource(
            Level level,
            BlockPos origin,
            List<DisplayKind> kindPriority,
            Predicate<ItemStack> predicate
    ) {
        for (DisplayKind kind : kindPriority) {
            for (DisplayStorageBlockEntity display :
                    findDisplays(level, origin, kind)) {

                for (int slot = 0;
                     slot < display.getContainerSize();
                     slot++) {

                    ItemStack stack = display.getItem(slot);

                    if (!stack.isEmpty()
                            && predicate.test(stack)) {

                        return Optional.of(
                                new Source(
                                        display,
                                        slot,
                                        stack.copyWithCount(1)
                                )
                        );
                    }
                }
            }
        }

        return Optional.empty();
    }

    public static Optional<Destination> findDestination(
            Level level,
            BlockPos origin,
            ItemStack offered,
            List<DisplayKind> kindPriority
    ) {
        if (offered.isEmpty()) {
            return Optional.empty();
        }

        for (DisplayKind kind : kindPriority) {
            for (DisplayStorageBlockEntity display :
                    findDisplays(level, origin, kind)) {

                for (int slot = 0;
                     slot < display.getContainerSize();
                     slot++) {

                    if (display.canInsertIntoSlot(
                            slot,
                            offered
                    )) {
                        return Optional.of(
                                new Destination(
                                        display,
                                        slot
                                )
                        );
                    }
                }
            }
        }

        return Optional.empty();
    }

    private static List<DisplayStorageBlockEntity>
    findDisplays(
            Level level,
            BlockPos origin,
            DisplayKind kind
    ) {
        List<DisplayStorageBlockEntity> found =
                new ArrayList<>();

        BlockPos minimum =
                origin.offset(
                        -SEARCH_RADIUS,
                        -SEARCH_RADIUS,
                        -SEARCH_RADIUS
                );

        BlockPos maximum =
                origin.offset(
                        SEARCH_RADIUS,
                        SEARCH_RADIUS,
                        SEARCH_RADIUS
                );

        for (BlockPos cursor :
                BlockPos.betweenClosed(
                        minimum,
                        maximum
                )) {

            if (origin.distSqr(cursor)
                    > SEARCH_RADIUS
                    * SEARCH_RADIUS) {

                continue;
            }

            if (!level.hasChunkAt(cursor)) {
                continue;
            }

            if (level.getBlockEntity(cursor)
                    instanceof DisplayStorageBlockEntity
                    display
                    && display.getDisplayKind() == kind) {

                found.add(display);
            }
        }

        found.sort(
                Comparator
                        .comparingDouble(
                                (DisplayStorageBlockEntity display) ->
                                        origin.distSqr(
                                                display.getBlockPos()
                                        )
                        )
                        .thenComparingLong(
                                display ->
                                        display.getBlockPos()
                                                .asLong()
                        )
        );

        return found;
    }

    public record Source(
            DisplayStorageBlockEntity display,
            int slot,
            ItemStack expected
    ) {
        public ItemStack currentStack() {
            return display.getItem(slot);
        }

        public boolean isStillValid() {
            ItemStack current =
                    currentStack();

            return !current.isEmpty()
                    && ItemStack.isSameItemSameComponents(
                            current,
                            expected
                    );
        }

        public boolean consumeOne() {
            if (!isStillValid()) {
                return false;
            }

            ItemStack removed =
                    display.removeItem(
                            slot,
                            1
                    );

            return !removed.isEmpty();
        }

        public void restoreOne() {
            ItemStack remainder =
                    display.insertIntoSlot(
                            slot,
                            expected.copy(),
                            false
                    );

            if (!remainder.isEmpty()) {
                remainder =
                        display.insertAnywhere(
                                remainder,
                                false
                        );
            }

            /*
             * A second player could theoretically fill the display between
             * simulation and rollback. Never delete the restored ingredient;
             * drop it beside its original display as a final fallback.
             */
            if (!remainder.isEmpty()
                    && display.getLevel() != null
                    && !display.getLevel().isClientSide) {

                Block.popResource(
                        display.getLevel(),
                        display.getBlockPos(),
                        remainder
                );
            }
        }
    }

    public record Destination(
            DisplayStorageBlockEntity display,
            int slot
    ) {
        public boolean insert(
                ItemStack stack
        ) {
            return display.insertIntoSlot(
                    slot,
                    stack.copy(),
                    false
            ).isEmpty();
        }
    }
}
