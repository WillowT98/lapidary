package name.lapidary.magic.spell;

import name.lapidary.block.ModBlocks;
import name.lapidary.magic.HardenedBlocks;
import name.lapidary.mixin.ZombieVillagerInvoker;
import name.lapidary.tag.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** First-pass implementations for the agreed ordinary spells. */
public final class FirstPassSpells {
    private static final double BLOCK_RANGE = 8.0D;
    private static final double ENTITY_RANGE = 12.0D;

    private static final Map<Block, Block> COPPER_PREVIOUS = copperPrevious();
    private static final Map<Block, Block> CHISEL_NEXT = chiselCycle();
    private static final Map<Item, Recomposition> RECOMPOSITION = recomposition();
    private static final List<Block> FLOWERS = List.of(
            Blocks.DANDELION,
            Blocks.POPPY,
            Blocks.BLUE_ORCHID,
            Blocks.ALLIUM,
            Blocks.AZURE_BLUET,
            Blocks.RED_TULIP,
            Blocks.ORANGE_TULIP,
            Blocks.WHITE_TULIP,
            Blocks.PINK_TULIP,
            Blocks.OXEYE_DAISY,
            Blocks.CORNFLOWER,
            Blocks.LILY_OF_THE_VALLEY,
            Blocks.TORCHFLOWER
    );

    private FirstPassSpells() {
    }

    // ---------------------------------------------------------------------
    // Summoning
    // ---------------------------------------------------------------------

    public static boolean canSummon(SpellCastContext context) {
        return SpellTargeting.placementPosition(context.caster(), BLOCK_RANGE)
                .filter(pos -> context.caster().serverLevel()
                        .getBlockState(pos).canBeReplaced())
                .isPresent();
    }

    public static void summonChicken(SpellCastContext context) {
        summon(context, EntityType.CHICKEN);
    }

    public static void summonCow(SpellCastContext context) {
        summon(context, EntityType.COW);
    }

    public static void summonWolf(SpellCastContext context) {
        summon(context, EntityType.WOLF);
    }

    public static void summonHorse(SpellCastContext context) {
        summon(context, EntityType.HORSE);
    }

