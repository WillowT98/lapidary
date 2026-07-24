package name.lapidary.network;

import name.lapidary.Lapidary;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TomePurchasePayload(
        BlockPos tablePosition,
        int nodeIndex
) implements CustomPacketPayload {

    public static final Type<TomePurchasePayload> TYPE =
            new Type<>(
                    ResourceLocation
                            .fromNamespaceAndPath(
                                    Lapidary.MOD_ID,
                                    "purchase_tome_node"
                            )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            TomePurchasePayload
            > STREAM_CODEC =
            new StreamCodec<>() {

                @Override
                public TomePurchasePayload decode(
                        RegistryFriendlyByteBuf buffer
                ) {
                    return new TomePurchasePayload(
                            buffer.readBlockPos(),
                            buffer.readVarInt()
                    );
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buffer,
                        TomePurchasePayload payload
                ) {
                    buffer.writeBlockPos(
                            payload.tablePosition()
                    );

                    buffer.writeVarInt(
                            payload.nodeIndex()
                    );
                }
            };

    @Override
    public Type<? extends CustomPacketPayload>
    type() {
        return TYPE;
    }
}