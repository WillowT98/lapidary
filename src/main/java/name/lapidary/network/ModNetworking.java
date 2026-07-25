package name.lapidary.network;

import name.lapidary.Lapidary;
import name.lapidary.block.ModBlocks;
import name.lapidary.item.MageBackpackAccess;
import name.lapidary.magic.PlayerMagic;
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
         * Server-to-client state synchronization.
         */
        PayloadTypeRegistry.playS2C().register(
                InsightSyncPayload.TYPE,
                InsightSyncPayload.STREAM_CODEC
        );

        PayloadTypeRegistry.playS2C().register(
                TomeOpenPayload.TYPE,
                TomeOpenPayload.STREAM_CODEC
        );

        PayloadTypeRegistry.playS2C().register(
                TomeStatePayload.TYPE,
                TomeStatePayload.STREAM_CODEC
        );

        PayloadTypeRegistry.playS2C().register(
                MagicStatePayload.TYPE,
                MagicStatePayload.STREAM_CODEC
        );

        /*
         * Backpack interaction.
         */
        PayloadTypeRegistry.playC2S().register(
                OpenMageBackpackPayload.TYPE,
                OpenMageBackpackPayload.STREAM_CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
                OpenMageBackpackPayload.TYPE,
                (payload, context) ->
                        MageBackpackAccess.openEquipped(
                                context.player()
                        )
        );

        /*
         * Tome requests.
         */
        PayloadTypeRegistry.playC2S().register(
                TomePurchasePayload.TYPE,
                TomePurchasePayload.STREAM_CODEC
        );

        PayloadTypeRegistry.playC2S().register(
                TomePrepareSpellPayload.TYPE,
                TomePrepareSpellPayload.STREAM_CODEC
        );

        PayloadTypeRegistry.playC2S().register(
                TomeClearPreparedSpellPayload.TYPE,
                TomeClearPreparedSpellPayload
                        .STREAM_CODEC
        );

        PayloadTypeRegistry.playC2S().register(
                TomeSwapPreparedSpellsPayload.TYPE,
                TomeSwapPreparedSpellsPayload
                        .STREAM_CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
                TomePurchasePayload.TYPE,
                (payload, context) -> {
                    ServerPlayer player =
                            context.player();

                    if (!isValidTomeRequest(
                            player,
                            payload.tablePosition()
                    )) {
                        return;
                    }

                    TomeProgression.tryPurchase(
                            player,
                            payload.nodeId()
                    );

                    /*
                     * Always return authoritative progression state,
                     * including when the request was rejected.
                     */
                    TomeProgression.syncOpenScreen(
                            player
                    );
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(
                TomePrepareSpellPayload.TYPE,
                (payload, context) -> {
                    ServerPlayer player =
                            context.player();

                    if (!isValidTomeRequest(
                            player,
                            payload.tablePosition()
                    )) {
                        /*
                         * The client updated optimistically, so correct it.
                         */
                        PlayerMagic.sync(player);
                        return;
                    }

                    boolean changed =
                            PlayerMagic.prepareSpell(
                                    player,
                                    payload.slot(),
                                    payload.spellId()
                            );

                    /*
                     * Successful mutations synchronize through
                     * PlayerMagic.set(). Rejected mutations still need
                     * an authoritative correction.
                     */
                    if (!changed) {
                        PlayerMagic.sync(player);
                    }
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(
                TomeClearPreparedSpellPayload.TYPE,
                (payload, context) -> {
                    ServerPlayer player =
                            context.player();

                    if (!isValidTomeRequest(
                            player,
                            payload.tablePosition()
                    )) {
                        PlayerMagic.sync(player);
                        return;
                    }

                    boolean changed =
                            PlayerMagic.clearPreparedSlot(
                                    player,
                                    payload.slot()
                            );

                    if (!changed) {
                        PlayerMagic.sync(player);
                    }
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(
                TomeSwapPreparedSpellsPayload.TYPE,
                (payload, context) -> {
                    ServerPlayer player =
                            context.player();

                    if (!isValidTomeRequest(
                            player,
                            payload.tablePosition()
                    )) {
                        PlayerMagic.sync(player);
                        return;
                    }

                    boolean changed =
                            PlayerMagic.swapPreparedSlots(
                                    player,
                                    payload.firstSlot(),
                                    payload.secondSlot()
                            );

                    if (!changed) {
                        PlayerMagic.sync(player);
                    }
                }
        );

        /*
         * Persistent player data must be sent when the client joins.
         */
        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> {
                    LapidaryInsight.sync(
                            handler.player
                    );

                    PlayerMagic.sync(
                            handler.player
                    );
                }
        );

        /*
         * Attachments copy across death, but the new client-side player
         * still needs the authoritative values sent again.
         */
        ServerPlayerEvents.AFTER_RESPAWN.register(
                (oldPlayer, newPlayer, alive) -> {
                    LapidaryInsight.sync(
                            newPlayer
                    );

                    PlayerMagic.sync(
                            newPlayer
                    );
                }
        );
    }

    private static boolean isValidTomeRequest(
            ServerPlayer player,
            BlockPos tablePosition
    ) {
        if (!player.level().isLoaded(
                tablePosition
        )) {
            return false;
        }

        if (!player.level()
                .getBlockState(tablePosition)
                .is(ModBlocks.TOME_TABLE)) {

            return false;
        }

        double distanceSquared =
                player.distanceToSqr(
                        tablePosition.getX() + 0.5D,
                        tablePosition.getY() + 0.5D,
                        tablePosition.getZ() + 0.5D
                );

        return distanceSquared
                <= MAX_TOME_DISTANCE_SQUARED;
    }
}