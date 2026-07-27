package name.lapidary.magic.spell;

import name.lapidary.block.ModBlocks;
import name.lapidary.magic.HardenedBlocks;
import name.lapidary.network.RevealOresPayload;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import name.lapidary.tag.ModBlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Shared ticking state for first-pass spells. */
public final class SpellRuntime {
    private static final long SECOND = 20L;
    private static final AttributeModifier AUTO_STEP_MODIFIER =
            new AttributeModifier(
                    name.lapidary.Lapidary.id("spell_auto_step"),
                    0.6D,
                    AttributeModifier.Operation.ADD_VALUE
            );

    private static final Map<UUID, TimedOwnerEffect> APPEASED = new HashMap<>();
    private static final Map<UUID, Long> PACIFIED = new HashMap<>();
    private static final Map<UUID, UUID> CONTROLLED = new HashMap<>();
    private static final Map<UUID, Long> AUTO_STEP = new HashMap<>();
    private static final Map<UUID, Long> ORE_SIGHT = new HashMap<>();
    private static final Map<UUID, Long> HARD_LIGHT = new HashMap<>();
    private static final Map<UUID, Long> FROST_WALKER = new HashMap<>();
    private static final Map<UUID, Long> LAVA_WALKER = new HashMap<>();
    private static final Map<UUID, UUID> BOUND_UNDEAD = new HashMap<>();

    private static boolean initialized;

