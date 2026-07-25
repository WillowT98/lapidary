package name.lapidary.network;

import name.lapidary.Lapidary;
import name.lapidary.magic.PlayerMagicData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

public record MagicStatePayload(
        List<String> knownSpells,
        List<String> knownRituals,
        List<String> preparedSpells,
        int selectedSlot
) implements CustomPacketPayload {

    public static final Type<MagicStatePayload> TYPE =
            new Type<>(
                    Lapidary.id("sync_magic_state")
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            MagicStatePayload
            > STREAM_CODEC =
            new StreamCodec<>() {

                @Override
                public MagicStatePayload decode(
                        RegistryFriendlyByteBuf buffer
                ) {
                    return new MagicStatePayload(
                            readStrings(buffer),
                            readStrings(buffer),
                            readStrings(buffer),
                            buffer.readVarInt()
                    );
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buffer,
                        MagicStatePayload payload
                ) {
                    writeStrings(
                            buffer,
                            payload.knownSpells()
                    );

                    writeStrings(
                            buffer,
                            payload.knownRituals()
                    );

                    writeStrings(
                            buffer,
                            payload.preparedSpells()
                    );

                    buffer.writeVarInt(
                            payload.selectedSlot()
                    );
                }
            };

    public MagicStatePayload(PlayerMagicData data) {
        this(
                data.knownSpells(),
                data.knownRituals(),
                data.preparedSpells(),
                data.selectedSlot()
        );
    }

    public MagicStatePayload {
        knownSpells = List.copyOf(knownSpells);
        knownRituals = List.copyOf(knownRituals);
        preparedSpells = List.copyOf(preparedSpells);
    }

    public PlayerMagicData toData() {
        return new PlayerMagicData(
                knownSpells,
                knownRituals,
                preparedSpells,
                selectedSlot
        );
    }

    @Override
    public Type<MagicStatePayload> type() {
        return TYPE;
    }

    private static List<String> readStrings(
            RegistryFriendlyByteBuf buffer
    ) {
        int count = buffer.readVarInt();

        /*
         * These collections should always be tiny. Reject obviously
         * corrupt data rather than trying to allocate an enormous list.
         */
        if (count < 0 || count > 1024) {
            throw new IllegalArgumentException(
                    "Invalid magic-state list size: " + count
            );
        }

        List<String> values =
                new ArrayList<>(count);

        for (int index = 0; index < count; index++) {
            values.add(buffer.readUtf());
        }

        return List.copyOf(values);
    }

    private static void writeStrings(
            RegistryFriendlyByteBuf buffer,
            List<String> values
    ) {
        buffer.writeVarInt(values.size());

        for (String value : values) {
            buffer.writeUtf(value);
        }
    }
}