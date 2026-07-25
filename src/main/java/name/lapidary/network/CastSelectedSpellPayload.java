package name.lapidary.network;

import name.lapidary.Lapidary;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CastSelectedSpellPayload()
        implements CustomPacketPayload {

    public static final CastSelectedSpellPayload
            INSTANCE =
            new CastSelectedSpellPayload();

    public static final Type<
            CastSelectedSpellPayload
            > TYPE =
            new Type<>(
                    Lapidary.id(
                            "cast_selected_spell"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            CastSelectedSpellPayload
            > STREAM_CODEC =
            new StreamCodec<>() {

                @Override
                public CastSelectedSpellPayload decode(
                        RegistryFriendlyByteBuf buffer
                ) {
                    return INSTANCE;
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buffer,
                        CastSelectedSpellPayload payload
                ) {
                    /*
                     * No data is sent. The server resolves the held
                     * focus and the player's selected spell itself.
                     */
                }
            };

    @Override
    public Type<? extends CustomPacketPayload>
    type() {
        return TYPE;
    }
}