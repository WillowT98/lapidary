package name.lapidary.network;

import name.lapidary.Lapidary;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SelectWindowBackgroundPayload(
        int containerId,
        int blockRegistryId
) implements CustomPacketPayload {

    public static final Type<SelectWindowBackgroundPayload> TYPE =
            new Type<>(
                    Lapidary.id(
                            "select_window_background"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            SelectWindowBackgroundPayload
            > STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public SelectWindowBackgroundPayload decode(
                        RegistryFriendlyByteBuf buffer
                ) {
                    return new SelectWindowBackgroundPayload(
                            buffer.readVarInt(),
                            buffer.readVarInt()
                    );
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buffer,
                        SelectWindowBackgroundPayload payload
                ) {
                    buffer.writeVarInt(
                            payload.containerId()
                    );

                    buffer.writeVarInt(
                            payload.blockRegistryId()
                    );
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
