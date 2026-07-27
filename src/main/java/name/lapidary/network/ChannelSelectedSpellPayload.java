package name.lapidary.network;

import name.lapidary.Lapidary;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Sent periodically while the player holds the staff attack button. */
public record ChannelSelectedSpellPayload() implements CustomPacketPayload {
    public static final ChannelSelectedSpellPayload INSTANCE =
            new ChannelSelectedSpellPayload();

    public static final Type<ChannelSelectedSpellPayload> TYPE =
            new Type<>(Lapidary.id("channel_selected_spell"));

    public static final StreamCodec<RegistryFriendlyByteBuf,
            ChannelSelectedSpellPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public ChannelSelectedSpellPayload decode(
                        RegistryFriendlyByteBuf buffer
                ) {
                    return INSTANCE;
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buffer,
                        ChannelSelectedSpellPayload payload
                ) {
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
