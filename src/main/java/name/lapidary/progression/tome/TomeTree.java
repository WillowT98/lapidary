package name.lapidary.progression.tome;

import name.lapidary.magic.spell.ModSpells;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Static layout and reward definitions for the Tome of Insight.
 *
 * Every non-root node in a school page represents one implemented spell.
 * School unlock nodes remain on the overview page and do not grant spells.
 */
public final class TomeTree {
    public static final String SCHOOLS_PAGE_ID = "schools";

    /* Schools overview. */
    public static final TomeNode SCHOOLS_ROOT =
            root("schools/root", "schools", 0, 0);

    public static final TomeNode UNLOCK_SUMMONING =
            node("schools/summoning", "schools", -110, -55, 10, SCHOOLS_ROOT.id());
    public static final TomeNode UNLOCK_COMMAND =
            node("schools/command", "schools", -55, -75, 10, SCHOOLS_ROOT.id());
    public static final TomeNode UNLOCK_NATURE =
            node("schools/nature", "schools", 0, -80, 10, SCHOOLS_ROOT.id());
    public static final TomeNode UNLOCK_TRANSMUTATION =
            node("schools/transmutation", "schools", 55, -75, 10, SCHOOLS_ROOT.id());
    public static final TomeNode UNLOCK_WARDING =
            node("schools/warding", "schools", 110, -55, 10, SCHOOLS_ROOT.id());
    public static final TomeNode UNLOCK_DIVINATION =
            node("schools/divination", "schools", -110, 55, 10, SCHOOLS_ROOT.id());
    public static final TomeNode UNLOCK_ILLUSION =
            node("schools/illusion", "schools", -55, 75, 10, SCHOOLS_ROOT.id());
    public static final TomeNode UNLOCK_PASSAGE =
            node("schools/passage", "schools", 0, 80, 10, SCHOOLS_ROOT.id());
    public static final TomeNode UNLOCK_GRIEFING =
            node("schools/griefing", "schools", 55, 75, 10, SCHOOLS_ROOT.id());
    public static final TomeNode UNLOCK_NECROMANCY =
            node("schools/necromancy", "schools", 110, 55, 10, SCHOOLS_ROOT.id());

