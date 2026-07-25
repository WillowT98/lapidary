package name.lapidary.network;

import name.lapidary.Lapidary;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TomePrepareSpellPayload(
        BlockPos tablePosition,
        int slot,
        ResourceLocation spellId
) implements CustomPacketPayload {

    public static final Type<TomePrepareSpellPayload> TYPE =
            new Type<>(
                    Lapidary.id(
                            "tome_prepare_spell"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            TomePrepareSpellPayload
            > STREAM_CODEC =
            new StreamCodec<>() {

                @Override
                public TomePrepareSpellPayload decode(
                        RegistryFriendlyByteBuf buffer
                ) {
                    return new TomePrepareSpellPayload(
                            buffer.readBlockPos(),
                            buffer.readVarInt(),
                            buffer.readResourceLocation()
                    );
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buffer,
                        TomePrepareSpellPayload payload
                ) {
                    buffer.writeBlockPos(
                            payload.tablePosition()
                    );

                    buffer.writeVarInt(
                            payload.slot()
                    );

                    buffer.writeResourceLocation(
                            payload.spellId()
                    );
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}