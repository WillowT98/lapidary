package name.lapidary.network;

import name.lapidary.Lapidary;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Sends caster-only ore positions to the client for temporary highlighting. */
public record RevealOresPayload(
        long[] packedPositions,
        int durationTicks
) implements CustomPacketPayload {
    private static final int MAX_POSITIONS = 16_384;

    public static final Type<RevealOresPayload> TYPE =
            new Type<>(Lapidary.id("reveal_ores"));

    public static final StreamCodec<RegistryFriendlyByteBuf,
            RevealOresPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public RevealOresPayload decode(
                        RegistryFriendlyByteBuf buffer
                ) {
                    int count = buffer.readVarInt();
                    if (count < 0 || count > MAX_POSITIONS) {
                        throw new IllegalArgumentException(
                                "Invalid revealed ore count: " + count
                        );
                    }

                    long[] positions = new long[count];
                    for (int index = 0; index < count; index++) {
                        positions[index] = buffer.readLong();
                    }

                    return new RevealOresPayload(
                            positions,
                            buffer.readVarInt()
                    );
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buffer,
                        RevealOresPayload payload
                ) {
                    long[] positions = payload.packedPositions();
                    if (positions.length > MAX_POSITIONS) {
                        throw new IllegalArgumentException(
                                "Too many revealed ore positions: "
                                        + positions.length
                        );
                    }

                    buffer.writeVarInt(positions.length);
                    for (long position : positions) {
                        buffer.writeLong(position);
                    }
                    buffer.writeVarInt(
                            Math.max(0, payload.durationTicks())
                    );
                }
            };

    public RevealOresPayload {
        packedPositions = packedPositions.clone();
        durationTicks = Math.max(0, durationTicks);
    }

    @Override
    public long[] packedPositions() {
        return packedPositions.clone();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