    private SpellRuntime() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        ServerTickEvents.END_SERVER_TICK.register(SpellRuntime::tick);
        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> HardenedBlocks.syncAll(handler.player)
        );
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(
                (player, origin, destination) -> HardenedBlocks.syncAll(player)
        );
        PlayerBlockBreakEvents.AFTER.register(
                (level, player, pos, state, blockEntity) -> {
                    if (level instanceof ServerLevel serverLevel) {
                        HardenedBlocks.remove(serverLevel, pos);
                    }
                }
        );
        PlayerBlockBreakEvents.BEFORE.register(
                (level, player, pos, state, blockEntity) ->
                        !state.is(ModBlocks.FROSTED_OBSIDIAN)
        );
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(
                (victim, source, amount) -> allowCommandDamage(victim, source.getEntity())
        );
    }

    public static void appease(Mob mob, ServerPlayer owner, long duration) {
        APPEASED.put(
                mob.getUUID(),
                new TimedOwnerEffect(owner.getUUID(), owner.serverLevel().getGameTime() + duration)
        );
        mob.setTarget(null);
    }

    public static void pacify(Mob mob, long duration) {
        PACIFIED.put(mob.getUUID(), mob.level().getGameTime() + duration);
        mob.setTarget(null);
    }

    public static void control(Mob mob, ServerPlayer owner) {
        CONTROLLED.entrySet().removeIf(entry -> entry.getValue().equals(owner.getUUID()));
        CONTROLLED.put(mob.getUUID(), owner.getUUID());
        mob.setPersistenceRequired();
        mob.setTarget(null);
    }

    public static void bindUndead(Mob mob, ServerPlayer owner) {
        BOUND_UNDEAD.put(mob.getUUID(), owner.getUUID());
        mob.setPersistenceRequired();
        mob.setTarget(null);
    }

    public static boolean isControlled(Entity entity) {
        return CONTROLLED.containsKey(entity.getUUID())
                || BOUND_UNDEAD.containsKey(entity.getUUID());
    }

    public static Optional<SkeletonHorse> boundSteed(ServerPlayer owner) {
        for (Map.Entry<UUID, UUID> entry : BOUND_UNDEAD.entrySet()) {
            if (!entry.getValue().equals(owner.getUUID())) {
                continue;
            }
            Mob mob = findMob(owner.serverLevel().getServer(), entry.getKey()).orElse(null);
            if (mob instanceof SkeletonHorse horse && horse.isAlive()) {
                return Optional.of(horse);
            }
        }
        return Optional.empty();
    }

    public static void grantAutoStep(ServerPlayer player, long duration) {
        AUTO_STEP.put(player.getUUID(), player.serverLevel().getGameTime() + duration);
        var instance = player.getAttribute(Attributes.STEP_HEIGHT);
        if (instance != null && !instance.hasModifier(AUTO_STEP_MODIFIER.id())) {
            instance.addTransientModifier(AUTO_STEP_MODIFIER);
        }
    }

    public static void grantOreSight(ServerPlayer player, long duration) {
        ORE_SIGHT.put(
                player.getUUID(),
                player.serverLevel().getGameTime() + duration
        );
        revealOres(player, duration);
    }

    public static void grantHardLight(ServerPlayer player, long duration) {
        HARD_LIGHT.put(
                player.getUUID(),
                player.serverLevel().getGameTime() + duration
        );
        applyHardLight(player);
    }

    public static void grantFrostWalker(ServerPlayer player, long duration) {
        FROST_WALKER.put(player.getUUID(), player.serverLevel().getGameTime() + duration);
    }

    public static void grantLavaWalker(ServerPlayer player, long duration) {
        LAVA_WALKER.put(player.getUUID(), player.serverLevel().getGameTime() + duration);
    }

    private static void tick(MinecraftServer server) {
        long time = server.overworld().getGameTime();
        tickMobStates(server, time);
        tickPlayerStates(server, time);
        if (time % SECOND == 0L) {
            HardenedBlocks.tick(server);
        }
    }

    private static void tickMobStates(MinecraftServer server, long time) {
        Iterator<Map.Entry<UUID, TimedOwnerEffect>> appeasedIterator =
                APPEASED.entrySet().iterator();
        while (appeasedIterator.hasNext()) {
            var entry = appeasedIterator.next();
            if (time >= entry.getValue().expiresAt()) {
                appeasedIterator.remove();
                continue;
            }
            Mob mob = findMob(server, entry.getKey()).orElse(null);
            if (mob == null) {
                continue;
            }
            if (mob.getTarget() != null
                    && mob.getTarget().getUUID().equals(entry.getValue().owner())) {
                mob.setTarget(null);
            }
        }

        PACIFIED.entrySet().removeIf(entry -> {
            if (time >= entry.getValue()) {
                return true;
            }
            findMob(server, entry.getKey()).ifPresent(mob -> mob.setTarget(null));
            return false;
        });

        if (time % SECOND == 0L) {
            tickOwnedMobs(server, CONTROLLED, true);
            tickOwnedMobs(server, BOUND_UNDEAD, false);
        }
    }

    private static void tickOwnedMobs(
            MinecraftServer server,
            Map<UUID, UUID> states,
            boolean damageOwner
    ) {
        Iterator<Map.Entry<UUID, UUID>> iterator = states.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            Mob mob = findMob(server, entry.getKey()).orElse(null);
            ServerPlayer owner = server.getPlayerList().getPlayer(entry.getValue());
            if (mob == null || !mob.isAlive()) {
                iterator.remove();
                continue;
            }
            if (owner == null || !owner.isAlive()) {
                mob.setTarget(null);
                if (damageOwner) {
                    iterator.remove();
                }
                continue;
            }

            if (damageOwner) {
                owner.hurt(owner.damageSources().magic(), 1.0F);
            }

            if (mob.getTarget() == owner) {
                mob.setTarget(null);
            }

            ServerLevel mobLevel = (ServerLevel) mob.level();
            List<Mob> candidates = mobLevel.getEntitiesOfClass(
                    Mob.class,
                    owner.getBoundingBox().inflate(12.0D),
                    candidate -> candidate != mob
                            && candidate instanceof Enemy
                            && !isControlled(candidate)
            );
            Mob nearest = null;
            double nearestDistance = Double.MAX_VALUE;
            for (Mob candidate : candidates) {
                double distance = mob.distanceToSqr(candidate);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = candidate;
                }
            }

            if (nearest != null) {
                mob.setTarget(nearest);
            } else if (mob.distanceToSqr(owner) > 25.0D) {
                mob.getNavigation().moveTo(owner, 1.1D);
            }
        }
    }

    private static void tickPlayerStates(MinecraftServer server, long time) {
        AUTO_STEP.entrySet().removeIf(entry -> {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                return false;
            }
            if (time < entry.getValue()) {
                return false;
            }
            var attribute = player.getAttribute(Attributes.STEP_HEIGHT);
            if (attribute != null) {
                attribute.removeModifier(AUTO_STEP_MODIFIER.id());
            }
            return true;
        });

        HARD_LIGHT.entrySet().removeIf(entry -> {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                return false;
            }
            if (time >= entry.getValue()) {
                return true;
            }
            applyHardLight(player);
            return false;
        });

        if (time % 10L != 0L) {
            return;
        }

        ORE_SIGHT.entrySet().removeIf(entry -> {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                return false;
            }
            long remaining = entry.getValue() - time;
            if (remaining <= 0L) {
                ServerPlayNetworking.send(
                        player,
                        new RevealOresPayload(new long[0], 0)
                );
                return true;
            }
            revealOres(player, remaining);
            return false;
        });

        FROST_WALKER.entrySet().removeIf(entry -> {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                return false;
            }
            if (time >= entry.getValue()) {
                return true;
            }
            applyFrostWalker(player);
            return false;
        });

        LAVA_WALKER.entrySet().removeIf(entry -> {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                return false;
            }
            if (time >= entry.getValue()) {
                return true;
            }
            applyLavaWalker(player);
            return false;
        });
    }

    private static void revealOres(
            ServerPlayer player,
            long remainingTicks
    ) {
        ServerLevel level = player.serverLevel();
        BlockPos center = player.blockPosition();
        int radius = 12;
        List<Long> positions = new java.util.ArrayList<>();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius)
        )) {
            if (center.distSqr(pos) > radius * radius) {
                continue;
            }
            if (level.getBlockState(pos).is(ModBlockTags.ORES)) {
                positions.add(pos.asLong());
            }
        }

        long[] packed = new long[positions.size()];
        for (int index = 0; index < positions.size(); index++) {
            packed[index] = positions.get(index);
        }

        ServerPlayNetworking.send(
                player,
                new RevealOresPayload(
                        packed,
                        (int) Math.min(Integer.MAX_VALUE, remainingTicks)
                )
        );
    }

    private static void applyHardLight(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        /*
         * The player's Y coordinate is the position of their feet. Using the
         * block below blockPosition() ensures the construct is beneath them,
         * rather than appearing inside their legs while jumping or flying.
         */
        BlockPos pos = player.blockPosition().below();
        BlockState state = level.getBlockState(pos);
        if (!state.isAir()) {
            return;
        }

        level.setBlockAndUpdate(
                pos,
                ModBlocks.HARD_LIGHT_BLOCK.defaultBlockState()
        );
        level.scheduleTick(
                pos,
                ModBlocks.HARD_LIGHT_BLOCK,
                20 * 30
        );
    }

    private static void applyFrostWalker(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos center = BlockPos.containing(
                player.getX(),
                player.getY() - 1.0D,
                player.getZ()
        );
        int radius = 3;
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, 0, -radius),
                center.offset(radius, 0, radius)
        )) {
            if (center.distSqr(pos) > radius * radius) {
                continue;
            }
            if (!level.getBlockState(pos).is(Blocks.WATER)
                    || !level.getBlockState(pos.above()).isAir()) {
                continue;
            }
            level.setBlockAndUpdate(pos, Blocks.FROSTED_ICE.defaultBlockState());
            level.scheduleTick(pos, Blocks.FROSTED_ICE, 80);
        }
    }

    private static void applyLavaWalker(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos center = BlockPos.containing(
                player.getX(),
                player.getY() - 1.0D,
                player.getZ()
        );
        int radius = 3;
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, 0, -radius),
                center.offset(radius, 0, radius)
        )) {
            if (center.distSqr(pos) > radius * radius) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (!state.is(Blocks.LAVA)
                    || !state.getFluidState().isSource()
                    || !level.getBlockState(pos.above()).isAir()) {
                continue;
            }

            level.setBlockAndUpdate(
                    pos,
                    ModBlocks.FROSTED_OBSIDIAN.defaultBlockState()
            );
            level.scheduleTick(
                    pos,
                    ModBlocks.FROSTED_OBSIDIAN,
                    60
            );
        }
    }


    /**
     * Cancels attacks which the simple first-pass AI could otherwise perform
     * in the same tick that its target is cleared.
     */
    private static boolean allowCommandDamage(
            net.minecraft.world.entity.LivingEntity victim,
            Entity attacker
    ) {
        if (!(attacker instanceof Mob mob)) {
            return true;
        }

        Long pacifiedUntil = PACIFIED.get(mob.getUUID());
        if (pacifiedUntil != null
                && mob.level().getGameTime() < pacifiedUntil) {
            return false;
        }

        TimedOwnerEffect appeased = APPEASED.get(mob.getUUID());
        if (appeased != null
                && mob.level().getGameTime() < appeased.expiresAt()
                && victim.getUUID().equals(appeased.owner())) {
            return false;
        }

        UUID controller = CONTROLLED.get(mob.getUUID());
        if (controller == null) {
            controller = BOUND_UNDEAD.get(mob.getUUID());
        }
        if (controller == null) {
            return true;
        }

        /*
         * Controlled and bound undead are friendly creatures, not merely mobs
         * which avoid their owner. Their first-pass attacks are therefore
         * restricted to uncontrolled hostile mobs.
         */
        return victim instanceof Enemy && !isControlled(victim);
    }

    private static Optional<Mob> findMob(MinecraftServer server, UUID uuid) {
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(uuid);
            if (entity instanceof Mob mob) {
                return Optional.of(mob);
            }
        }
        return Optional.empty();
    }

    private record TimedOwnerEffect(UUID owner, long expiresAt) {
    }

}
