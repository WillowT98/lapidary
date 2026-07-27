package name.lapidary.network;

import name.lapidary.Lapidary;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Synchronizes one position-based hardened-block entry to a client. */
public record HardenedBlockSyncPayload(
        ResourceLocation dimension,
        long packedPosition,
        boolean hardened,
        boolean clearDimension
) implements CustomPacketPayload {
    public static final Type<HardenedBlockSyncPayload> TYPE =
            new Type<>(Lapidary.id("sync_hardened_block"));

    public static final StreamCodec<RegistryFriendlyByteBuf,
            HardenedBlockSyncPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public HardenedBlockSyncPayload decode(
                        RegistryFriendlyByteBuf buffer
                ) {
                    return new HardenedBlockSyncPayload(
                            buffer.readResourceLocation(),
                            buffer.readLong(),
                            buffer.readBoolean(),
                            buffer.readBoolean()
                    );
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buffer,
                        HardenedBlockSyncPayload payload
                ) {
                    buffer.writeResourceLocation(payload.dimension());
                    buffer.writeLong(payload.packedPosition());
                    buffer.writeBoolean(payload.hardened());
                    buffer.writeBoolean(payload.clearDimension());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
