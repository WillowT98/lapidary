package name.lapidary.client.origin;

public final class ClientOriginState {

    private static int originKind;
    private static int resource;
    private static int maximum;
    private static boolean secondaryActive;

    private ClientOriginState() {
    }

    public static void update(
            int newOriginKind,
            int newResource,
            int newMaximum,
            boolean newSecondaryActive
    ) {
        originKind =
                newOriginKind;

        resource =
                newResource;

        maximum =
                newMaximum;

        secondaryActive =
                newSecondaryActive;
    }

    public static int originKind() {
        return originKind;
    }

    public static int resource() {
        return resource;
    }

    public static int maximum() {
        return maximum;
    }

    public static boolean secondaryActive() {
        return secondaryActive;
    }

    public static void reset() {
        update(
                0,
                0,
                0,
                false
        );
    }
}
