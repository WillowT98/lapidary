package name.lapidary.progression.tome;

import java.util.List;

public final class TomeTree {

    /*
     * The numeric indices are part of the player's saved data.
     *
     * Once a node reaches a released version, do not assign its index
     * to a different node.
     */
    public static final TomeNode ROOT =
            new TomeNode(
                    0,
                    "root",
                    "root",
                    0,
                    -90,
                    0,
                    List.of(),
                    true
            );

    public static final TomeNode BRANCH_A_1 =
            new TomeNode(
                    1,
                    "branch_a_1",
                    "branch_a",
                    -70,
                    -30,
                    5,
                    List.of(ROOT.index()),
                    false
            );

    public static final TomeNode BRANCH_A_2 =
            new TomeNode(
                    2,
                    "branch_a_2",
                    "branch_a",
                    -125,
                    25,
                    10,
                    List.of(BRANCH_A_1.index()),
                    false
            );

    public static final TomeNode BRANCH_A_3 =
            new TomeNode(
                    3,
                    "branch_a_3",
                    "branch_a",
                    -175,
                    80,
                    20,
                    List.of(BRANCH_A_2.index()),
                    false
            );

    public static final TomeNode BRANCH_B_1 =
            new TomeNode(
                    4,
                    "branch_b_1",
                    "branch_b",
                    70,
                    -30,
                    5,
                    List.of(ROOT.index()),
                    false
            );

    public static final TomeNode BRANCH_B_2 =
            new TomeNode(
                    5,
                    "branch_b_2",
                    "branch_b",
                    125,
                    25,
                    10,
                    List.of(BRANCH_B_1.index()),
                    false
            );

    public static final TomeNode BRANCH_B_3 =
            new TomeNode(
                    6,
                    "branch_b_3",
                    "branch_b",
                    175,
                    80,
                    20,
                    List.of(BRANCH_B_2.index()),
                    false
            );

    public static final List<TomeNode> NODES =
            List.of(
                    ROOT,
                    BRANCH_A_1,
                    BRANCH_A_2,
                    BRANCH_A_3,
                    BRANCH_B_1,
                    BRANCH_B_2,
                    BRANCH_B_3
            );

    private TomeTree() {
    }

    public static TomeNode getByIndex(
            int index
    ) {
        for (TomeNode node : NODES) {
            if (node.index() == index) {
                return node;
            }
        }

        return null;
    }

    public static boolean isOwned(
            long purchasedMask,
            TomeNode node
    ) {
        /*
         * The central root is always treated as unlocked.
         */
        if (node.root()) {
            return true;
        }

        return (purchasedMask & bitFor(node)) != 0L;
    }

    public static boolean prerequisitesMet(
            long purchasedMask,
            TomeNode node
    ) {
        for (int prerequisiteIndex :
                node.prerequisites()) {

            TomeNode prerequisite =
                    getByIndex(prerequisiteIndex);

            if (prerequisite == null
                    || !isOwned(
                    purchasedMask,
                    prerequisite
            )) {

                return false;
            }
        }

        return true;
    }

    public static long addNode(
            long purchasedMask,
            TomeNode node
    ) {
        if (node.root()) {
            return purchasedMask;
        }

        return purchasedMask | bitFor(node);
    }

    private static long bitFor(
            TomeNode node
    ) {
        return 1L << node.index();
    }
}