package name.lapidary.network;

import name.lapidary.Lapidary;
import name.lapidary.block.ModBlocks;
import name.lapidary.progression.LapidaryInsight;
import name.lapidary.progression.tome.TomeProgression;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public final class ModNetworking {

    private static final double MAX_TOME_DISTANCE_SQUARED =
            64.0D;

    private ModNetworking() {
    }

    public static void initialize() {
        Lapidary.LOGGER.info(
                "Registering Lapidary networking"
        );

        /*
         * Existing Insight synchronization.
         */
        PayloadTypeRegistry.playS2C().register(
                InsightSyncPayload.TYPE,
                InsightSyncPayload.STREAM_CODEC
        );

        /*
         * Tome server-to-client payloads.
         */
        PayloadTypeRegistry.playS2C().register(
                TomeOpenPayload.TYPE,
                TomeOpenPayload.STREAM_CODEC
        );

        PayloadTypeRegistry.playS2C().register(
                TomeStatePayload.TYPE,
                TomeStatePayload.STREAM_CODEC
        );

        /*
         * Tome client-to-server purchase request.
         */
        PayloadTypeRegistry.playC2S().register(
                TomePurchasePayload.TYPE,
                TomePurchasePayload.STREAM_CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
                TomePurchasePayload.TYPE,
                (payload, context) -> {
                    ServerPlayer player =
                            context.player();

                    BlockPos tablePosition =
                            payload.tablePosition();

                    /*
                     * Reject requests for unloaded positions.
                     */
                    if (!player.level().isLoaded(
                            tablePosition
                    )) {
                        return;
                    }

                    /*
                     * The referenced block must still be a Tome Table.
                     */
                    if (!player.level()
                            .getBlockState(tablePosition)
                            .is(ModBlocks.TOME_TABLE)) {

                        return;
                    }

                    /*
                     * Prevent remote purchases after walking away and
                     * prevent fabricated packets targeting distant tables.
                     */
                    double distanceSquared =
                            player.distanceToSqr(
                                    tablePosition.getX()
                                            + 0.5D,
                                    tablePosition.getY()
                                            + 0.5D,
                                    tablePosition.getZ()
                                            + 0.5D
                            );

                    if (distanceSquared
                            > MAX_TOME_DISTANCE_SQUARED) {

                        return;
                    }

                    TomeProgression.tryPurchase(
                            player,
                            payload.nodeId()
                    );

                    /*
                     * Return the authoritative state regardless of whether
                     * the purchase succeeded.
                     */
                    TomeProgression.syncOpenScreen(
                            player
                    );
                }
        );

        /*
         * Existing Insight synchronization when joining.
         */
        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) ->
                        LapidaryInsight.sync(
                                handler.player
                        )
        );

        /*
         * Existing Insight synchronization after respawning.
         */
        ServerPlayerEvents.AFTER_RESPAWN.register(
                (oldPlayer, newPlayer, alive) ->
                        LapidaryInsight.sync(
                                newPlayer
                        )
        );
    }
}