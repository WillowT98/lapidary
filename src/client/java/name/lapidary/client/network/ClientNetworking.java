package name.lapidary.client.network;

import name.lapidary.client.magic.ClientMagicData;
import name.lapidary.client.origin.ClientOriginState;
import name.lapidary.client.progression.ClientInsightData;
import name.lapidary.client.screen.TomeScreen;
import name.lapidary.magic.PlayerMagicData;
import name.lapidary.network.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import name.lapidary.client.screen.SpellRadialScreen;
import name.lapidary.magic.focus.SpellcastingFocusHelper;
import net.minecraft.world.entity.Entity;

public final class ClientNetworking {

    private ClientNetworking() {
    }

    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(
                OriginStatePayload.TYPE,
                (
                        payload,
                        context
                ) -> context.client().execute(
                        () -> {
                            ClientOriginState.update(
                                    payload.originKind(),
                                    payload.resource(),
                                    payload.maximum(),
                                    payload.secondaryActive()
                            );

                            if (context.client().player == null) {
                                return;
                            }

                            if (payload.cameraEntityId() < 0) {
                                context.client().setCameraEntity(
                                        context.client().player
                                );
                                return;
                            }

                            Entity camera =
                                    context.client().level
                                            .getEntity(
                                                    payload.cameraEntityId()
                                            );

                            if (camera != null) {
                                context.client().setCameraEntity(
                                        camera
                                );
                            }
                        }
                )
        );
        ClientOriginState.reset();


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
                OpenSpellRadialPayload.TYPE,
                (payload, context) ->
                        context.client().execute(
                                () -> {
                                    if (context.client().player == null) {
                                        return;
                                    }

                                    /*
                                     * Do not unexpectedly replace an inventory,
                                     * Tome screen, chat screen, or other GUI.
                                     */
                                    if (context.client().screen != null) {
                                        return;
                                    }

                                    if (!SpellcastingFocusHelper
                                            .isHoldingFocus(
                                                    context.client().player
                                            )) {

                                        return;
                                    }

                                    context.client().setScreen(
                                            new SpellRadialScreen()
                                    );
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