    private static <T extends Mob> void summon(
            SpellCastContext context,
            EntityType<T> type
    ) {
        ServerPlayer caster = context.caster();
        ServerLevel level = caster.serverLevel();
        Optional<BlockPos> position = SpellTargeting.placementPosition(
                caster,
                BLOCK_RANGE
        );
        if (position.isEmpty()) {
            return;
        }

        T mob = type.create(level);
        if (mob == null) {
            return;
        }
        BlockPos pos = position.get();
        mob.moveTo(
                pos.getX() + 0.5D,
                pos.getY(),
                pos.getZ() + 0.5D,
                caster.getYRot(),
                0.0F
        );
        if (!level.noCollision(mob)) {
            mob.discard();
            return;
        }
        mob.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(pos),
                MobSpawnType.MOB_SUMMONED,
                null
        );
        mob.setPersistenceRequired();
        level.addFreshEntity(mob);
        level.sendParticles(
                ParticleTypes.PORTAL,
                mob.getX(),
                mob.getY() + 0.5D,
                mob.getZ(),
                30,
                0.5D,
                0.5D,
                0.5D,
                0.1D
        );
    }

    // ---------------------------------------------------------------------
    // Command
    // ---------------------------------------------------------------------

    public static boolean canDisarm(SpellCastContext context) {
        return targetMob(context)
                .filter(mob -> !mob.getMainHandItem().isEmpty())
                .isPresent();
    }

    public static void disarm(SpellCastContext context) {
        targetMob(context).ifPresent(mob -> {
            ItemStack held = mob.getMainHandItem().copy();
            mob.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            mob.spawnAtLocation(held);
            mob.level().playSound(
                    null,
                    mob.blockPosition(),
                    SoundEvents.ITEM_PICKUP,
                    SoundSource.HOSTILE,
                    1.0F,
                    0.8F
            );
        });
    }

    public static boolean canCommandHostile(SpellCastContext context) {
        return targetHostile(context).isPresent();
    }

    public static void appease(SpellCastContext context) {
        targetHostile(context).ifPresent(mob ->
                SpellRuntime.appease(mob, context.caster(), 20L * 60L)
        );
    }

    public static void pacify(SpellCastContext context) {
        targetHostile(context).ifPresent(mob ->
                SpellRuntime.pacify(mob, 20L * 30L)
        );
    }

    public static void controlHostile(SpellCastContext context) {
        targetHostile(context).ifPresent(mob ->
                SpellRuntime.control(mob, context.caster())
        );
    }

    // ---------------------------------------------------------------------
    // Nature
    // ---------------------------------------------------------------------

    public static boolean canAccelerateGrowth(SpellCastContext context) {
        BlockPos center = context.caster().blockPosition();
        ServerLevel level = context.caster().serverLevel();
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-4, -2, -4),
                center.offset(4, 2, 4)
        )) {
            if (level.getBlockState(pos).getBlock() instanceof BonemealableBlock) {
                return true;
            }
        }
        return false;
    }

    public static void accelerateGrowth(SpellCastContext context) {
        ServerLevel level = context.caster().serverLevel();
        BlockPos center = context.caster().blockPosition();
        List<BlockPos> candidates = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-4, -2, -4),
                center.offset(4, 2, 4)
        )) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof BonemealableBlock growable
                    && growable.isValidBonemealTarget(level, pos, state)) {
                candidates.add(pos.immutable());
            }
        }

        RandomSource random = level.getRandom();
        for (int index = 0; index < Math.min(6, candidates.size()); index++) {
            BlockPos pos = candidates.remove(random.nextInt(candidates.size()));
            BlockState state = level.getBlockState(pos);
            BonemealableBlock growable = (BonemealableBlock) state.getBlock();
            if (growable.isBonemealSuccess(level, random, pos, state)) {
                growable.performBonemeal(level, random, pos, state);
                level.sendParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        pos.getX() + 0.5D,
                        pos.getY() + 0.5D,
                        pos.getZ() + 0.5D,
                        4,
                        0.3D,
                        0.3D,
                        0.3D,
                        0.0D
                );
            }
        }
    }

    public static boolean canFlowerField(SpellCastContext context) {
        return SpellTargeting.block(context.caster(), BLOCK_RANGE, false)
                .isPresent();
    }

    public static void flowerField(SpellCastContext context) {
        ServerLevel level = context.caster().serverLevel();
        BlockPos center = SpellTargeting.block(
                context.caster(),
                BLOCK_RANGE,
                false
        ).map(BlockHitResult::getBlockPos).orElse(context.caster().blockPosition());
        RandomSource random = level.getRandom();
        int radius = 6;

        for (BlockPos cursor : BlockPos.betweenClosed(
                center.offset(-radius, -2, -radius),
                center.offset(radius, 2, radius)
        )) {
            if (center.distSqr(cursor) > radius * radius) {
                continue;
            }
            BlockPos ground = cursor;
            BlockPos above = ground.above();
            if (!level.getBlockState(above).canBeReplaced()
                    || random.nextFloat() > 0.22F) {
                continue;
            }
            Block flower = FLOWERS.get(random.nextInt(FLOWERS.size()));
            BlockState flowerState = flower.defaultBlockState();
            if (flowerState.canSurvive(level, above)) {
                level.setBlock(above, flowerState, 3);
            }
        }
    }

    public static boolean canBigTree(SpellCastContext context) {
        Optional<BlockHitResult> hit = SpellTargeting.block(
                context.caster(),
                BLOCK_RANGE,
                false
        );
        if (hit.isEmpty()) {
            return false;
        }
        BlockPos base = hit.get().getBlockPos().above();
        ServerLevel level = context.caster().serverLevel();
        for (int y = 0; y < 18; y++) {
            BlockState state = level.getBlockState(base.above(y));
            if (!isTreeReplaceable(state)) {
                return false;
            }
        }
        return true;
    }

    public static void bigTree(SpellCastContext context) {
        ServerLevel level = context.caster().serverLevel();
        BlockPos base = SpellTargeting.block(
                context.caster(),
                BLOCK_RANGE,
                false
        ).orElseThrow().getBlockPos().above();
        RandomSource random = level.getRandom();
        int height = Mth.nextInt(random, 13, 17);

        for (int y = 0; y < height; y++) {
            int driftX = y / 7;
            int driftZ = y / 9;
            placeTreeBlock(level, base.offset(driftX, y, driftZ), Blocks.OAK_LOG.defaultBlockState());
            if (y < height / 2) {
                placeTreeBlock(level, base.offset(driftX + 1, y, driftZ), Blocks.OAK_LOG.defaultBlockState());
                placeTreeBlock(level, base.offset(driftX, y, driftZ + 1), Blocks.OAK_LOG.defaultBlockState());
            }
        }

        BlockPos crown = base.offset(height / 7, height - 2, height / 9);
        Direction[] directions = {
                Direction.NORTH,
                Direction.SOUTH,
                Direction.EAST,
                Direction.WEST
        };
        for (Direction direction : directions) {
            int length = Mth.nextInt(random, 4, 7);
            BlockPos branch = crown.below(random.nextInt(4));
            for (int step = 1; step <= length; step++) {
                BlockPos pos = branch.relative(direction, step).above(step / 3);
                placeTreeBlock(level, pos, Blocks.OAK_LOG.defaultBlockState());
            }
            BlockPos tip = branch.relative(direction, length).above(length / 3);
            leafSphere(level, tip, 3);
        }
        leafSphere(level, crown.above(2), 4);
    }

    // ---------------------------------------------------------------------
    // Transmutation
    // ---------------------------------------------------------------------

    public static boolean canUnoxidize(SpellCastContext context) {
        ItemStack stack = context.caster().getOffhandItem();
        return stack.getItem() instanceof BlockItem blockItem
                && COPPER_PREVIOUS.containsKey(blockItem.getBlock());
    }

    public static void unoxidize(SpellCastContext context) {
        ServerPlayer player = context.caster();
        ItemStack stack = player.getOffhandItem();
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return;
        }

        Block previous = COPPER_PREVIOUS.get(blockItem.getBlock());
        if (previous == null) {
            return;
        }

        replaceOffhandBlockStack(player, stack, previous);
        player.serverLevel().sendParticles(
                ParticleTypes.WAX_OFF,
                player.getX(),
                player.getEyeY() - 0.4D,
                player.getZ(),
                8,
                0.25D,
                0.25D,
                0.25D,
                0.0D
        );
    }

    public static boolean canRepair(SpellCastContext context) {
        ItemStack stack = context.caster().getOffhandItem();
        return stack.isDamageableItem() && stack.isDamaged();
    }

    public static void repair(SpellCastContext context) {
        ItemStack stack = context.caster().getOffhandItem();
        if (stack.isDamageableItem() && stack.isDamaged()) {
            stack.setDamageValue(Math.max(0, stack.getDamageValue() - 8));
            context.caster().serverLevel().sendParticles(
                    ParticleTypes.ENCHANT,
                    context.caster().getX(),
                    context.caster().getEyeY() - 0.4D,
                    context.caster().getZ(),
                    6,
                    0.3D,
                    0.3D,
                    0.3D,
                    0.0D
            );
        }
    }

    public static boolean canChisel(SpellCastContext context) {
        ItemStack stack = context.caster().getOffhandItem();
        return stack.getItem() instanceof BlockItem blockItem
                && CHISEL_NEXT.containsKey(blockItem.getBlock());
    }

    public static void chisel(SpellCastContext context) {
        ServerPlayer player = context.caster();
        ItemStack stack = player.getOffhandItem();
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return;
        }

        Block next = CHISEL_NEXT.get(blockItem.getBlock());
        if (next == null) {
            return;
        }

        replaceOffhandBlockStack(player, stack, next);
        player.serverLevel().playSound(
                null,
                player.blockPosition(),
                SoundEvents.UI_STONECUTTER_SELECT_RECIPE,
                SoundSource.PLAYERS,
                0.65F,
                1.0F
        );
    }

    public static boolean canRecompose(SpellCastContext context) {
        Item source = context.caster().getOffhandItem().getItem();
        Recomposition recipe = RECOMPOSITION.get(source);
        return recipe != null
                && countItem(context.caster(), source) >= recipe.inputCount()
                && canStoreOutputAfterConsuming(
                        context.caster(),
                        source,
                        recipe.inputCount(),
                        new ItemStack(
                                recipe.output(),
                                recipe.outputCount()
                        )
                );
    }

    public static void recompose(SpellCastContext context) {
        ServerPlayer player = context.caster();
        Item source = player.getOffhandItem().getItem();
        Recomposition recipe = RECOMPOSITION.get(source);
        ItemStack output = recipe == null
                ? ItemStack.EMPTY
                : new ItemStack(recipe.output(), recipe.outputCount());
        if (recipe == null
                || countItem(player, source) < recipe.inputCount()
                || !canStoreOutputAfterConsuming(
                        player,
                        source,
                        recipe.inputCount(),
                        output
                )) {
            return;
        }
        consumeItem(player, source, recipe.inputCount());
        if (player.getOffhandItem().isEmpty()) {
            player.setItemInHand(
                    InteractionHand.OFF_HAND,
                    output
            );
        } else {
            player.addItem(output);
        }
    }

    // ---------------------------------------------------------------------
    // Warding
    // ---------------------------------------------------------------------

    public static boolean canHardenBlock(SpellCastContext context) {
        Optional<BlockHitResult> hit = SpellTargeting.block(
                context.caster(),
                BLOCK_RANGE,
                false
        );
        return hit.isPresent()
                && !context.caster().serverLevel().getBlockState(hit.get().getBlockPos()).isAir()
                && context.caster().serverLevel().getBlockEntity(hit.get().getBlockPos()) == null
                && !HardenedBlocks.isHardened(
                context.caster().serverLevel(),
                hit.get().getBlockPos()
        );
    }

    public static void hardenBlock(SpellCastContext context) {
        SpellTargeting.block(context.caster(), BLOCK_RANGE, false).ifPresent(hit ->
                HardenedBlocks.harden(
                        context.caster().serverLevel(),
                        hit.getBlockPos()
                )
        );
    }

    public static boolean canHardenGlass(SpellCastContext context) {
        return targetedBlock(context).filter(state -> state.is(Blocks.GLASS)).isPresent();
    }

    public static void hardenGlass(SpellCastContext context) {
        SpellTargeting.block(context.caster(), BLOCK_RANGE, false).ifPresent(hit ->
                context.caster().serverLevel().setBlockAndUpdate(
                        hit.getBlockPos(),
                        ModBlocks.REINFORCED_GLASS.defaultBlockState()
                )
        );
    }

    public static void fireProtection(SpellCastContext context) {
        context.caster().addEffect(
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 60 * 5, 0)
        );
    }

    // ---------------------------------------------------------------------
    // Divination
    // ---------------------------------------------------------------------

    public static void revealMobs(SpellCastContext context) {
        ServerPlayer caster = context.caster();
        for (Mob mob : caster.serverLevel().getEntitiesOfClass(
                Mob.class,
                caster.getBoundingBox().inflate(32.0D),
                Entity::isAlive
        )) {
            mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 30, 0));
        }
    }

    public static void revealOres(SpellCastContext context) {
        SpellRuntime.grantOreSight(context.caster(), 20L * 20L);
    }

    public static void nightVision(SpellCastContext context) {
        context.caster().addEffect(
                new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 60 * 8, 0)
        );
    }

    // ---------------------------------------------------------------------
    // Passage
    // ---------------------------------------------------------------------

    public static void speed(SpellCastContext context) {
        context.caster().addEffect(
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 60 * 3, 1)
        );
    }

    public static void autoStep(SpellCastContext context) {
        SpellRuntime.grantAutoStep(context.caster(), 20L * 60L * 3L);
    }

    public static boolean canBlink(SpellCastContext context) {
        return findBlinkDestination(context.caster()).isPresent();
    }

    public static void blink(SpellCastContext context) {
        findBlinkDestination(context.caster()).ifPresent(destination -> {
            ServerPlayer player = context.caster();
            player.teleportTo(
                    player.serverLevel(),
                    destination.x,
                    destination.y,
                    destination.z,
                    player.getYRot(),
                    player.getXRot()
            );
            player.resetFallDistance();
        });
    }

    public static void frostWalker(SpellCastContext context) {
        SpellRuntime.grantFrostWalker(context.caster(), 20L * 60L * 2L);
    }

    public static void lavaWalker(SpellCastContext context) {
        SpellRuntime.grantLavaWalker(context.caster(), 20L * 60L * 2L);
    }

    // ---------------------------------------------------------------------
    // Illusion
    // ---------------------------------------------------------------------

    public static void invisibility(SpellCastContext context) {
        context.caster().addEffect(
                new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 60 * 3, 0)
        );
    }

    public static boolean canHardLight(SpellCastContext context) {
        return true;
    }

    public static void hardLight(SpellCastContext context) {
        /*
         * Hard Light is a temporary trail effect: while active, the runtime
         * creates a narrow platform directly beneath the caster as they move.
         */
        SpellRuntime.grantHardLight(context.caster(), 20L * 60L);
    }

    // ---------------------------------------------------------------------
    // Necromancy
    // ---------------------------------------------------------------------

    public static void summonSkeleton(SpellCastContext context) {
        ServerPlayer caster = context.caster();
        Optional<BlockPos> position = SpellTargeting.placementPosition(caster, BLOCK_RANGE);
        if (position.isEmpty()) {
            return;
        }
        Skeleton skeleton = EntityType.SKELETON.create(caster.serverLevel());
        if (skeleton == null) {
            return;
        }
        BlockPos pos = position.get();
        skeleton.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, caster.getYRot(), 0.0F);
        if (!caster.serverLevel().noCollision(skeleton)) {
            skeleton.discard();
            return;
        }
        skeleton.finalizeSpawn(
                caster.serverLevel(),
                caster.serverLevel().getCurrentDifficultyAt(pos),
                MobSpawnType.MOB_SUMMONED,
                null
        );
        skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        skeleton.setPersistenceRequired();
        caster.serverLevel().addFreshEntity(skeleton);
        SpellRuntime.bindUndead(skeleton, caster);
    }

    public static boolean canFlense(SpellCastContext context) {
        return SpellTargeting.entity(
                context.caster(),
                ENTITY_RANGE,
                Zombie.class,
                zombie -> !(zombie instanceof ZombieVillager)
        ).isPresent();
    }

    public static void flense(SpellCastContext context) {
        SpellTargeting.entity(
                context.caster(),
                ENTITY_RANGE,
                Zombie.class,
                zombie -> !(zombie instanceof ZombieVillager)
        ).ifPresent(zombie -> {
            ServerLevel level = (ServerLevel) zombie.level();
            Skeleton skeleton = EntityType.SKELETON.create(level);
            if (skeleton == null) {
                return;
            }
            copyMobPositionAndEquipment(zombie, skeleton);
            level.addFreshEntity(skeleton);
            zombie.spawnAtLocation(new ItemStack(Items.ROTTEN_FLESH, 3));
            zombie.discard();
        });
    }

    public static boolean canCleanseVillager(SpellCastContext context) {
        return SpellTargeting.entity(
                context.caster(),
                ENTITY_RANGE,
                ZombieVillager.class,
                Entity::isAlive
        ).isPresent();
    }

    public static void cleanseVillager(SpellCastContext context) {
        SpellTargeting.entity(
                context.caster(),
                ENTITY_RANGE,
                ZombieVillager.class,
                Entity::isAlive
        ).ifPresent(villager ->
                ((ZombieVillagerInvoker) villager)
                        .lapidary$startConverting(
                                context.caster().getUUID(),
                                1
                        )
        );
    }

    public static boolean canChangeUndead(SpellCastContext context) {
        return SpellTargeting.entity(
                context.caster(),
                ENTITY_RANGE,
                Mob.class,
                FirstPassSpells::isConvertibleUndead
        ).isPresent();
    }

    public static void changeUndead(SpellCastContext context) {
        SpellTargeting.entity(
                context.caster(),
                ENTITY_RANGE,
                Mob.class,
                FirstPassSpells::isConvertibleUndead
        ).ifPresent(oldMob -> {
            ServerLevel level = (ServerLevel) oldMob.level();
            EntityType<? extends Mob> nextType = nextUndeadType(oldMob);
            Mob replacement = nextType.create(level);
            if (replacement == null) {
                return;
            }
            copyMobPositionAndEquipment(oldMob, replacement);
            level.addFreshEntity(replacement);
            oldMob.discard();
        });
    }

    public static void skeletonSteed(SpellCastContext context) {
        ServerPlayer caster = context.caster();
        Optional<BlockPos> position = SpellTargeting.placementPosition(caster, BLOCK_RANGE);
        if (position.isEmpty()) {
            return;
        }
        BlockPos pos = position.get();

        Optional<SkeletonHorse> existingSteed = SpellRuntime.boundSteed(caster);
        if (existingSteed.isPresent()) {
            SkeletonHorse horse = existingSteed.get();
            double oldX = horse.getX();
            double oldY = horse.getY();
            double oldZ = horse.getZ();
            float oldYaw = horse.getYRot();
            float oldPitch = horse.getXRot();
            horse.stopRiding();
            horse.moveTo(
                    pos.getX() + 0.5D,
                    pos.getY(),
                    pos.getZ() + 0.5D,
                    caster.getYRot(),
                    0.0F
            );
            if (!caster.serverLevel().noCollision(horse)) {
                horse.moveTo(oldX, oldY, oldZ, oldYaw, oldPitch);
                return;
            }
            horse.resetFallDistance();
            caster.serverLevel().sendParticles(
                    ParticleTypes.SOUL,
                    horse.getX(),
                    horse.getY() + 0.5D,
                    horse.getZ(),
                    24,
                    0.5D,
                    0.5D,
                    0.5D,
                    0.02D
            );
            return;
        }

        SkeletonHorse horse = EntityType.SKELETON_HORSE.create(caster.serverLevel());
        if (horse == null) {
            return;
        }
        horse.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, caster.getYRot(), 0.0F);
        if (!caster.serverLevel().noCollision(horse)) {
            horse.discard();
            return;
        }
        horse.finalizeSpawn(
                caster.serverLevel(),
                caster.serverLevel().getCurrentDifficultyAt(pos),
                MobSpawnType.MOB_SUMMONED,
                null
        );
        horse.setTamed(true);
        horse.setOwnerUUID(caster.getUUID());
        horse.setPersistenceRequired();
        setBaseValue(horse, Attributes.MAX_HEALTH, 40.0D);
        setBaseValue(horse, Attributes.MOVEMENT_SPEED, 0.32D);
        setBaseValue(horse, Attributes.JUMP_STRENGTH, 0.9D);
        horse.setHealth(horse.getMaxHealth());
        caster.serverLevel().addFreshEntity(horse);
        SpellRuntime.bindUndead(horse, caster);
    }

    // ---------------------------------------------------------------------
    // Griefing
    // ---------------------------------------------------------------------

    public static void ghastFireball(SpellCastContext context) {
        ServerPlayer caster = context.caster();
        Vec3 direction = caster.getLookAngle().normalize();
        LargeFireball fireball = new LargeFireball(
                caster.serverLevel(),
                caster,
                direction,
                1
        );
        Vec3 spawn = caster.getEyePosition().add(direction.scale(1.0D));
        fireball.setPos(spawn.x, spawn.y, spawn.z);
        caster.serverLevel().addFreshEntity(fireball);
    }

    public static boolean canTnt(SpellCastContext context) {
        return SpellTargeting.placementPosition(context.caster(), BLOCK_RANGE)
                .filter(pos -> context.caster().serverLevel()
                        .getBlockState(pos).canBeReplaced())
                .isPresent();
    }

    public static void litTnt(SpellCastContext context) {
        SpellTargeting.placementPosition(context.caster(), BLOCK_RANGE).ifPresent(pos -> {
            PrimedTnt tnt = new PrimedTnt(
                    context.caster().serverLevel(),
                    pos.getX() + 0.5D,
                    pos.getY(),
                    pos.getZ() + 0.5D,
                    context.caster()
            );
            context.caster().serverLevel().addFreshEntity(tnt);
        });
    }

    public static boolean canEraseMatching(SpellCastContext context) {
        ServerLevel level = context.caster().serverLevel();
        return SpellTargeting.block(context.caster(), BLOCK_RANGE, false)
                .filter(hit -> {
                    BlockPos pos = hit.getBlockPos();
                    BlockState state = level.getBlockState(pos);
                    return !state.isAir()
                            && level.getBlockEntity(pos) == null
                            && !state.is(BlockTags.WITHER_IMMUNE);
                })
                .isPresent();
    }

    public static void eraseMatching(SpellCastContext context) {
        ServerLevel level = context.caster().serverLevel();
        BlockHitResult hit = SpellTargeting.block(
                context.caster(),
                BLOCK_RANGE,
                false
        ).orElseThrow();
        BlockPos center = hit.getBlockPos();
        Block target = level.getBlockState(center).getBlock();
        int radius = 4;

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius)
        )) {
            if (center.distSqr(pos) > radius * radius) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (!state.is(target)
                    || level.getBlockEntity(pos) != null
                    || state.is(BlockTags.WITHER_IMMUNE)) {
                continue;
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            level.sendParticles(
                    ParticleTypes.POOF,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D,
                    2,
                    0.2D,
                    0.2D,
                    0.2D,
                    0.0D
            );
        }
    }

    public static boolean canGravity(SpellCastContext context) {
        Optional<BlockHitResult> hit = SpellTargeting.block(
                context.caster(),
                BLOCK_RANGE,
                false
        );
        if (hit.isEmpty()) {
            return false;
        }

        ServerLevel level = context.caster().serverLevel();
        BlockPos center = hit.get().getBlockPos();
        int radius = 4;
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius)
        )) {
            if (center.distSqr(pos) > radius * radius) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (state.is(ModBlockTags.GRAVITY_AFFECTED)
                    && level.getBlockEntity(pos) == null
                    && !state.isAir()) {
                return true;
            }
        }
        return false;
    }

    public static void gravity(SpellCastContext context) {
        ServerLevel level = context.caster().serverLevel();
        BlockPos center = SpellTargeting.block(
                context.caster(),
                BLOCK_RANGE,
                false
        ).orElseThrow().getBlockPos();
        int radius = 4;
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius)
        )) {
            if (center.distSqr(pos) > radius * radius) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (!state.is(ModBlockTags.GRAVITY_AFFECTED)
                    || level.getBlockEntity(pos) != null
                    || state.isAir()) {
                continue;
            }
            FallingBlockEntity falling = FallingBlockEntity.fall(level, pos, state);
            falling.dropItem = false;
        }
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private static Optional<Mob> targetMob(SpellCastContext context) {
        return SpellTargeting.entity(
                context.caster(),
                ENTITY_RANGE,
                Mob.class,
                Entity::isAlive
        );
    }

    private static Optional<Mob> targetHostile(SpellCastContext context) {
        return SpellTargeting.entity(
                context.caster(),
                ENTITY_RANGE,
                Mob.class,
                mob -> mob instanceof Enemy
        );
    }

    private static Optional<BlockState> targetedBlock(SpellCastContext context) {
        return SpellTargeting.block(context.caster(), BLOCK_RANGE, false)
                .map(hit -> context.caster().serverLevel()
                        .getBlockState(hit.getBlockPos()));
    }

    private static Optional<Vec3> findBlinkDestination(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 start = player.position();
        Vec3 direction = player.getLookAngle().normalize();
        for (double distance = 8.0D; distance >= 1.0D; distance -= 0.5D) {
            Vec3 candidate = start.add(direction.scale(distance));
            BlockPos feet = BlockPos.containing(candidate);
            if (!level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()) {
                continue;
            }
            if (!level.getBlockState(feet.above())
                    .getCollisionShape(level, feet.above()).isEmpty()) {
                continue;
            }
            if (level.getBlockState(feet.below())
                    .getCollisionShape(level, feet.below()).isEmpty()) {
                continue;
            }
            return Optional.of(new Vec3(
                    feet.getX() + 0.5D,
                    feet.getY(),
                    feet.getZ() + 0.5D
            ));
        }
        return Optional.empty();
    }

    private static boolean isTreeReplaceable(BlockState state) {
        return state.canBeReplaced()
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.SAPLINGS);
    }

    private static void placeTreeBlock(
            ServerLevel level,
            BlockPos pos,
            BlockState state
    ) {
        if (isTreeReplaceable(level.getBlockState(pos))) {
            level.setBlock(pos, state, 3);
        }
    }

    private static void leafSphere(ServerLevel level, BlockPos center, int radius) {
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius)
        )) {
            if (center.distSqr(pos) <= radius * radius
                    && isTreeReplaceable(level.getBlockState(pos))) {
                level.setBlock(pos, Blocks.OAK_LEAVES.defaultBlockState(), 3);
            }
        }
    }

    private static void replaceOffhandBlockStack(
            ServerPlayer player,
            ItemStack original,
            Block replacement
    ) {
        ItemStack converted = new ItemStack(
                replacement.asItem(),
                original.getCount()
        );
        player.setItemInHand(
                InteractionHand.OFF_HAND,
                converted
        );
    }

    private static boolean canStoreOutputAfterConsuming(
            ServerPlayer player,
            Item source,
            int sourceCount,
            ItemStack output
    ) {
        /*
         * The offhand selects the recipe and also counts as inventory. If the
         * conversion empties it, the result can occupy that slot directly.
         */
        ItemStack offhand = player.getOffhandItem();
        int remainingToConsume = sourceCount;
        if (offhand.is(source)) {
            int removed = Math.min(
                    remainingToConsume,
                    offhand.getCount()
            );
            remainingToConsume -= removed;
            if (removed == offhand.getCount()) {
                return true;
            }
        }

        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            ItemStack existing = player.getInventory().getItem(slot);
            if (existing.isEmpty()) {
                return true;
            }
            if (ItemStack.isSameItemSameComponents(existing, output)
                    && existing.getCount() + output.getCount()
                    <= existing.getMaxStackSize()) {
                return true;
            }
            if (remainingToConsume <= 0 || !existing.is(source)) {
                continue;
            }

            int removed = Math.min(
                    remainingToConsume,
                    existing.getCount()
            );
            remainingToConsume -= removed;
            if (removed == existing.getCount()) {
                return true;
            }
        }
        return false;
    }

    private static int countItem(ServerPlayer player, Item item) {
        int count = player.getOffhandItem().is(item)
                ? player.getOffhandItem().getCount()
                : 0;
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void consumeItem(ServerPlayer player, Item item, int count) {
        int remaining = count;
        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(item)) {
            int removed = Math.min(
                    remaining,
                    offhand.getCount()
            );
            offhand.shrink(removed);
            remaining -= removed;
        }

        for (int slot = 0;
             slot < Inventory.INVENTORY_SIZE && remaining > 0;
             slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(item)) {
                continue;
            }
            int removed = Math.min(remaining, stack.getCount());
            stack.shrink(removed);
            remaining -= removed;
        }
    }

    private static boolean isConvertibleUndead(Mob mob) {
        return mob instanceof Zombie
                || mob instanceof Skeleton
                || mob instanceof Stray
                || mob instanceof Husk
                || mob instanceof Drowned;
    }

    private static EntityType<? extends Mob> nextUndeadType(Mob mob) {
        if (mob instanceof Husk) {
            return EntityType.DROWNED;
        }
        if (mob instanceof Drowned) {
            return EntityType.ZOMBIE;
        }
        if (mob instanceof Zombie) {
            return EntityType.SKELETON;
        }
        if (mob instanceof Stray) {
            return EntityType.HUSK;
        }
        return EntityType.STRAY;
    }

    private static void copyMobPositionAndEquipment(Mob oldMob, Mob newMob) {
        newMob.moveTo(
                oldMob.getX(),
                oldMob.getY(),
                oldMob.getZ(),
                oldMob.getYRot(),
                oldMob.getXRot()
        );
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = oldMob.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                newMob.setItemSlot(slot, stack.copy());
            }
        }
        if (oldMob.hasCustomName()) {
            newMob.setCustomName(oldMob.getCustomName());
            newMob.setCustomNameVisible(oldMob.isCustomNameVisible());
        }
        newMob.setPersistenceRequired();
    }

    private static void setBaseValue(Mob mob, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, double value) {
        var instance = mob.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    private static Map<Item, Recomposition> recomposition() {
        Map<Item, Recomposition> map = new LinkedHashMap<>();
        map.put(Items.COAL, new Recomposition(Items.RAW_COPPER, 10, 1));
        map.put(Items.RAW_COPPER, new Recomposition(Items.RAW_IRON, 10, 1));
        map.put(Items.RAW_IRON, new Recomposition(Items.LAPIS_LAZULI, 10, 1));
        map.put(Items.LAPIS_LAZULI, new Recomposition(Items.REDSTONE, 10, 1));
        map.put(Items.REDSTONE, new Recomposition(Items.DIAMOND, 10, 1));
        map.put(Items.DIAMOND, new Recomposition(Items.EMERALD, 10, 1));
        return Map.copyOf(map);
    }

    private static Map<Block, Block> chiselCycle() {
        Map<Block, Block> map = new LinkedHashMap<>();
        cycle(map, Blocks.STONE, Blocks.ANDESITE, Blocks.DIORITE, Blocks.GRANITE);
        cycle(map, Blocks.COBBLESTONE, Blocks.STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS);
        cycle(map, Blocks.DEEPSLATE, Blocks.COBBLED_DEEPSLATE, Blocks.POLISHED_DEEPSLATE, Blocks.DEEPSLATE_BRICKS, Blocks.DEEPSLATE_TILES);
        cycle(map, Blocks.SANDSTONE, Blocks.CUT_SANDSTONE, Blocks.SMOOTH_SANDSTONE);
        cycle(map, Blocks.RED_SANDSTONE, Blocks.CUT_RED_SANDSTONE, Blocks.SMOOTH_RED_SANDSTONE);
        return Map.copyOf(map);
    }

    private static void cycle(Map<Block, Block> map, Block... blocks) {
        for (int index = 0; index < blocks.length; index++) {
            map.put(blocks[index], blocks[(index + 1) % blocks.length]);
        }
    }

    private static Map<Block, Block> copperPrevious() {
        Map<Block, Block> map = new LinkedHashMap<>();
        previous(map, Blocks.EXPOSED_COPPER, Blocks.COPPER_BLOCK);
        previous(map, Blocks.WEATHERED_COPPER, Blocks.EXPOSED_COPPER);
        previous(map, Blocks.OXIDIZED_COPPER, Blocks.WEATHERED_COPPER);
        previous(map, Blocks.EXPOSED_CUT_COPPER, Blocks.CUT_COPPER);
        previous(map, Blocks.WEATHERED_CUT_COPPER, Blocks.EXPOSED_CUT_COPPER);
        previous(map, Blocks.OXIDIZED_CUT_COPPER, Blocks.WEATHERED_CUT_COPPER);
        previous(map, Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.CUT_COPPER_STAIRS);
        previous(map, Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.EXPOSED_CUT_COPPER_STAIRS);
        previous(map, Blocks.OXIDIZED_CUT_COPPER_STAIRS, Blocks.WEATHERED_CUT_COPPER_STAIRS);
        previous(map, Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.CUT_COPPER_SLAB);
        previous(map, Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER_SLAB);
        previous(map, Blocks.OXIDIZED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER_SLAB);
        previous(map, Blocks.EXPOSED_COPPER_DOOR, Blocks.COPPER_DOOR);
        previous(map, Blocks.WEATHERED_COPPER_DOOR, Blocks.EXPOSED_COPPER_DOOR);
        previous(map, Blocks.OXIDIZED_COPPER_DOOR, Blocks.WEATHERED_COPPER_DOOR);
        previous(map, Blocks.EXPOSED_COPPER_TRAPDOOR, Blocks.COPPER_TRAPDOOR);
        previous(map, Blocks.WEATHERED_COPPER_TRAPDOOR, Blocks.EXPOSED_COPPER_TRAPDOOR);
        previous(map, Blocks.OXIDIZED_COPPER_TRAPDOOR, Blocks.WEATHERED_COPPER_TRAPDOOR);
        previous(map, Blocks.EXPOSED_COPPER_GRATE, Blocks.COPPER_GRATE);
        previous(map, Blocks.WEATHERED_COPPER_GRATE, Blocks.EXPOSED_COPPER_GRATE);
        previous(map, Blocks.OXIDIZED_COPPER_GRATE, Blocks.WEATHERED_COPPER_GRATE);
        previous(map, Blocks.EXPOSED_CHISELED_COPPER, Blocks.CHISELED_COPPER);
        previous(map, Blocks.WEATHERED_CHISELED_COPPER, Blocks.EXPOSED_CHISELED_COPPER);
        previous(map, Blocks.OXIDIZED_CHISELED_COPPER, Blocks.WEATHERED_CHISELED_COPPER);
        previous(map, Blocks.EXPOSED_COPPER_BULB, Blocks.COPPER_BULB);
        previous(map, Blocks.WEATHERED_COPPER_BULB, Blocks.EXPOSED_COPPER_BULB);
        previous(map, Blocks.OXIDIZED_COPPER_BULB, Blocks.WEATHERED_COPPER_BULB);
        return Map.copyOf(map);
    }

    private static void previous(Map<Block, Block> map, Block from, Block to) {
        map.put(from, to);
    }

    private record Recomposition(Item output, int inputCount, int outputCount) {
    }
}