    public static final TomePage SCHOOLS_PAGE = page(
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

    /* Summoning: two permanent-creature branches. */
    public static final TomeNode SUMMONING_ROOT =
            root("summoning/root", "summoning", 0, -75);
    public static final TomeNode SUMMON_CHICKEN =
            node("summoning/summon_chicken", "summoning", -80, -20, 5, SUMMONING_ROOT.id());
    public static final TomeNode SUMMON_WOLF =
            node("summoning/summon_wolf", "summoning", 80, -20, 8, SUMMONING_ROOT.id());
    public static final TomeNode SUMMON_COW =
            node("summoning/summon_cow", "summoning", -80, 55, 10, SUMMON_CHICKEN.id());
    public static final TomeNode SUMMON_HORSE =
            node("summoning/summon_horse", "summoning", 80, 55, 15, SUMMON_WOLF.id());
    public static final TomePage SUMMONING_PAGE = page(
            "summoning", UNLOCK_SUMMONING.id(),
            SUMMONING_ROOT, SUMMON_CHICKEN, SUMMON_WOLF, SUMMON_COW, SUMMON_HORSE
    );

    /* Command: restraint on the left, coercion on the right, control at the convergence. */
    public static final TomeNode COMMAND_ROOT =
            root("command/root", "command", 0, -75);
    public static final TomeNode APPEASE =
            node("command/appease", "command", -80, -25, 5, COMMAND_ROOT.id());
    public static final TomeNode DISARM =
            node("command/disarm", "command", 80, -25, 5, COMMAND_ROOT.id());
    public static final TomeNode PACIFY =
            node("command/pacify", "command", -70, 45, 10, APPEASE.id());
    public static final TomeNode CONTROL_HOSTILE =
            node("command/control_hostile", "command", 45, 55, 20, PACIFY.id(), DISARM.id());
    public static final TomePage COMMAND_PAGE = page(
            "command", UNLOCK_COMMAND.id(),
            COMMAND_ROOT, APPEASE, DISARM, PACIFY, CONTROL_HOSTILE
    );

    /* Nature: growth is the common foundation for field and tree magic. */
    public static final TomeNode NATURE_ROOT =
            root("nature/root", "nature", 0, -75);
    public static final TomeNode ACCELERATE_GROWTH =
            node("nature/accelerate_growth", "nature", 0, -20, 5, NATURE_ROOT.id());
    public static final TomeNode FLOWER_FIELD =
            node("nature/flower_field", "nature", -75, 50, 10, ACCELERATE_GROWTH.id());
    public static final TomeNode BIG_TREE =
            node("nature/big_tree", "nature", 75, 50, 20, ACCELERATE_GROWTH.id());
    public static final TomePage NATURE_PAGE = page(
            "nature", UNLOCK_NATURE.id(),
            NATURE_ROOT, ACCELERATE_GROWTH, FLOWER_FIELD, BIG_TREE
    );

    /* Transmutation: three basic studies converge on ore recomposition. */
    public static final TomeNode TRANSMUTATION_ROOT =
            root("transmutation/root", "transmutation", 0, -75);
    public static final TomeNode CHISEL =
            node("transmutation/chisel", "transmutation", -95, 0, 5, TRANSMUTATION_ROOT.id());
    public static final TomeNode REPAIR =
            node("transmutation/repair", "transmutation", 0, 0, 10, TRANSMUTATION_ROOT.id());
    public static final TomeNode UNOXIDIZE =
            node("transmutation/unoxidize", "transmutation", 95, 0, 5, TRANSMUTATION_ROOT.id());
    public static final TomeNode ORE_RECOMPOSITION =
            node("transmutation/ore_recomposition", "transmutation", 0, 70, 20,
                    CHISEL.id(), REPAIR.id(), UNOXIDIZE.id());
    public static final TomePage TRANSMUTATION_PAGE = page(
            "transmutation", UNLOCK_TRANSMUTATION.id(),
            TRANSMUTATION_ROOT, CHISEL, REPAIR, UNOXIDIZE, ORE_RECOMPOSITION
    );

    /* Warding: personal protection and material protection. */
    public static final TomeNode WARDING_ROOT =
            root("warding/root", "warding", 0, -75);
    public static final TomeNode FIRE_PROTECTION =
            node("warding/fire_protection", "warding", -80, -10, 5, WARDING_ROOT.id());
    public static final TomeNode HARDEN_GLASS =
            node("warding/harden_glass", "warding", 75, -10, 10, WARDING_ROOT.id());
    public static final TomeNode HARDEN_BLOCK =
            node("warding/harden_block", "warding", 75, 60, 15, HARDEN_GLASS.id());
    public static final TomePage WARDING_PAGE = page(
            "warding", UNLOCK_WARDING.id(),
            WARDING_ROOT, FIRE_PROTECTION, HARDEN_GLASS, HARDEN_BLOCK
    );

    /* Divination: enhanced senses converge on hidden material revelation. */
    public static final TomeNode DIVINATION_ROOT =
            root("divination/root", "divination", 0, -75);
    public static final TomeNode NIGHT_VISION =
            node("divination/night_vision", "divination", -75, -15, 5, DIVINATION_ROOT.id());
    public static final TomeNode REVEAL_MOBS =
            node("divination/reveal_mobs", "divination", 75, -15, 10, DIVINATION_ROOT.id());
    public static final TomeNode REVEAL_ORES =
            node("divination/reveal_ores", "divination", 0, 65, 15,
                    NIGHT_VISION.id(), REVEAL_MOBS.id());
    public static final TomePage DIVINATION_PAGE = page(
            "divination", UNLOCK_DIVINATION.id(),
            DIVINATION_ROOT, NIGHT_VISION, REVEAL_MOBS, REVEAL_ORES
    );

    /* Illusion: concealment and solidified unreality. */
    public static final TomeNode ILLUSION_ROOT =
            root("illusion/root", "illusion", 0, -70);
    public static final TomeNode INVISIBILITY =
            node("illusion/invisibility", "illusion", -70, 25, 10, ILLUSION_ROOT.id());
    public static final TomeNode HARD_LIGHT =
            node("illusion/hard_light", "illusion", 70, 25, 10, ILLUSION_ROOT.id());
    public static final TomePage ILLUSION_PAGE = page(
            "illusion", UNLOCK_ILLUSION.id(),
            ILLUSION_ROOT, INVISIBILITY, HARD_LIGHT
    );

    /* Passage: movement branches converge on mastery over hostile terrain. */
    public static final TomeNode PASSAGE_ROOT =
            root("passage/root", "passage", 0, -75);
    public static final TomeNode SPEED =
            node("passage/speed", "passage", -95, -20, 5, PASSAGE_ROOT.id());
    public static final TomeNode AUTO_STEP =
            node("passage/auto_step", "passage", 0, -5, 5, PASSAGE_ROOT.id());
    public static final TomeNode BLINK =
            node("passage/blink", "passage", 95, -20, 10, PASSAGE_ROOT.id());
    public static final TomeNode FROST_WALKER =
            node("passage/frost_walker", "passage", -55, 55, 10, SPEED.id(), AUTO_STEP.id());
    public static final TomeNode LAVA_WALKER =
            node("passage/lava_walker", "passage", 55, 55, 20, FROST_WALKER.id(), BLINK.id());
    public static final TomePage PASSAGE_PAGE = page(
            "passage", UNLOCK_PASSAGE.id(),
            PASSAGE_ROOT, SPEED, AUTO_STEP, BLINK, FROST_WALKER, LAVA_WALKER
    );

    /* Griefing: explosive and gravitational branches converge on erasure. */
    public static final TomeNode GRIEFING_ROOT =
            root("griefing/root", "griefing", 0, -75);
    public static final TomeNode GHAST_FIREBALL =
            node("griefing/ghast_fireball", "griefing", -85, -20, 10, GRIEFING_ROOT.id());
    public static final TomeNode GRAVITY =
            node("griefing/gravity", "griefing", 85, -20, 15, GRIEFING_ROOT.id());
    public static final TomeNode LIT_TNT =
            node("griefing/lit_tnt", "griefing", -65, 50, 10, GHAST_FIREBALL.id());
    public static final TomeNode ERASE_MATCHING =
            node("griefing/erase_matching", "griefing", 50, 60, 20, LIT_TNT.id(), GRAVITY.id());
    public static final TomePage GRIEFING_PAGE = page(
            "griefing", UNLOCK_GRIEFING.id(),
            GRIEFING_ROOT, GHAST_FIREBALL, GRAVITY, LIT_TNT, ERASE_MATCHING
    );

    /* Necromancy: transformation, summoning, restoration, and the bound steed. */
    public static final TomeNode NECROMANCY_ROOT =
            root("necromancy/root", "necromancy", 0, -75);
    public static final TomeNode SUMMON_SKELETON =
            node("necromancy/summon_skeleton", "necromancy", -95, -20, 10, NECROMANCY_ROOT.id());
    public static final TomeNode FLENSE =
            node("necromancy/flense", "necromancy", 0, -10, 5, NECROMANCY_ROOT.id());
    public static final TomeNode CLEANSE_VILLAGER =
            node("necromancy/cleanse_villager", "necromancy", 95, -20, 15, NECROMANCY_ROOT.id());
    public static final TomeNode CHANGE_UNDEAD =
            node("necromancy/change_undead", "necromancy", -55, 55, 15,
                    SUMMON_SKELETON.id(), FLENSE.id());
    public static final TomeNode SKELETON_STEED =
            node("necromancy/skeleton_steed", "necromancy", 55, 55, 25,
                    SUMMON_SKELETON.id(), CLEANSE_VILLAGER.id());
    public static final TomePage NECROMANCY_PAGE = page(
            "necromancy", UNLOCK_NECROMANCY.id(),
            NECROMANCY_ROOT, SUMMON_SKELETON, FLENSE,
            CLEANSE_VILLAGER, CHANGE_UNDEAD, SKELETON_STEED
    );

    /** Page order is also tab order. */
    public static final List<TomePage> PAGES = List.of(
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

    private static final Map<String, TomePage> PAGES_BY_ID;
    private static final Map<String, TomeNode> NODES_BY_ID;
    private static final Map<String, ResourceLocation> SPELL_REWARDS_BY_NODE_ID;
    private static final List<ResourceLocation> MANAGED_SPELL_IDS;

    static {
        Map<String, TomePage> pages = new LinkedHashMap<>();
        Map<String, TomeNode> nodes = new LinkedHashMap<>();
        List<TomeNode> allNodes = new ArrayList<>();

        for (TomePage page : PAGES) {
            if (pages.put(page.id(), page) != null) {
                throw new IllegalStateException("Duplicate Tome page ID: " + page.id());
            }

            for (TomeNode node : page.nodes()) {
                if (nodes.put(node.id(), node) != null) {
                    throw new IllegalStateException("Duplicate Tome node ID: " + node.id());
                }
                allNodes.add(node);
            }
        }

        for (TomeNode node : allNodes) {
            for (String prerequisite : node.prerequisites()) {
                if (!nodes.containsKey(prerequisite)) {
                    throw new IllegalStateException(
                            "Unknown prerequisite " + prerequisite + " for " + node.id()
                    );
                }
            }
        }

        for (TomePage page : PAGES) {
            if (page.unlockNodeId() != null && !nodes.containsKey(page.unlockNodeId())) {
                throw new IllegalStateException(
                        "Unknown page unlock node " + page.unlockNodeId() + " for " + page.id()
                );
            }
        }

        Map<String, ResourceLocation> rewards = new LinkedHashMap<>();

        reward(rewards, SUMMON_CHICKEN, ModSpells.SUMMON_CHICKEN.id());
        reward(rewards, SUMMON_COW, ModSpells.SUMMON_COW.id());
        reward(rewards, SUMMON_WOLF, ModSpells.SUMMON_WOLF.id());
        reward(rewards, SUMMON_HORSE, ModSpells.SUMMON_HORSE.id());

        reward(rewards, DISARM, ModSpells.DISARM.id());
        reward(rewards, APPEASE, ModSpells.APPEASE.id());
        reward(rewards, PACIFY, ModSpells.PACIFY.id());
        reward(rewards, CONTROL_HOSTILE, ModSpells.CONTROL_HOSTILE.id());

        reward(rewards, ACCELERATE_GROWTH, ModSpells.ACCELERATE_GROWTH.id());
        reward(rewards, FLOWER_FIELD, ModSpells.FLOWER_FIELD.id());
        reward(rewards, BIG_TREE, ModSpells.BIG_TREE.id());

        reward(rewards, UNOXIDIZE, ModSpells.UNOXIDIZE.id());
        reward(rewards, REPAIR, ModSpells.REPAIR.id());
        reward(rewards, CHISEL, ModSpells.CHISEL.id());
        reward(rewards, ORE_RECOMPOSITION, ModSpells.ORE_RECOMPOSITION.id());

        reward(rewards, HARDEN_BLOCK, ModSpells.HARDEN_BLOCK.id());
        reward(rewards, HARDEN_GLASS, ModSpells.HARDEN_GLASS.id());
        reward(rewards, FIRE_PROTECTION, ModSpells.FIRE_PROTECTION.id());

        reward(rewards, REVEAL_MOBS, ModSpells.REVEAL_MOBS.id());
        reward(rewards, REVEAL_ORES, ModSpells.REVEAL_ORES.id());
        reward(rewards, NIGHT_VISION, ModSpells.NIGHT_VISION.id());

        reward(rewards, SPEED, ModSpells.SPEED.id());
        reward(rewards, AUTO_STEP, ModSpells.AUTO_STEP.id());
        reward(rewards, BLINK, ModSpells.BLINK.id());
        reward(rewards, FROST_WALKER, ModSpells.FROST_WALKER.id());
        reward(rewards, LAVA_WALKER, ModSpells.LAVA_WALKER.id());

        reward(rewards, INVISIBILITY, ModSpells.INVISIBILITY.id());
        reward(rewards, HARD_LIGHT, ModSpells.HARD_LIGHT.id());

        reward(rewards, SUMMON_SKELETON, ModSpells.SUMMON_SKELETON.id());
        reward(rewards, FLENSE, ModSpells.FLENSE.id());
        reward(rewards, CLEANSE_VILLAGER, ModSpells.CLEANSE_VILLAGER.id());
        reward(rewards, CHANGE_UNDEAD, ModSpells.CHANGE_UNDEAD.id());
        reward(rewards, SKELETON_STEED, ModSpells.SKELETON_STEED.id());

        reward(rewards, GHAST_FIREBALL, ModSpells.GHAST_FIREBALL.id());
        reward(rewards, LIT_TNT, ModSpells.LIT_TNT.id());
        reward(rewards, ERASE_MATCHING, ModSpells.ERASE_MATCHING.id());
        reward(rewards, GRAVITY, ModSpells.GRAVITY.id());

        for (Map.Entry<String, ResourceLocation> entry : rewards.entrySet()) {
            if (!nodes.containsKey(entry.getKey())) {
                throw new IllegalStateException("Spell reward references unknown Tome node: " + entry.getKey());
            }
            if (!ModSpells.contains(entry.getValue())) {
                throw new IllegalStateException("Tome reward references unknown spell: " + entry.getValue());
            }
        }

        PAGES_BY_ID = Map.copyOf(pages);
        NODES_BY_ID = Map.copyOf(nodes);
        SPELL_REWARDS_BY_NODE_ID = Map.copyOf(rewards);
        MANAGED_SPELL_IDS = List.copyOf(new LinkedHashSet<>(rewards.values()));
        ALL_NODES = List.copyOf(allNodes);
    }

    private TomeTree() {
    }

    public static TomePage getPage(String pageId) {
        return PAGES_BY_ID.get(pageId);
    }

    public static TomeNode getNode(String nodeId) {
        return NODES_BY_ID.get(nodeId);
    }

    public static ResourceLocation getSpellReward(String nodeId) {
        return SPELL_REWARDS_BY_NODE_ID.get(nodeId);
    }

    public static ResourceLocation getSpellReward(TomeNode node) {
        return node == null ? null : getSpellReward(node.id());
    }

    /** Every spell whose ownership is controlled by this Tome tree. */
    public static List<ResourceLocation> getManagedSpellIds() {
        return MANAGED_SPELL_IDS;
    }

    public static boolean isOwned(Collection<String> purchasedNodeIds, TomeNode node) {
        return node.root() || purchasedNodeIds.contains(node.id());
    }

    public static boolean prerequisitesMet(
            Collection<String> purchasedNodeIds,
            TomeNode node
    ) {
        for (String prerequisiteId : node.prerequisites()) {
            TomeNode prerequisite = getNode(prerequisiteId);
            if (prerequisite == null || !isOwned(purchasedNodeIds, prerequisite)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isPageUnlocked(
            Collection<String> purchasedNodeIds,
            TomePage page
    ) {
        return page.alwaysAvailable() || purchasedNodeIds.contains(page.unlockNodeId());
    }

    public static List<TomePage> getUnlockedPages(Collection<String> purchasedNodeIds) {
        List<TomePage> result = new ArrayList<>();
        for (TomePage page : PAGES) {
            if (isPageUnlocked(purchasedNodeIds, page)) {
                result.add(page);
            }
        }
        return List.copyOf(result);
    }

    private static void reward(
            Map<String, ResourceLocation> rewards,
            TomeNode node,
            ResourceLocation spellId
    ) {
        ResourceLocation previous = rewards.put(node.id(), spellId);
        if (previous != null) {
            throw new IllegalStateException("Duplicate spell reward for Tome node: " + node.id());
        }
    }

    private static TomeNode root(String id, String pageId, int x, int y) {
        return new TomeNode(id, pageId, x, y, 0, List.of(), true);
    }

    private static TomeNode node(
            String id,
            String pageId,
            int x,
            int y,
            int cost,
            String... prerequisites
    ) {
        return new TomeNode(id, pageId, x, y, cost, List.of(prerequisites), false);
    }

    private static TomePage page(
            String id,
            String unlockNodeId,
            TomeNode... nodes
    ) {
        return new TomePage(id, unlockNodeId, List.of(nodes));
    }
}
