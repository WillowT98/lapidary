package name.lapidary.network;

import name.lapidary.Lapidary;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TomeOpenPayload(
        BlockPos tablePosition,
        int insight,
        long purchasedMask
) implements CustomPacketPayload {

    public static final Type<TomeOpenPayload> TYPE =
            new Type<>(
                    ResourceLocation
                            .fromNamespaceAndPath(
                                    Lapidary.MOD_ID,
                                    "open_tome"
                            )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            TomeOpenPayload
            > STREAM_CODEC =
            new StreamCodec<>() {

                @Override
                public TomeOpenPayload decode(
                        RegistryFriendlyByteBuf buffer
                ) {
                    return new TomeOpenPayload(
                            buffer.readBlockPos(),
                            buffer.readVarInt(),
                            buffer.readLong()
                    );
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buffer,
                        TomeOpenPayload payload
                ) {
                    buffer.writeBlockPos(
                            payload.tablePosition()
                    );

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