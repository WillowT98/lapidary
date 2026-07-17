package name.lapidary.sifting;

import name.lapidary.block.ModBlocks;
import name.lapidary.item.ModItems;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
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
         * This fires when the mining progress has completed, immediately
         * before Minecraft would destroy the block.
         */
        PlayerBlockBreakEvents.BEFORE.register(
                (level, player, pos, state, blockEntity) -> {
                    ItemStack heldItem = player.getMainHandItem();
                    BlockState result = getSiftingResult(state);

                    if (!heldItem.is(ModItems.SIEVE) || result == null) {
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
                     * - Sand remains in the world for the moment.
                     * - No sand item is dropped.
                     * - The CANCELED event runs next.
                     */
                    return false;
                }
        );

        PlayerBlockBreakEvents.CANCELED.register(
                (level, player, pos, state, blockEntity) -> {
                    PendingSift pending =
                            PENDING_SIFTS.remove(player.getUUID());

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

                    ItemStack heldItem = player.getMainHandItem();

                    /*
                     * Verify that the player is still holding the sieve.
                     */
                    if (!heldItem.is(ModItems.SIEVE)) {
                        return;
                    }

                    level.setBlockAndUpdate(
                            pos,
                            pending.resultState()
                    );

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

    private static BlockState getSiftingResult(BlockState input) {
        //this is where we can intercept anything else for sifting!
        if (input.is(Blocks.SAND)) {
            return ModBlocks.FINE_SAND.defaultBlockState();
        }

        return null;
    }

    private record PendingSift(
            BlockPos position,
            BlockState originalState,
            BlockState resultState
    ) {
    }
}