package name.lapidary.network;

import name.lapidary.Lapidary;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TomeStatePayload(
        int insight,
        long purchasedMask
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
                            buffer.readLong()
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

                    buffer.writeLong(
                            payload.purchasedMask()
                    );
                }
            };

    @Override
    public Type<? extends CustomPacketPayload>
    type() {
        return TYPE;
    }
}