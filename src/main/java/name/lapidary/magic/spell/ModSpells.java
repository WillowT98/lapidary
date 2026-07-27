package name.lapidary.magic.spell;

import name.lapidary.Lapidary;
import name.lapidary.fluid.CanisterFluidStorage;
import name.lapidary.item.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class ModSpells {
    private static final long B = CanisterFluidStorage.BUCKET;
    private static final Map<ResourceLocation, SpellDefinition> SPELLS =
            new LinkedHashMap<>();

    public static final SpellDefinition MAGE_LIGHT = instant(
            "mage_light", B, ModItems.MAGE_LIGHT_ORB, MageLightSpell::cast
    );

    // Summoning
    public static final SpellDefinition SUMMON_CHICKEN = instant(
            "summon_chicken", B / 2, Items.CHICKEN_SPAWN_EGG,
            FirstPassSpells::canSummon, FirstPassSpells::summonChicken
    );
    public static final SpellDefinition SUMMON_COW = instant(
            "summon_cow", B + B / 2, Items.COW_SPAWN_EGG,
            FirstPassSpells::canSummon, FirstPassSpells::summonCow
    );
    public static final SpellDefinition SUMMON_WOLF = instant(
            "summon_wolf", B, Items.WOLF_SPAWN_EGG,
            FirstPassSpells::canSummon, FirstPassSpells::summonWolf
    );
    public static final SpellDefinition SUMMON_HORSE = instant(
            "summon_horse", B * 2, Items.HORSE_SPAWN_EGG,
            FirstPassSpells::canSummon, FirstPassSpells::summonHorse
    );

    // Command
    public static final SpellDefinition DISARM = instant(
            "disarm", B / 4, Items.IRON_SWORD,
            FirstPassSpells::canDisarm, FirstPassSpells::disarm
    );
    public static final SpellDefinition APPEASE = instant(
            "appease", B / 8, Items.POPPY,
            FirstPassSpells::canCommandHostile, FirstPassSpells::appease
    );
    public static final SpellDefinition PACIFY = instant(
            "pacify", B / 2, Items.WHITE_WOOL,
            FirstPassSpells::canCommandHostile, FirstPassSpells::pacify
    );
    public static final SpellDefinition CONTROL_HOSTILE = instant(
            "control_hostile", B / 2, Items.CHAIN,
            FirstPassSpells::canCommandHostile, FirstPassSpells::controlHostile
    );

    // Nature
    public static final SpellDefinition ACCELERATE_GROWTH = channelled(
            "accelerate_growth", B / 40, Items.BONE_MEAL,
            FirstPassSpells::canAccelerateGrowth, FirstPassSpells::accelerateGrowth
    );
    public static final SpellDefinition FLOWER_FIELD = instant(
            "flower_field", B / 2, Items.ALLIUM,
            FirstPassSpells::canFlowerField, FirstPassSpells::flowerField
    );
    public static final SpellDefinition BIG_TREE = instant(
            "big_tree", B * 2, Items.OAK_SAPLING,
            FirstPassSpells::canBigTree, FirstPassSpells::bigTree
    );

    // Transmutation
    public static final SpellDefinition UNOXIDIZE = instant(
            "unoxidize", B / 8, Items.COPPER_INGOT,
            FirstPassSpells::canUnoxidize, FirstPassSpells::unoxidize
    );
    public static final SpellDefinition REPAIR = channelled(
            "repair", B / 40, Items.ANVIL,
            FirstPassSpells::canRepair, FirstPassSpells::repair
    );
    public static final SpellDefinition CHISEL = instant(
            "chisel", B / 20, Items.STONECUTTER,
            FirstPassSpells::canChisel, FirstPassSpells::chisel
    );
    public static final SpellDefinition ORE_RECOMPOSITION = instant(
            "ore_recomposition", B / 4, Items.RAW_IRON,
            FirstPassSpells::canRecompose, FirstPassSpells::recompose
    );

    // Warding
    public static final SpellDefinition HARDEN_BLOCK = instant(
            "harden_block", B / 2, Items.OBSIDIAN,
            FirstPassSpells::canHardenBlock, FirstPassSpells::hardenBlock
    );
    public static final SpellDefinition HARDEN_GLASS = instant(
            "harden_glass", B / 2, Items.GLASS,
            FirstPassSpells::canHardenGlass, FirstPassSpells::hardenGlass
    );
    public static final SpellDefinition FIRE_PROTECTION = instant(
            "fire_protection", B / 2, Items.MAGMA_CREAM,
            FirstPassSpells::fireProtection
    );

    // Divination
    public static final SpellDefinition REVEAL_MOBS = instant(
            "reveal_mobs", B / 4, Items.SPYGLASS,
            FirstPassSpells::revealMobs
    );
    public static final SpellDefinition REVEAL_ORES = instant(
            "reveal_ores", B / 2, Items.RAW_COPPER,
            FirstPassSpells::revealOres
    );
    public static final SpellDefinition NIGHT_VISION = instant(
            "night_vision", B / 4, Items.GOLDEN_CARROT,
            FirstPassSpells::nightVision
    );

    // Passage
    public static final SpellDefinition SPEED = instant(
            "speed", B / 4, Items.SUGAR,
            FirstPassSpells::speed
    );
    public static final SpellDefinition AUTO_STEP = instant(
            "auto_step", B / 4, Items.OAK_STAIRS,
            FirstPassSpells::autoStep
    );
    public static final SpellDefinition BLINK = instant(
            "blink", B / 4, Items.ENDER_PEARL,
            FirstPassSpells::canBlink, FirstPassSpells::blink
    );
    public static final SpellDefinition FROST_WALKER = instant(
            "frost_walker", B / 2, Items.BLUE_ICE,
            FirstPassSpells::frostWalker
    );
    public static final SpellDefinition LAVA_WALKER = instant(
            "lava_walker", B, Items.OBSIDIAN,
            FirstPassSpells::lavaWalker
    );

    // Illusion
    public static final SpellDefinition INVISIBILITY = instant(
            "invisibility", B / 2, Items.FERMENTED_SPIDER_EYE,
            FirstPassSpells::invisibility
    );
    public static final SpellDefinition HARD_LIGHT = instant(
            "hard_light", B / 8, Items.LIGHT_BLUE_STAINED_GLASS,
            FirstPassSpells::canHardLight, FirstPassSpells::hardLight
    );

    // Necromancy
    public static final SpellDefinition SUMMON_SKELETON = instant(
            "summon_skeleton", B, Items.SKELETON_SKULL,
            FirstPassSpells::canSummon, FirstPassSpells::summonSkeleton
    );
    public static final SpellDefinition FLENSE = instant(
            "flense", B / 4, Items.BONE,
            FirstPassSpells::canFlense, FirstPassSpells::flense
    );
    public static final SpellDefinition CLEANSE_VILLAGER = instant(
            "cleanse_villager", B * 2, Items.GOLDEN_APPLE,
            FirstPassSpells::canCleanseVillager, FirstPassSpells::cleanseVillager
    );
    public static final SpellDefinition CHANGE_UNDEAD = instant(
            "change_undead", B / 2, Items.WITHER_ROSE,
            FirstPassSpells::canChangeUndead, FirstPassSpells::changeUndead
    );
    public static final SpellDefinition SKELETON_STEED = instant(
            "skeleton_steed", B * 3, Items.SKELETON_HORSE_SPAWN_EGG,
            FirstPassSpells::canSummon, FirstPassSpells::skeletonSteed
    );

    // Griefing
    public static final SpellDefinition GHAST_FIREBALL = instant(
            "ghast_fireball", B / 2, Items.FIRE_CHARGE,
            FirstPassSpells::ghastFireball
    );
    public static final SpellDefinition LIT_TNT = instant(
            "lit_tnt", B / 2, Items.TNT,
            FirstPassSpells::canTnt, FirstPassSpells::litTnt
    );
    public static final SpellDefinition ERASE_MATCHING = instant(
            "erase_matching", B, Items.NETHERITE_PICKAXE,
            FirstPassSpells::canEraseMatching, FirstPassSpells::eraseMatching
    );
    public static final SpellDefinition GRAVITY = instant(
            "gravity", B, Items.ANVIL,
            FirstPassSpells::canGravity, FirstPassSpells::gravity
    );

    private ModSpells() {
    }

    public static void initialize() {
        Lapidary.LOGGER.info("Registered {} Lapidary spells", SPELLS.size());
    }

    private static SpellDefinition instant(
            String path,
            long manaCost,
            Item icon,
            SpellEffect effect
    ) {
        return register(path, manaCost, () -> new ItemStack(icon),
                SpellCastingMode.INSTANT, effect);
    }

    private static SpellDefinition instant(
            String path,
            long manaCost,
            Item icon,
            Predicate<SpellCastContext> canCast,
            SpellEffect effect
    ) {
        return register(path, manaCost, () -> new ItemStack(icon),
                SpellCastingMode.INSTANT, guarded(canCast, effect));
    }

    private static SpellDefinition channelled(
            String path,
            long manaCost,
            Item icon,
            Predicate<SpellCastContext> canCast,
            SpellEffect effect
    ) {
        return register(path, manaCost, () -> new ItemStack(icon),
                SpellCastingMode.CHANNELLED, guarded(canCast, effect));
    }

    private static SpellEffect guarded(
            Predicate<SpellCastContext> canCast,
            SpellEffect effect
    ) {
        return new SpellEffect() {
            @Override
            public void cast(SpellCastContext context) {
                effect.cast(context);
            }

            @Override
            public boolean canCast(SpellCastContext context) {
                return canCast.test(context);
            }
        };
    }

    public static SpellDefinition register(
            String path,
            long manaCost,
            Supplier<ItemStack> iconSupplier,
            SpellCastingMode mode,
            SpellEffect effect
    ) {
        return register(Lapidary.id(path), manaCost, iconSupplier, mode, effect);
    }

    public static SpellDefinition register(
            ResourceLocation id,
            long manaCost,
            Supplier<ItemStack> iconSupplier,
            SpellCastingMode mode,
            SpellEffect effect
    ) {
        if (SPELLS.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate spell ID: " + id);
        }
        SpellDefinition definition = new SpellDefinition(
                id,
                manaCost,
                iconSupplier,
                mode,
                effect
        );
        SPELLS.put(id, definition);
        return definition;
    }

    public static Optional<SpellDefinition> get(ResourceLocation id) {
        return Optional.ofNullable(SPELLS.get(id));
    }

    public static boolean contains(ResourceLocation id) {
        return SPELLS.containsKey(id);
    }

    public static List<SpellDefinition> values() {
        return List.copyOf(SPELLS.values());
    }
}
