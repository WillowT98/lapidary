package name.lapidary.client.network;

import name.lapidary.client.magic.ClientMagicData;
import name.lapidary.client.progression.ClientInsightData;
import name.lapidary.client.screen.TomeScreen;
import name.lapidary.magic.PlayerMagicData;
import name.lapidary.network.InsightSyncPayload;
import name.lapidary.network.MagicStatePayload;
import name.lapidary.network.TomeOpenPayload;
import name.lapidary.network.TomeStatePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ClientNetworking {

    private ClientNetworking() {
    }

    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(
                InsightSyncPayload.TYPE,
                (payload, context) ->
                        ClientInsightData.set(
                                payload.insight()
                        )
        );

        ClientPlayNetworking.registerGlobalReceiver(
                MagicStatePayload.TYPE,
                (payload, context) ->
                        context.client().execute(
                                () -> {
                                    PlayerMagicData data =
                                            payload.toData();

                                    ClientMagicData.set(
                                            data
                                    );

                                    if (context.client().screen
                                            instanceof TomeScreen
                                            tomeScreen) {

                                        tomeScreen.updateMagicData(
                                                data
                                        );
                                    }
                                }
                        )
        );

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
                                                            payload.tablePosition(),
                                                            payload.insight(),
                                                            payload.purchasedNodeIds()
                                                    )
                                            );
                                }
                        )
        );

        ClientPlayNetworking.registerGlobalReceiver(
                TomeStatePayload.TYPE,
                (payload, context) ->
                        context.client().execute(
                                () -> {
                                    ClientInsightData.set(
                                            payload.insight()
                                    );

                                    if (context.client().screen
                                            instanceof TomeScreen
                                            tomeScreen) {

                                        tomeScreen.updateState(
                                                payload.insight(),
                                                payload.purchasedNodeIds()
                                        );
                                    }
                                }
                        )
        );

        ClientPlayConnectionEvents.DISCONNECT.register(
                (handler, client) -> {
                    ClientInsightData.reset();
                    ClientMagicData.reset();
                }
        );
    }
}