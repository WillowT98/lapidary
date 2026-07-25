package name.lapidary.network;

import name.lapidary.Lapidary;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SelectPreparedSpellPayload(
        int slot
) implements CustomPacketPayload {

    public static final Type<
            SelectPreparedSpellPayload
            > TYPE =
            new Type<>(
                    Lapidary.id(
                            "select_prepared_spell"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            SelectPreparedSpellPayload
            > STREAM_CODEC =
            new StreamCodec<>() {

                @Override
                public SelectPreparedSpellPayload decode(
                        RegistryFriendlyByteBuf buffer
                ) {
                    return new SelectPreparedSpellPayload(
                            buffer.readVarInt()
                    );
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buffer,
                        SelectPreparedSpellPayload payload
                ) {
                    buffer.writeVarInt(
                            payload.slot()
                    );
                }
            };

    @Override
    public Type<SelectPreparedSpellPayload> type() {
        return TYPE;
    }
}