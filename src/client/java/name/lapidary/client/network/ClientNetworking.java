package name.lapidary.client.network;

import name.lapidary.client.progression.ClientInsightData;
import name.lapidary.client.screen.TomeScreen;
import name.lapidary.network.InsightSyncPayload;
import name.lapidary.network.TomeOpenPayload;
import name.lapidary.network.TomeStatePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ClientNetworking {

    private ClientNetworking() {
    }

    public static void initialize() {
        /*
         * Existing Insight updates.
         */
        ClientPlayNetworking.registerGlobalReceiver(
                InsightSyncPayload.TYPE,
                (payload, context) ->
                        ClientInsightData.set(
                                payload.insight()
                        )
        );

        /*
         * Open a new Tome screen with authoritative server state.
         */
        ClientPlayNetworking.registerGlobalReceiver(
                TomeOpenPayload.TYPE,
                (payload, context) ->
                        context.client().execute(
                                () -> {
                                    ClientInsightData.set(
                                            payload.insight()
                                    );

                                    context.client()
                                            .setScreen(
                                                    new TomeScreen(
                                                            payload
                                                                    .tablePosition(),
                                                            payload
                                                                    .insight(),
                                                            payload
                                                                    .purchasedMask()
                                                    )
                                            );
                                }
                        )
        );

        /*
         * Refresh an already-open Tome after a purchase request.
         */
        ClientPlayNetworking.registerGlobalReceiver(
                TomeStatePayload.TYPE,
                (payload, context) ->
                        context.client().execute(
                                () -> {
                                    ClientInsightData.set(
                                            payload.insight()
                                    );

                                    if (context.client()
                                            .screen
                                            instanceof TomeScreen
                                            tomeScreen) {

                                        tomeScreen.updateState(
                                                payload.insight(),
                                                payload
                                                        .purchasedMask()
                                        );
                                    }
                                }
                        )
        );

        ClientPlayConnectionEvents.DISCONNECT.register(
                (handler, client) ->
                        ClientInsightData.reset()
        );
    }
}