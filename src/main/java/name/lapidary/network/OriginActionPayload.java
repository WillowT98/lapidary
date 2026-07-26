package name.lapidary.network;

import name.lapidary.Lapidary;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OriginActionPayload(
        int action
) implements CustomPacketPayload {

    public static final int ACTIVE =
            0;

    public static final int MAGIC =
            1;

    public static final int VOCALIZE =
            2;

    public static final int FLAP =
            3;

    public static final Type<OriginActionPayload> TYPE =
            new Type<>(
                    Lapidary.id(
                            "origin_action"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            OriginActionPayload
            > STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public OriginActionPayload decode(
                        RegistryFriendlyByteBuf buffer
                ) {
                    return new OriginActionPayload(
                            buffer.readVarInt()
                    );
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buffer,
                        OriginActionPayload payload
                ) {
                    buffer.writeVarInt(
                            payload.action()
                    );
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
