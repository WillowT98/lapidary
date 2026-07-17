package name.lapidary.client.progression;

import name.lapidary.progression.LapidaryInsight;

public final class ClientInsightData {

    private static int insight = 0;

    private ClientInsightData() {
    }

    public static int get() {
        return insight;
    }

    public static void set(int amount) {
        insight = Math.max(
                0,
                Math.min(
                        LapidaryInsight.MAX_INSIGHT,
                        amount
                )
        );
    }

    public static void reset() {
        insight = 0;
    }
}