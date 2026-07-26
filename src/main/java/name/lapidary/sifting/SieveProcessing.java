package name.lapidary.sifting;

import name.lapidary.block.ModBlocks;
import name.lapidary.item.ModItems;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SieveProcessing {

    /*
     * A player can only actively finish breaking one block at a time.
     * This records that Lapidary was responsible for canceling the break.
     */
    private static final Map<UUID, PendingSift> PENDING_SIFTS =
            new HashMap<>();

    private SieveProcessing() {
    }

    public static void initialize() {
        /*
         * This fires when mining progress has completed, immediately
         * before Minecraft would normally destroy the block.
         */
        PlayerBlockBreakEvents.BEFORE.register(
                (level, player, pos, state, blockEntity) -> {
                    ItemStack heldItem =
                            player.getMainHandItem();

                    SiftingResult result =
                            getSiftingResult(state);

                    if (!heldItem.is(ModItems.SIEVE)
                            || result == null) {
                        return true;
                    }

                    PENDING_SIFTS.put(
                            player.getUUID(),
                            new PendingSift(
                                    pos.immutable(),
                                    state,
                                    result
                            )
                    );

                    /*
                     * Cancel ordinary destruction:
                     *
                     * - The original block remains temporarily.
                     * - Its normal loot table is not used.
                     * - The CANCELED event performs the sifting result.
                     */
                    return false;
                }
        );

        PlayerBlockBreakEvents.CANCELED.register(
                (level, player, pos, state, blockEntity) -> {
                    PendingSift pending =
                            PENDING_SIFTS.remove(
                                    player.getUUID()
                            );

                    if (pending == null
                            || !pending.position().equals(pos)) {
                        return;
                    }

                    /*
                     * Do not overwrite the location if another system
                     * changed the block in the meantime.
                     */
                    if (!level.getBlockState(pos)
                            .equals(pending.originalState())) {
                        return;
                    }

                    ItemStack heldItem =
                            player.getMainHandItem();

                    /*
                     * Verify that the player is still holding the sieve.
                     */
                    if (!heldItem.is(ModItems.SIEVE)) {
                        return;
                    }

                    SiftingResult result =
                            pending.result();

                    /*
                     * Gravel uses air as its resulting state, while sand,
                     * dirt, and grass transform into another block.
                     */
                    level.setBlockAndUpdate(
                            pos,
                            result.resultState()
                    );

                    /*
                     * Spawn any item produced by the sifting operation.
                     * A copy prevents the stored result stack from being
                     * modified by Minecraft's item-spawning code.
                     */
                    if (!result.droppedItem().isEmpty()) {
                        Block.popResource(
                                level,
                                pos,
                                result.droppedItem().copy()
                        );
                    }

                    if (!player.getAbilities().instabuild) {
                        heldItem.hurtAndBreak(
                                1,
                                player,
                                EquipmentSlot.MAINHAND
                        );
                    }
                }
        );
    }

    private static SiftingResult getSiftingResult(
            BlockState input
    ) {
        if (input.is(Blocks.SAND)) {
            return replaceWith(
                    ModBlocks.FINE_SAND
                            .defaultBlockState()
            );
        }

        if (input.is(Blocks.DIRT)
                || input.is(Blocks.GRASS_BLOCK)) {
            return replaceWith(
                    ModBlocks.LOAM
                            .defaultBlockState()
            );
        }

        if (input.is(Blocks.GRAVEL)) {
            return removeAndDrop(
                    new ItemStack(Items.FLINT)
            );
        }

        return null;
    }

    private static SiftingResult replaceWith(
            BlockState resultState
    ) {
        return new SiftingResult(
                resultState,
                ItemStack.EMPTY
        );
    }

    private static SiftingResult removeAndDrop(
            ItemStack droppedItem
    ) {
        return new SiftingResult(
                Blocks.AIR.defaultBlockState(),
                droppedItem
        );
    }

    private record PendingSift(
            BlockPos position,
            BlockState originalState,
            SiftingResult result
    ) {
    }

    private record SiftingResult(
            BlockState resultState,
            ItemStack droppedItem
    ) {
    }
}