package name.lapidary.network;

import name.lapidary.Lapidary;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record TomeStatePayload(
        int insight,
        List<String> purchasedNodeIds
) implements CustomPacketPayload {

    public static final Type<TomeStatePayload> TYPE =
            new Type<>(
                    ResourceLocation
                            .fromNamespaceAndPath(
                                    Lapidary.MOD_ID,
                                    "sync_tome_state"
                            )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            TomeStatePayload
            > STREAM_CODEC =
            new StreamCodec<>() {

                @Override
                public TomeStatePayload decode(
                        RegistryFriendlyByteBuf buffer
                ) {
                    return new TomeStatePayload(
                            buffer.readVarInt(),
                            TomePayloadSerialization
                                    .readNodeIds(buffer)
                    );
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buffer,
                        TomeStatePayload payload
                ) {
                    buffer.writeVarInt(
                            payload.insight()
                    );

                    TomePayloadSerialization
                            .writeNodeIds(
                                    buffer,
                                    payload.purchasedNodeIds()
                            );
                }
            };

    public TomeStatePayload {
        purchasedNodeIds =
                List.copyOf(
                        purchasedNodeIds
                );
    }

    @Override
    public Type<? extends CustomPacketPayload>
    type() {
        return TYPE;
    }
}