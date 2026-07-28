package name.lapidary.guide;

import name.lapidary.Lapidary;
import name.lapidary.magic.PlayerMagic;
import name.lapidary.magic.spell.ModSpells;
import name.lapidary.magic.spell.SpellDefinition;
import name.lapidary.origin.OriginKind;
import name.lapidary.progression.LapidaryInsight;
import name.lapidary.progression.tome.TomeProgression;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Reconciles the field journal with facts learned by one specific player.
 * Invisible advancements make the discoveries persistent and allow Patchouli
 * to hide entries until they become actionable.
 */
public final class GuidebookProgression {
    private static final int SILENT_LOGIN_TICKS = 100;
    private static final Map<UUID, Integer> SILENT_TICKS = new HashMap<>();

    private static final Set<ResourceLocation> GEM_ITEMS = ids(
            "lapidary:sea_glass",
            "minecraft:amethyst_shard",
            "lapidary:bismuth_shard",
            "minecraft:diamond",
            "minecraft:ender_pearl",
            "lapidary:fulgurite",
            "lapidary:heartroot",
            "lapidary:pure_lapis",
            "lapidary:pearl"
    );

    private static final Map<ResourceLocation, Unlock> SPECIMENS = Map.ofEntries(
            specimen("lapidary:sea_glass", "sea_glass", "Sea Glass"),
            specimen("lapidary:fine_sand", "fine_sand", "Fine Sand"),
            specimen("lapidary:loam", "loam", "Loam"),
            specimen("lapidary:gold_flakes", "gold_flakes", "Gold Flakes"),
            specimen("lapidary:fulgurite", "fulgurite", "Fulgurite"),
            specimen("lapidary:pure_lapis", "pure_lapis", "Pure Lapis"),
            specimen("lapidary:pearl", "pearl", "Pearl"),
            specimen("lapidary:heartroot", "heartroot", "Heartroot"),
            specimen("lapidary:bismuth_shard", "bismuth_shard", "Bismuth Shard"),
            specimen("lapidary:sable_fur", "sable_fur", "Sable Fur"),
            specimen("lapidary:electrostatic_mix", "electrostatic_mix", "Electrostatic Mix")
    );

    private static final Map<String, String> SCHOOL_TITLES = Map.ofEntries(
            Map.entry("summoning", "Summoning"),
            Map.entry("command", "Command"),
            Map.entry("nature", "Nature"),
            Map.entry("transmutation", "Transmutation"),
            Map.entry("warding", "Warding"),
            Map.entry("divination", "Divination"),
            Map.entry("passage", "Passage"),
            Map.entry("illusion", "Illusion"),
            Map.entry("necromancy", "Necromancy"),
            Map.entry("griefing", "Griefing")
    );

    private static final Map<String, SpellNote> SPELL_NOTES = spellNotes();

    private GuidebookProgression() {
    }

