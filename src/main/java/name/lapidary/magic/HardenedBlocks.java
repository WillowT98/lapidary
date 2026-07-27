package name.lapidary.magic;

import name.lapidary.network.HardenedBlockSyncPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * First-pass position-based hardened block storage.
 *
 * Server entries remember the block type that was hardened. If that
 * block is replaced, moved, transformed, or destroyed, the ward is discarded
 * instead of silently applying to a later block placed at the same position.
 * Persistence across a full server restart is intentionally left for the
 * later SavedData pass.
 */
public final class HardenedBlocks {
    public static final float MINING_MULTIPLIER = 4.0F;

    private static final Map<ResourceKey<Level>, Map<Long, Block>> SERVER =
            new HashMap<>();
    private static final Map<ResourceLocation, Set<Long>> CLIENT =
            new HashMap<>();

    private HardenedBlocks() {
    }

    public static boolean harden(ServerLevel level, BlockPos pos) {
        Map<Long, Block> positions = SERVER.computeIfAbsent(
                level.dimension(),
                ignored -> new HashMap<>()
        );
        long packed = pos.asLong();
        if (positions.containsKey(packed)) {
            return false;
        }

        positions.put(packed, level.getBlockState(pos).getBlock());
        broadcast(level, pos, true);
        return true;
    }

    public static boolean remove(ServerLevel level, BlockPos pos) {
        Map<Long, Block> positions = SERVER.get(level.dimension());
        boolean removed = positions != null
                && positions.remove(pos.asLong()) != null;
        if (removed) {
            broadcast(level, pos, false);
        }
        return removed;
    }

    public static boolean isHardened(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            Set<Long> positions = CLIENT.get(level.dimension().location());
            return positions != null && positions.contains(pos.asLong());
        }

        Map<Long, Block> positions = SERVER.get(level.dimension());
        if (positions == null) {
            return false;
        }

        Block expected = positions.get(pos.asLong());
        if (expected == null) {
            return false;
        }

        if (level.getBlockState(pos).getBlock() != expected) {
            positions.remove(pos.asLong());
            if (level instanceof ServerLevel serverLevel) {
                broadcast(serverLevel, pos, false);
            }
            return false;
        }

        return true;
    }

    /** Removes wards whose block has been destroyed or replaced. */
    public static void tick(net.minecraft.server.MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            Map<Long, Block> positions = SERVER.get(level.dimension());
            if (positions == null || positions.isEmpty()) {
                continue;
            }

            var iterator = positions.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                BlockPos pos = BlockPos.of(entry.getKey());
                if (level.getBlockState(pos).getBlock() == entry.getValue()) {
                    continue;
                }

                iterator.remove();
                broadcast(level, pos, false);
            }
        }
    }

    public static void syncAll(ServerPlayer player) {
        ResourceLocation dimension = player.level().dimension().location();
        if (!ServerPlayNetworking.canSend(player, HardenedBlockSyncPayload.TYPE)) {
            return;
        }

        ServerPlayNetworking.send(
                player,
                new HardenedBlockSyncPayload(dimension, 0L, false, true)
        );

        Map<Long, Block> positions = SERVER.get(player.level().dimension());
        if (positions == null) {
            return;
        }

        for (long packed : positions.keySet()) {
            ServerPlayNetworking.send(
                    player,
                    new HardenedBlockSyncPayload(dimension, packed, true, false)
            );
        }
    }

    public static void applyClient(HardenedBlockSyncPayload payload) {
        Set<Long> positions = CLIENT.computeIfAbsent(
                payload.dimension(),
                ignored -> new HashSet<>()
        );

        if (payload.clearDimension()) {
            positions.clear();
            return;
        }

        if (payload.hardened()) {
            positions.add(payload.packedPosition());
        } else {
            positions.remove(payload.packedPosition());
        }
    }

    public static void clearClient() {
        CLIENT.clear();
    }

    private static void broadcast(
            ServerLevel level,
            BlockPos pos,
            boolean hardened
    ) {
        HardenedBlockSyncPayload payload = new HardenedBlockSyncPayload(
                level.dimension().location(),
                pos.asLong(),
                hardened,
                false
        );

        for (ServerPlayer player : level.players()) {
            if (ServerPlayNetworking.canSend(player, HardenedBlockSyncPayload.TYPE)) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }
}
