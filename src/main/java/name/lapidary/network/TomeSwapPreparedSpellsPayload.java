package name.lapidary.network;

import name.lapidary.Lapidary;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TomeSwapPreparedSpellsPayload(
        BlockPos tablePosition,
        int firstSlot,
        int secondSlot
) implements CustomPacketPayload {

    public static final Type<
            TomeSwapPreparedSpellsPayload
            > TYPE =
            new Type<>(
                    Lapidary.id(
                            "tome_swap_prepared_spells"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            TomeSwapPreparedSpellsPayload
            > STREAM_CODEC =
            new StreamCodec<>() {

                @Override
                public TomeSwapPreparedSpellsPayload decode(
                        RegistryFriendlyByteBuf buffer
                ) {
                    return new TomeSwapPreparedSpellsPayload(
                            buffer.readBlockPos(),
                            buffer.readVarInt(),
                            buffer.readVarInt()
                    );
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buffer,
                        TomeSwapPreparedSpellsPayload payload
                ) {
                    buffer.writeBlockPos(
                            payload.tablePosition()
                    );

                    buffer.writeVarInt(
                            payload.firstSlot()
                    );

                    buffer.writeVarInt(
                            payload.secondSlot()
                    );
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}