package name.lapidary.client.network;

import name.lapidary.client.progression.ClientInsightData;
import name.lapidary.network.InsightSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ClientNetworking {

    private ClientNetworking() {
    }

    public static void initialize() {
        /*
         * Store every Insight update received from the server.
         */
        ClientPlayNetworking.registerGlobalReceiver(
                InsightSyncPayload.TYPE,
                (payload, context) ->
                        ClientInsightData.set(payload.insight())
        );

        /*
         * Prevent one world's value from remaining visible briefly when
         * connecting to a different world or server.
         */
        ClientPlayConnectionEvents.DISCONNECT.register(
                (handler, client) ->
                        ClientInsightData.reset()
        );
    }
}