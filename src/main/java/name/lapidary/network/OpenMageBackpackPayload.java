package name.lapidary.network;

import name.lapidary.Lapidary;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenMageBackpackPayload()
        implements CustomPacketPayload {

    public static final OpenMageBackpackPayload
            INSTANCE =
            new OpenMageBackpackPayload();

    public static final Type<
            OpenMageBackpackPayload
            > TYPE =
            new Type<>(
                    ResourceLocation
                            .fromNamespaceAndPath(
                                    Lapidary.MOD_ID,
                                    "open_mage_backpack"
                            )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            OpenMageBackpackPayload
            > STREAM_CODEC =
            new StreamCodec<>() {

                @Override
                public OpenMageBackpackPayload decode(
                        RegistryFriendlyByteBuf buffer
                ) {
                    return INSTANCE;
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buffer,
                        OpenMageBackpackPayload payload
                ) {
                    /*
                     * No data is needed. The server determines which
                     * backpack the player currently has equipped.
                     */
                }
            };

    @Override
    public Type<? extends CustomPacketPayload>
    type() {
        return TYPE;
    }
}