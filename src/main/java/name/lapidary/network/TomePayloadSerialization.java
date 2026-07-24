package name.lapidary.network;

import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

final class TomePayloadSerialization {

    private static final int MAX_NODE_COUNT =
            4096;

    static final int MAX_NODE_ID_LENGTH =
            128;

    private TomePayloadSerialization() {
    }

    static List<String> readNodeIds(
            RegistryFriendlyByteBuf buffer
    ) {
        int count =
                buffer.readVarInt();

        if (count < 0
                || count > MAX_NODE_COUNT) {

            throw new IllegalArgumentException(
                    "Invalid Tome node count: "
                            + count
            );
        }

        List<String> result =
                new ArrayList<>(count);

        for (int index = 0;
             index < count;
             index++) {

            result.add(
                    buffer.readUtf(
                            MAX_NODE_ID_LENGTH
                    )
            );
        }

        return List.copyOf(result);
    }

    static void writeNodeIds(
            RegistryFriendlyByteBuf buffer,
            List<String> nodeIds
    ) {
        if (nodeIds.size()
                > MAX_NODE_COUNT) {

            throw new IllegalArgumentException(
                    "Too many Tome nodes: "
                            + nodeIds.size()
            );
        }

        buffer.writeVarInt(
                nodeIds.size()
        );

        for (String nodeId : nodeIds) {
            buffer.writeUtf(
                    nodeId,
                    MAX_NODE_ID_LENGTH
            );
        }
    }
}