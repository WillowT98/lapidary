package name.lapidary.network;

import name.lapidary.Lapidary;
import name.lapidary.progression.LapidaryInsight;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class ModNetworking {

    private ModNetworking() {
    }

    public static void initialize() {
        Lapidary.LOGGER.info("Registering Lapidary networking");

        /*
         * This registration occurs through the common initializer, so it
         * happens on both the physical client and dedicated server.
         */
        PayloadTypeRegistry.playS2C().register(
                InsightSyncPayload.TYPE,
                InsightSyncPayload.STREAM_CODEC
        );

        /*
         * Send the saved value when the player first enters the world.
         */
        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) ->
                        LapidaryInsight.sync(handler.player)
        );

        /*
         * The attachment is copied to the new player entity on death.
         * Send that copied value to the client after respawning.
         */
        ServerPlayerEvents.AFTER_RESPAWN.register(
                (oldPlayer, newPlayer, alive) ->
                        LapidaryInsight.sync(newPlayer)
        );
    }
}