    public static void initialize() {
        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> {
                    ServerPlayer player = handler.player;
                    SILENT_TICKS.put(
                            player.getUUID(),
                            SILENT_LOGIN_TICKS
                    );
                    reconcile(player, false);
                }
        );

        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) ->
                        SILENT_TICKS.remove(handler.player.getUUID())
        );

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                int remaining = SILENT_TICKS.getOrDefault(
                        player.getUUID(),
                        0
                );
                if (remaining > 0) {
                    SILENT_TICKS.put(player.getUUID(), remaining - 1);
                }

                if (player.tickCount % 20 == 0) {
                    reconcile(player, remaining <= 0);
                }
            }
        });

        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }

            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(
                    level.getBlockState(hit.getBlockPos()).getBlock()
            );
            unlockForUsedBlock(serverPlayer, blockId, true);
            return InteractionResult.PASS;
        });

        Lapidary.LOGGER.info("Initialized personal field-guide progression");
    }

    public static void reconcile(
            ServerPlayer player,
            boolean notify
    ) {
        boolean firstJournal = GuidebookAdvancements.grant(
                player,
                GuidebookAdvancements.ROOT,
                "A Strange Awakening",
                false
        );
        if (firstJournal) {
            GuidebookAdvancements.giveInitialJournal(player);
        }

        Set<ResourceLocation> inventory = inventoryIds(player);
        unlockOrigin(player, inventory, notify);

        boolean underground = player.getY() <= 48.0D
                && !player.serverLevel().canSeeSky(player.blockPosition());
        if (underground
                || inventory.contains(id("lapidary:sieve"))
                || inventory.contains(id("lapidary:gold_pan"))) {
            grant(player, "fieldcraft", "Unfamiliar Absences", notify);
        }

        if (inventory.contains(id("lapidary:sieve"))) {
            grant(player, "fieldcraft", "Unfamiliar Absences", false);
            grant(player, "sieve", "Working the Sieve", notify);
        }
        if (inventory.contains(id("lapidary:gold_pan"))) {
            grant(player, "fieldcraft", "Unfamiliar Absences", false);
            grant(player, "gold_pan", "Panning a River", notify);
        }

        for (Map.Entry<ResourceLocation, Unlock> entry : SPECIMENS.entrySet()) {
            if (inventory.contains(entry.getKey())) {
                Unlock unlock = entry.getValue();
                grant(player, unlock.path(), unlock.title(), notify);
            }
        }

        boolean hasGem = inventory.stream().anyMatch(GEM_ITEMS::contains);
        if (hasGem) {
            grant(player, "gem", "Something Within the Stone", notify);
        }

        if (inventory.contains(id("lapidary:gem_cutter"))) {
            grantWorkshopThrough(player, "gem_cutter", notify);
        }
        if (LapidaryInsight.get(player) > 0) {
            grant(player, "gem", "Something Within the Stone", false);
            grant(player, "insight", "Insight", notify);
        }
        if (inventory.contains(id("lapidary:jewelers_table"))) {
            grantWorkshopThrough(player, "jewelers_table", notify);
        }

        boolean hasMana = inventory.contains(id("lapidary:mana_bucket"));
        if (hasMana) {
            unlockMana(player, notify);
        }
        if (inventory.contains(id("lapidary:canister"))) {
            grant(player, "canister", "Mana Canisters", notify);
        }
        if (inventory.contains(id("lapidary:mage_backpack"))) {
            grant(player, "mage_backpack", "A Reservoir Carried", notify);
        }
        if (inventory.contains(id("lapidary:staff"))) {
            grant(player, "staff", "A Focus for Magic", notify);
        }
        if (inventory.contains(id("lapidary:tome_table"))) {
            grant(player, "tome", "The Tome of Insight", notify);
        }

        unlockTomeKnowledge(player, notify);
        unlockNearbyCreatures(player, notify);
    }

    private static void unlockOrigin(
            ServerPlayer player,
            Set<ResourceLocation> inventory,
            boolean notify
    ) {
        OriginKind origin = OriginKind.of(player);
        String originPath;
        String originTitle;
        switch (origin) {
            case FELINE -> {
                originPath = "feline";
                originTitle = "A Feline Once-Over";
            }
            case MOTH -> {
                originPath = "moth";
                originTitle = "A Moth Once-Over";
            }
            case FAIRY -> {
                originPath = "fairy";
                originTitle = "A Fairy Once-Over";
            }
            default -> {
                return;
            }
        }

        grant(player, "origin/" + originPath, originTitle, notify);
        if (inventory.contains(id("lapidary:mana_bucket"))) {
            grant(
                    player,
                    "innate/" + originPath,
                    switch (origin) {
                        case FELINE -> "An Innate Art: Night Form";
                        case MOTH -> "An Innate Art Unformed";
                        case FAIRY -> "An Innate Art: Restorative Burst";
                        default -> "An Innate Art";
                    },
                    notify
            );
        }
    }

    private static void unlockMana(
            ServerPlayer player,
            boolean notify
    ) {
        grant(player, "mana", "Mana, Made Tangible", notify);

        OriginKind origin = OriginKind.of(player);
        if (origin != OriginKind.NONE) {
            String path = origin.name().toLowerCase();
            grant(
                    player,
                    "innate/" + path,
                    switch (origin) {
                        case FELINE -> "An Innate Art: Night Form";
                        case MOTH -> "An Innate Art Unformed";
                        case FAIRY -> "An Innate Art: Restorative Burst";
                        default -> "An Innate Art";
                    },
                    notify
            );
        }
    }

    private static void unlockTomeKnowledge(
            ServerPlayer player,
            boolean notify
    ) {
        List<String> purchased = TomeProgression.getPurchasedNodeIds(player);
        for (Map.Entry<String, String> school : SCHOOL_TITLES.entrySet()) {
            if (purchased.contains("schools/" + school.getKey())) {
                grant(
                        player,
                        "school/" + school.getKey(),
                        school.getValue(),
                        notify
                );
            }
        }

        for (SpellDefinition spell : ModSpells.values()) {
            String path = spell.id().getPath();
            if (path.equals("mage_light")
                    || !PlayerMagic.get(player).knowsSpell(spell.id())) {
                continue;
            }

            SpellNote note = SPELL_NOTES.get(path);
            if (note == null) {
                Lapidary.LOGGER.warn(
                        "Known spell {} has no field-guide note mapping",
                        spell.id()
                );
                continue;
            }
            grant(player, "school/" + note.school(),
                    SCHOOL_TITLES.get(note.school()), false);
            grant(player, "spell/" + path, note.title(), notify);
        }
    }

    private static void unlockNearbyCreatures(
            ServerPlayer player,
            boolean notify
    ) {
        for (Entity entity : player.serverLevel().getEntities(
                player,
                player.getBoundingBox().inflate(12.0D),
                Entity::isAlive
        )) {
            ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(
                    entity.getType()
            );
            if (typeId.equals(id("lapidary:ore_mimic"))) {
                grant(player, "creature/ore_mimic", "Ore Mimics", notify);
            } else if (typeId.equals(id("lapidary:sable"))) {
                grant(player, "creature/sable", "Sables", notify);
            }
        }
    }

    private static void unlockForUsedBlock(
            ServerPlayer player,
            ResourceLocation blockId,
            boolean notify
    ) {
        String id = blockId.toString();
        switch (id) {
            case "lapidary:gem_cutter" ->
                    grantWorkshopThrough(player, "gem_cutter", notify);
            case "lapidary:jewelers_table" ->
                    grantWorkshopThrough(player, "jewelers_table", notify);
            case "lapidary:mana_percolator" ->
                    unlockMana(player, notify);
            case "lapidary:canister" ->
                    grant(player, "canister", "Mana Canisters", notify);
            case "lapidary:tome_table" ->
                    grant(player, "tome", "The Tome of Insight", notify);
            default -> {
            }
        }
    }

    private static void grantWorkshopThrough(
            ServerPlayer player,
            String stage,
            boolean notify
    ) {
        grant(player, "gem", "Something Within the Stone", false);
        grant(player, "gem_cutter", "The Gem Cutter", notify);
        if (stage.equals("jewelers_table")) {
            grant(player, "jewelers_table", "The Jeweler’s Table", notify);
        }
    }

    private static void grant(
            ServerPlayer player,
            String path,
            String title,
            boolean notify
    ) {
        GuidebookAdvancements.grant(player, path, title, notify);
    }

    private static Set<ResourceLocation> inventoryIds(ServerPlayer player) {
        Set<ResourceLocation> ids = new HashSet<>();
        for (int slot = 0;
             slot < player.getInventory().getContainerSize();
             slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty()) {
                ids.add(BuiltInRegistries.ITEM.getKey(stack.getItem()));
            }
        }
        return ids;
    }

    private static ResourceLocation id(String value) {
        int separator = value.indexOf(':');
        return ResourceLocation.fromNamespaceAndPath(
                value.substring(0, separator),
                value.substring(separator + 1)
        );
    }

    private static Set<ResourceLocation> ids(String... values) {
        Set<ResourceLocation> result = new HashSet<>();
        for (String value : values) {
            result.add(id(value));
        }
        return Set.copyOf(result);
    }

    private static Map.Entry<ResourceLocation, Unlock> specimen(
            String itemId,
            String path,
            String title
    ) {
        return Map.entry(
                id(itemId),
                new Unlock("specimen/" + path, title)
        );
    }

    private static Map<String, SpellNote> spellNotes() {
        Map<String, SpellNote> map = new HashMap<>();
        add(map, "summoning", "Summon Chicken", "summon_chicken");
        add(map, "summoning", "Summon Cow", "summon_cow");
        add(map, "summoning", "Summon Wolf", "summon_wolf");
        add(map, "summoning", "Summon Horse", "summon_horse");
        add(map, "command", "Appease", "appease");
        add(map, "command", "Disarm", "disarm");
        add(map, "command", "Pacify", "pacify");
        add(map, "command", "Control Hostile", "control_hostile");
        add(map, "nature", "Accelerate Growth", "accelerate_growth");
        add(map, "nature", "Flower Field", "flower_field");
        add(map, "nature", "Big Tree", "big_tree");
        add(map, "transmutation", "Chisel", "chisel");
        add(map, "transmutation", "Repair", "repair");
        add(map, "transmutation", "Unoxidize", "unoxidize");
        add(map, "transmutation", "Ore Recomposition", "ore_recomposition");
        add(map, "warding", "Fire Protection", "fire_protection");
        add(map, "warding", "Harden Glass", "harden_glass");
        add(map, "warding", "Harden Block", "harden_block");
        add(map, "divination", "Night Vision", "night_vision");
        add(map, "divination", "Reveal Creatures", "reveal_mobs");
        add(map, "divination", "Reveal Ores", "reveal_ores");
        add(map, "passage", "Fleetness", "speed");
        add(map, "passage", "Sure Step", "auto_step");
        add(map, "passage", "Blink", "blink");
        add(map, "passage", "Frost Walker", "frost_walker");
        add(map, "passage", "Lava Walker", "lava_walker");
        add(map, "illusion", "Invisibility", "invisibility");
        add(map, "illusion", "Hard Light", "hard_light");
        add(map, "necromancy", "Summon Skeleton", "summon_skeleton");
        add(map, "necromancy", "Flense", "flense");
        add(map, "necromancy", "Cleanse Villager", "cleanse_villager");
        add(map, "necromancy", "Change Undead", "change_undead");
        add(map, "necromancy", "Skeleton Steed", "skeleton_steed");
        add(map, "griefing", "Ghast Fireball", "ghast_fireball");
        add(map, "griefing", "Gravity", "gravity");
        add(map, "griefing", "Conjure Lit TNT", "lit_tnt");
        add(map, "griefing", "Erase Matching Blocks", "erase_matching");
        return Map.copyOf(map);
    }

    private static void add(
            Map<String, SpellNote> map,
            String school,
            String title,
            String spellPath
    ) {
        map.put(spellPath, new SpellNote(school, title));
    }

    private record Unlock(String path, String title) {
    }

    private record SpellNote(String school, String title) {
    }
}
