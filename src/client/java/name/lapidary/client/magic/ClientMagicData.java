package name.lapidary.client.magic;

import name.lapidary.magic.PlayerMagicData;

public final class ClientMagicData {

    private static PlayerMagicData data =
            PlayerMagicData.empty();

    private ClientMagicData() {
    }

    public static PlayerMagicData get() {
        return data;
    }

    public static void set(PlayerMagicData newData) {
        data = newData;
    }

    public static void reset() {
        data = PlayerMagicData.empty();
    }
}