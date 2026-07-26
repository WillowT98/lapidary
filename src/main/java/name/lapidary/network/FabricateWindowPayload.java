package name.lapidary.network;

import name.lapidary.Lapidary;
import name.lapidary.window.WindowDesign;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record FabricateWindowPayload(
        int containerId,
        int blockWidth,
        int blockHeight,
        byte[] pixels
) implements CustomPacketPayload {

    public static final Type<FabricateWindowPayload> TYPE =
            new Type<>(
                    Lapidary.id(
                            "fabricate_window"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            FabricateWindowPayload
            > STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public FabricateWindowPayload decode(
                        RegistryFriendlyByteBuf buffer
                ) {
                    return FabricateWindowPayload.read(
                            buffer
                    );
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buffer,
                        FabricateWindowPayload payload
                ) {
                    payload.write(
                            buffer
                    );
                }
            };

    public FabricateWindowPayload {
        pixels =
                pixels.clone();
    }

    @Override
    public byte[] pixels() {
        return pixels.clone();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static FabricateWindowPayload read(
            RegistryFriendlyByteBuf buffer
    ) {
        return new FabricateWindowPayload(
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readByteArray(
                        WindowDesign.MAX_PIXEL_COUNT
                )
        );
    }

    private void write(
            RegistryFriendlyByteBuf buffer
    ) {
        buffer.writeVarInt(
                containerId
        );

        buffer.writeVarInt(
                blockWidth
        );

        buffer.writeVarInt(
                blockHeight
        );

        buffer.writeByteArray(
                pixels
        );
    }
}
