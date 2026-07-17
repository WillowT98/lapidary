package name.lapidary.network;

import name.lapidary.Lapidary;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record InsightSyncPayload(
        int insight
) implements CustomPacketPayload {

    /*
     * Identifies this type of packet as:
     *
     * lapidary:sync_insight
     */
    public static final Type<InsightSyncPayload> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(
                            Lapidary.MOD_ID,
                            "sync_insight"
                    )
            );

    /*
     * Defines how the packet's one integer is written and read.
     */
    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            InsightSyncPayload
            > STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    InsightSyncPayload::insight,
                    InsightSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}