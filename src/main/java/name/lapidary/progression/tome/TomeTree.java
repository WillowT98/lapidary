package name.lapidary.progression.tome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TomeTree {

    public static final String SCHOOLS_PAGE_ID =
            "schools";

    /*
     * -----------------------------------------------------------------
     * Schools overview
     * -----------------------------------------------------------------
     */

    public static final TomeNode SCHOOLS_ROOT =
            root(
                    "schools/root",
                    "schools",
                    0,
                    0
            );

    public static final TomeNode UNLOCK_SUMMONING =
            node(
                    "schools/summoning",
                    "schools",
                    -110,
                    -55,
                    10,
                    SCHOOLS_ROOT.id()
            );

    public static final TomeNode UNLOCK_COMMAND =
            node(
                    "schools/command",
                    "schools",
                    -55,
                    -75,
                    10,
                    SCHOOLS_ROOT.id()
            );

    public static final TomeNode UNLOCK_NATURE =
            node(
                    "schools/nature",
                    "schools",
                    0,
                    -80,
                    10,
                    SCHOOLS_ROOT.id()
            );

    public static final TomeNode UNLOCK_TRANSMUTATION =
            node(
                    "schools/transmutation",
                    "schools",
                    55,
                    -75,
                    10,
                    SCHOOLS_ROOT.id()
            );

    public static final TomeNode UNLOCK_WARDING =
            node(
                    "schools/warding",
                    "schools",
                    110,
                    -55,
                    10,
                    SCHOOLS_ROOT.id()
            );

    public static final TomeNode UNLOCK_DIVINATION =
            node(
                    "schools/divination",
                    "schools",
                    -110,
                    55,
                    10,
                    SCHOOLS_ROOT.id()
            );

    public static final TomeNode UNLOCK_ILLUSION =
            node(
                    "schools/illusion",
                    "schools",
                    -55,
                    75,
                    10,
                    SCHOOLS_ROOT.id()
            );

    public static final TomeNode UNLOCK_PASSAGE =
            node(
                    "schools/passage",
                    "schools",
                    0,
                    80,
                    10,
                    SCHOOLS_ROOT.id()
            );

    public static final TomeNode UNLOCK_GRIEFING =
            node(
                    "schools/griefing",
                    "schools",
                    55,
                    75,
                    10,
                    SCHOOLS_ROOT.id()
            );

    public static final TomeNode UNLOCK_NECROMANCY =
            node(
                    "schools/necromancy",
                    "schools",
                    110,
                    55,
                    10,
                    SCHOOLS_ROOT.id()
            );

    public static final TomePage SCHOOLS_PAGE =
            page(
                    "schools",
                    null,
                    SCHOOLS_ROOT,
                    UNLOCK_SUMMONING,
                    UNLOCK_COMMAND,
                    UNLOCK_NATURE,
                    UNLOCK_TRANSMUTATION,
                    UNLOCK_WARDING,
                    UNLOCK_DIVINATION,
                    UNLOCK_ILLUSION,
                    UNLOCK_PASSAGE,
                    UNLOCK_GRIEFING,
                    UNLOCK_NECROMANCY
            );

    /*
     * -----------------------------------------------------------------
     * Summoning
     *
     * A fork that later converges.
     * -----------------------------------------------------------------
     */

    public static final TomeNode SUMMONING_ROOT =
            root(
                    "summoning/root",
                    "summoning",
                    0,
                    -70
            );

    public static final TomeNode SUMMONING_LESSER =
            node(
                    "summoning/lesser_calling",
                    "summoning",
                    -80,
                    0,
                    5,
                    SUMMONING_ROOT.id()
            );

    public static final TomeNode SUMMONING_GREATER =
            node(
                    "summoning/greater_calling",
                    "summoning",
                    80,
                    0,
                    5,
                    SUMMONING_ROOT.id()
            );

    public static final TomeNode SUMMONING_CONVERGENCE =
            node(
                    "summoning/convergence",
                    "summoning",
                    0,
                    70,
                    20,
                    SUMMONING_LESSER.id(),
                    SUMMONING_GREATER.id()
            );

    public static final TomePage SUMMONING_PAGE =
            page(
                    "summoning",
                    UNLOCK_SUMMONING.id(),
                    SUMMONING_ROOT,
                    SUMMONING_LESSER,
                    SUMMONING_GREATER,
                    SUMMONING_CONVERGENCE
            );

    /*
     * -----------------------------------------------------------------
     * Command
     *
     * A rising diagonal chain.
     * -----------------------------------------------------------------
     */

    public static final TomeNode COMMAND_ROOT =
            root(
                    "command/root",
                    "command",
                    -105,
                    -60
            );

    public static final TomeNode COMMAND_FIRST =
            node(
                    "command/compel_one",
                    "command",
                    -35,
                    -20,
                    5,
                    COMMAND_ROOT.id()
            );

    public static final TomeNode COMMAND_SECOND =
            node(
                    "command/compel_two",
                    "command",
                    35,
                    20,
                    10,
                    COMMAND_FIRST.id()
            );

    public static final TomeNode COMMAND_THIRD =
            node(
                    "command/compel_three",
                    "command",
                    105,
                    60,
                    20,
                    COMMAND_SECOND.id()
            );

    public static final TomePage COMMAND_PAGE =
            page(
                    "command",
                    UNLOCK_COMMAND.id(),
                    COMMAND_ROOT,
                    COMMAND_FIRST,
                    COMMAND_SECOND,
                    COMMAND_THIRD
            );

    /*
     * -----------------------------------------------------------------
     * Nature
     *
     * Three independent branches.
     * -----------------------------------------------------------------
     */

    public static final TomeNode NATURE_ROOT =
            root(
                    "nature/root",
                    "nature",
                    0,
                    -70
            );

    public static final TomeNode NATURE_VERDANCY =
            node(
                    "nature/verdancy",
                    "nature",
                    -90,
                    45,
                    5,
                    NATURE_ROOT.id()
            );

    public static final TomeNode NATURE_HUSBANDRY =
            node(
                    "nature/husbandry",
                    "nature",
                    0,
                    70,
                    10,
                    NATURE_ROOT.id()
            );

    public static final TomeNode NATURE_COMMUNION =
            node(
                    "nature/wild_communion",
                    "nature",
                    90,
                    45,
                    15,
                    NATURE_ROOT.id()
            );

    public static final TomePage NATURE_PAGE =
            page(
                    "nature",
                    UNLOCK_NATURE.id(),
                    NATURE_ROOT,
                    NATURE_VERDANCY,
                    NATURE_HUSBANDRY,
                    NATURE_COMMUNION
            );

    /*
     * -----------------------------------------------------------------
     * Transmutation
     *
     * A diamond requiring two studies before convergence.
     * -----------------------------------------------------------------
     */

    public static final TomeNode TRANSMUTATION_ROOT =
            root(
                    "transmutation/root",
                    "transmutation",
                    0,
                    -70
            );

    public static final TomeNode TRANSMUTATION_MATERIAL =
            node(
                    "transmutation/material_study",
                    "transmutation",
                    -75,
                    -5,
                    5,
                    TRANSMUTATION_ROOT.id()
            );

    public static final TomeNode TRANSMUTATION_LIVING =
            node(
                    "transmutation/living_study",
                    "transmutation",
                    75,
                    -5,
                    5,
                    TRANSMUTATION_ROOT.id()
            );

    public static final TomeNode TRANSMUTATION_EXCHANGE =
            node(
                    "transmutation/perfect_exchange",
                    "transmutation",
                    0,
                    70,
                    20,
                    TRANSMUTATION_MATERIAL.id(),
                    TRANSMUTATION_LIVING.id()
            );

    public static final TomePage TRANSMUTATION_PAGE =
            page(
                    "transmutation",
                    UNLOCK_TRANSMUTATION.id(),
                    TRANSMUTATION_ROOT,
                    TRANSMUTATION_MATERIAL,
                    TRANSMUTATION_LIVING,
                    TRANSMUTATION_EXCHANGE
            );

    /*
     * -----------------------------------------------------------------
     * Warding
     *
     * A simple vertical progression.
     * -----------------------------------------------------------------
     */

    public static final TomeNode WARDING_ROOT =
            root(
                    "warding/root",
                    "warding",
                    0,
                    -75
            );

    public static final TomeNode WARDING_MINOR =
            node(
                    "warding/minor_ward",
                    "warding",
                    0,
                    -20,
                    5,
                    WARDING_ROOT.id()
            );

    public static final TomeNode WARDING_FORTIFIED =
            node(
                    "warding/fortified_ward",
                    "warding",
                    0,
                    30,
                    10,
                    WARDING_MINOR.id()
            );

    public static final TomeNode WARDING_GREATER =
            node(
                    "warding/greater_ward",
                    "warding",
                    0,
                    75,
                    20,
                    WARDING_FORTIFIED.id()
            );

    public static final TomePage WARDING_PAGE =
            page(
                    "warding",
                    UNLOCK_WARDING.id(),
                    WARDING_ROOT,
                    WARDING_MINOR,
                    WARDING_FORTIFIED,
                    WARDING_GREATER
            );

    /*
     * -----------------------------------------------------------------
     * Divination
     *
     * A fan of separate forms of perception.
     * -----------------------------------------------------------------
     */

    public static final TomeNode DIVINATION_ROOT =
            root(
                    "divination/root",
                    "divination",
                    0,
                    -70
            );

    public static final TomeNode DIVINATION_SECOND =
            node(
                    "divination/second_sight",
                    "divination",
                    -90,
                    45,
                    5,
                    DIVINATION_ROOT.id()
            );

    public static final TomeNode DIVINATION_FAR =
            node(
                    "divination/far_sight",
                    "divination",
                    0,
                    70,
                    10,
                    DIVINATION_ROOT.id()
            );

    public static final TomeNode DIVINATION_PATTERN =
            node(
                    "divination/pattern_sight",
                    "divination",
                    90,
                    45,
                    15,
                    DIVINATION_ROOT.id()
            );

    public static final TomePage DIVINATION_PAGE =
            page(
                    "divination",
                    UNLOCK_DIVINATION.id(),
                    DIVINATION_ROOT,
                    DIVINATION_SECOND,
                    DIVINATION_FAR,
                    DIVINATION_PATTERN
            );

    /*
     * -----------------------------------------------------------------
     * Illusion
     *
     * A zigzag path.
     * -----------------------------------------------------------------
     */

    public static final TomeNode ILLUSION_ROOT =
            root(
                    "illusion/root",
                    "illusion",
                    -100,
                    -65
            );

    public static final TomeNode ILLUSION_GLAMOUR =
            node(
                    "illusion/glamour",
                    "illusion",
                    -35,
                    -20,
                    5,
                    ILLUSION_ROOT.id()
            );

    public static final TomeNode ILLUSION_PHANTASM =
            node(
                    "illusion/phantasm",
                    "illusion",
                    45,
                    -55,
                    10,
                    ILLUSION_GLAMOUR.id()
            );

    public static final TomeNode ILLUSION_MISDIRECTION =
            node(
                    "illusion/misdirection",
                    "illusion",
                    100,
                    15,
                    15,
                    ILLUSION_PHANTASM.id()
            );

    public static final TomeNode ILLUSION_MIRAGE =
            node(
                    "illusion/living_mirage",
                    "illusion",
                    20,
                    70,
                    20,
                    ILLUSION_MISDIRECTION.id()
            );

    public static final TomePage ILLUSION_PAGE =
            page(
                    "illusion",
                    UNLOCK_ILLUSION.id(),
                    ILLUSION_ROOT,
                    ILLUSION_GLAMOUR,
                    ILLUSION_PHANTASM,
                    ILLUSION_MISDIRECTION,
                    ILLUSION_MIRAGE
            );

    /*
     * -----------------------------------------------------------------
     * Passage
     *
     * A branching route that joins at the final threshold.
     * -----------------------------------------------------------------
     */

    public static final TomeNode PASSAGE_ROOT =
            root(
                    "passage/root",
                    "passage",
                    -105,
                    -55
            );

    public static final TomeNode PASSAGE_SHORT_STEP =
            node(
                    "passage/short_step",
                    "passage",
                    -35,
                    -15,
                    5,
                    PASSAGE_ROOT.id()
            );

    public static final TomeNode PASSAGE_WAYMARK =
            node(
                    "passage/waymark",
                    "passage",
                    35,
                    -55,
                    10,
                    PASSAGE_SHORT_STEP.id()
            );

    public static final TomeNode PASSAGE_THRESHOLD =
            node(
                    "passage/threshold",
                    "passage",
                    35,
                    35,
                    10,
                    PASSAGE_SHORT_STEP.id()
            );

    public static final TomeNode PASSAGE_CROSSING =
            node(
                    "passage/crossing",
                    "passage",
                    105,
                    -10,
                    25,
                    PASSAGE_WAYMARK.id(),
                    PASSAGE_THRESHOLD.id()
            );

    public static final TomePage PASSAGE_PAGE =
            page(
                    "passage",
                    UNLOCK_PASSAGE.id(),
                    PASSAGE_ROOT,
                    PASSAGE_SHORT_STEP,
                    PASSAGE_WAYMARK,
                    PASSAGE_THRESHOLD,
                    PASSAGE_CROSSING
            );

    /*
     * -----------------------------------------------------------------
     * Griefing
     *
     * A horizontal destructive progression.
     * -----------------------------------------------------------------
     */

    public static final TomeNode GRIEFING_ROOT =
            root(
                    "griefing/root",
                    "griefing",
                    -110,
                    0
            );

    public static final TomeNode GRIEFING_FRACTURE =
            node(
                    "griefing/fracture",
                    "griefing",
                    -35,
                    0,
                    5,
                    GRIEFING_ROOT.id()
            );

    public static final TomeNode GRIEFING_RUIN =
            node(
                    "griefing/ruin",
                    "griefing",
                    40,
                    0,
                    10,
                    GRIEFING_FRACTURE.id()
            );

    public static final TomeNode GRIEFING_DESOLATION =
            node(
                    "griefing/desolation",
                    "griefing",
                    110,
                    0,
                    20,
                    GRIEFING_RUIN.id()
            );

    public static final TomePage GRIEFING_PAGE =
            page(
                    "griefing",
                    UNLOCK_GRIEFING.id(),
                    GRIEFING_ROOT,
                    GRIEFING_FRACTURE,
                    GRIEFING_RUIN,
                    GRIEFING_DESOLATION
            );

    /*
     * -----------------------------------------------------------------
     * Necromancy
     *
     * A central study that divides into body and spirit.
     * -----------------------------------------------------------------
     */

    public static final TomeNode NECROMANCY_ROOT =
            root(
                    "necromancy/root",
                    "necromancy",
                    0,
                    -70
            );

    public static final TomeNode NECROMANCY_MEMORY =
            node(
                    "necromancy/mortal_memory",
                    "necromancy",
                    0,
                    -15,
                    5,
                    NECROMANCY_ROOT.id()
            );

    public static final TomeNode NECROMANCY_RAISE =
            node(
                    "necromancy/raise_lesser_dead",
                    "necromancy",
                    -75,
                    55,
                    15,
                    NECROMANCY_MEMORY.id()
            );

    public static final TomeNode NECROMANCY_BIND =
            node(
                    "necromancy/bind_spirit",
                    "necromancy",
                    75,
                    55,
                    15,
                    NECROMANCY_MEMORY.id()
            );

    public static final TomePage NECROMANCY_PAGE =
            page(
                    "necromancy",
                    UNLOCK_NECROMANCY.id(),
                    NECROMANCY_ROOT,
                    NECROMANCY_MEMORY,
                    NECROMANCY_RAISE,
                    NECROMANCY_BIND
            );

    /*
     * Page order is also tab order.
     */
    public static final List<TomePage> PAGES =
            List.of(
                    SCHOOLS_PAGE,
                    SUMMONING_PAGE,
                    COMMAND_PAGE,
                    NATURE_PAGE,
                    TRANSMUTATION_PAGE,
                    WARDING_PAGE,
                    DIVINATION_PAGE,
                    ILLUSION_PAGE,
                    PASSAGE_PAGE,
                    GRIEFING_PAGE,
                    NECROMANCY_PAGE
            );

    public static final List<TomeNode> ALL_NODES;

    private static final Map<String, TomePage>
            PAGES_BY_ID;

    private static final Map<String, TomeNode>
            NODES_BY_ID;

    static {
        Map<String, TomePage> pages =
                new LinkedHashMap<>();

        Map<String, TomeNode> nodes =
                new LinkedHashMap<>();

        List<TomeNode> allNodes =
                new ArrayList<>();

        for (TomePage page : PAGES) {
            if (pages.put(page.id(), page) != null) {
                throw new IllegalStateException(
                        "Duplicate Tome page ID: "
                                + page.id()
                );
            }

            for (TomeNode node : page.nodes()) {
                if (nodes.put(node.id(), node)
                        != null) {

                    throw new IllegalStateException(
                            "Duplicate Tome node ID: "
                                    + node.id()
                    );
                }

                allNodes.add(node);
            }
        }

        for (TomeNode node : allNodes) {
            for (String prerequisite :
                    node.prerequisites()) {

                if (!nodes.containsKey(prerequisite)) {
                    throw new IllegalStateException(
                            "Unknown prerequisite "
                                    + prerequisite
                                    + " for "
                                    + node.id()
                    );
                }
            }
        }

        for (TomePage page : PAGES) {
            if (page.unlockNodeId() != null
                    && !nodes.containsKey(
                    page.unlockNodeId()
            )) {

                throw new IllegalStateException(
                        "Unknown page unlock node "
                                + page.unlockNodeId()
                                + " for "
                                + page.id()
                );
            }
        }

        PAGES_BY_ID =
                Map.copyOf(pages);

        NODES_BY_ID =
                Map.copyOf(nodes);

        ALL_NODES =
                List.copyOf(allNodes);
    }

    private TomeTree() {
    }

    public static TomePage getPage(
            String pageId
    ) {
        return PAGES_BY_ID.get(pageId);
    }

    public static TomeNode getNode(
            String nodeId
    ) {
        return NODES_BY_ID.get(nodeId);
    }

    public static boolean isOwned(
            Collection<String> purchasedNodeIds,
            TomeNode node
    ) {
        if (node.root()) {
            return true;
        }

        return purchasedNodeIds.contains(
                node.id()
        );
    }

    public static boolean prerequisitesMet(
            Collection<String> purchasedNodeIds,
            TomeNode node
    ) {
        for (String prerequisiteId :
                node.prerequisites()) {

            TomeNode prerequisite =
                    getNode(prerequisiteId);

            if (prerequisite == null
                    || !isOwned(
                    purchasedNodeIds,
                    prerequisite
            )) {

                return false;
            }
        }

        return true;
    }

    public static boolean isPageUnlocked(
            Collection<String> purchasedNodeIds,
            TomePage page
    ) {
        return page.alwaysAvailable()
                || purchasedNodeIds.contains(
                page.unlockNodeId()
        );
    }

    public static List<TomePage> getUnlockedPages(
            Collection<String> purchasedNodeIds
    ) {
        List<TomePage> result =
                new ArrayList<>();

        for (TomePage page : PAGES) {
            if (isPageUnlocked(
                    purchasedNodeIds,
                    page
            )) {
                result.add(page);
            }
        }

        return List.copyOf(result);
    }

    private static TomeNode root(
            String id,
            String pageId,
            int x,
            int y
    ) {
        return new TomeNode(
                id,
                pageId,
                x,
                y,
                0,
                List.of(),
                true
        );
    }

    private static TomeNode node(
            String id,
            String pageId,
            int x,
            int y,
            int cost,
            String... prerequisites
    ) {
        return new TomeNode(
                id,
                pageId,
                x,
                y,
                cost,
                List.of(prerequisites),
                false
        );
    }

    private static TomePage page(
            String id,
            String unlockNodeId,
            TomeNode... nodes
    ) {
        return new TomePage(
                id,
                unlockNodeId,
                List.of(nodes)
        );
    }
}