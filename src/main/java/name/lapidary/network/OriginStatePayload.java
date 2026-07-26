package name.lapidary.network;

import name.lapidary.Lapidary;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OriginStatePayload(
        int originKind,
        int resource,
        int maximum,
        boolean secondaryActive,
        int cameraEntityId
) implements CustomPacketPayload {

    public static final Type<OriginStatePayload> TYPE =
            new Type<>(
                    Lapidary.id(
                            "origin_state"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            OriginStatePayload
            > STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public OriginStatePayload decode(
                        RegistryFriendlyByteBuf buffer
                ) {
                    return new OriginStatePayload(
                            buffer.readVarInt(),
                            buffer.readVarInt(),
                            buffer.readVarInt(),
                            buffer.readBoolean(),
                            buffer.readVarInt()
                    );
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buffer,
                        OriginStatePayload payload
                ) {
                    buffer.writeVarInt(
                            payload.originKind()
                    );

                    buffer.writeVarInt(
                            payload.resource()
                    );

                    buffer.writeVarInt(
                            payload.maximum()
                    );

                    buffer.writeBoolean(
                            payload.secondaryActive()
                    );

                    buffer.writeVarInt(
                            payload.cameraEntityId()
                    );
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
