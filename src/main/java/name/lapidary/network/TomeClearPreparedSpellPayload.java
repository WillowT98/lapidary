package name.lapidary.network;

import name.lapidary.Lapidary;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TomeClearPreparedSpellPayload(
        BlockPos tablePosition,
        int slot
) implements CustomPacketPayload {

    public static final Type<
            TomeClearPreparedSpellPayload
            > TYPE =
            new Type<>(
                    Lapidary.id(
                            "tome_clear_prepared_spell"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            TomeClearPreparedSpellPayload
            > STREAM_CODEC =
            new StreamCodec<>() {

                @Override
                public TomeClearPreparedSpellPayload decode(
                        RegistryFriendlyByteBuf buffer
                ) {
                    return new TomeClearPreparedSpellPayload(
                            buffer.readBlockPos(),
                            buffer.readVarInt()
                    );
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buffer,
                        TomeClearPreparedSpellPayload payload
                ) {
                    buffer.writeBlockPos(
                            payload.tablePosition()
                    );

                    buffer.writeVarInt(
                            payload.slot()
                    );
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}