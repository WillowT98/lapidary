package name.lapidary.network;

import name.lapidary.Lapidary;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OpenSpellRadialPayload()
        implements CustomPacketPayload {

    public static final OpenSpellRadialPayload INSTANCE =
            new OpenSpellRadialPayload();

    public static final Type<OpenSpellRadialPayload> TYPE =
            new Type<>(
                    Lapidary.id(
                            "open_spell_radial"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            OpenSpellRadialPayload
            > STREAM_CODEC =
            new StreamCodec<>() {

                @Override
                public OpenSpellRadialPayload decode(
                        RegistryFriendlyByteBuf buffer
                ) {
                    return INSTANCE;
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buffer,
                        OpenSpellRadialPayload payload
                ) {
                    /*
                     * This payload contains no data.
                     */
                }
            };

    @Override
    public Type<OpenSpellRadialPayload> type() {
        return TYPE;
    }
}