package name.lapidary.entity;

import name.lapidary.entity.ai.SableCreateCacheGoal;
import name.lapidary.entity.ai.SableReturnToCacheGoal;
import name.lapidary.item.ModItems;
import name.lapidary.tag.ModItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

public final class SableEntity extends Fox {

    private static final EntityDataAccessor<Boolean> DATA_TAMED =
            SynchedEntityData.defineId(
                    SableEntity.class,
                    EntityDataSerializers.BOOLEAN
            );

    private static final int PEBBLES_REQUIRED_TO_TAME = 4;

    /*
     * One Minecraft day.
     *
     * For rapid testing, temporarily change this to 200 ticks,
     * which is ten seconds.
     */
    private static final int FUR_COOLDOWN_TICKS = 24_000;

    private static final int CACHE_SEARCH_RADIUS = 12;

    /*
     * Temporary data-driven replacement:
     * the sable chooses one of these forest objects as its gift.
     */
    private static final Item[] FOREST_GIFTS = {
            Items.SWEET_BERRIES,
            Items.GLOW_BERRIES,
            Items.BROWN_MUSHROOM,
            Items.RED_MUSHROOM,
            Items.WHEAT_SEEDS,
            Items.PUMPKIN_SEEDS,
            Items.FEATHER,
            Items.STICK,
            Items.DANDELION,
            Items.OAK_SAPLING
    };

    private BlockPos cachePos;

    /*
     * This is authoritative for taming.
     *
     * We do not merely count Smooth Stone inside the chest, because
     * players could otherwise insert four manually and bypass the sable.
     */
    private int acceptedPebbles;

    /*
     * The player who gave the pebble currently being carried.
     *
     * When the fourth pebble is deposited, this player becomes owner.
     */
    private UUID pendingPebbleGiver;

    private UUID ownerUuid;

    private int furCooldown;


    public SableEntity(
            EntityType<? extends SableEntity> entityType,
            Level level
    ) {
        super(entityType, level);

        /*
         * The sable's mouth slot is controlled by our exchange system.
         * It should not independently collect random dropped items.
         */
        this.setCanPickUpLoot(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void registerGoals() {
        /*
         * Fox requires its own method to run because it initializes several
         * private goal fields used later during spawning.
         */
        super.registerGoals();

        /*
         * We do not actually want the ordinary fox behavior. The private
         * goal objects have now been initialized safely, so remove the
         * registered vanilla goals before installing the sable's behavior.
         */
        this.goalSelector.removeAllGoals(goal -> true);
        this.targetSelector.removeAllGoals(goal -> true);

        this.goalSelector.addGoal(
                0,
                new FloatGoal(this)
        );

        this.goalSelector.addGoal(
                1,
                new SableReturnToCacheGoal(
                        this,
                        1.15D
                )
        );

        this.goalSelector.addGoal(
                2,
                new PanicGoal(
                        this,
                        1.4D
                )
        );

        this.goalSelector.addGoal(
                3,
                new BreedGoal(
                        this,
                        1.0D
                )
        );

        this.goalSelector.addGoal(
                4,
                new TemptGoal(
                        this,
                        1.1D,
                        stack ->
                                !this.isCarryingPebble()
                                        && stack.is(Items.SMOOTH_STONE),
                        false
                )
        );

        this.goalSelector.addGoal(
                5,
                new AvoidEntityGoal<>(
                        this,
                        Player.class,
                        12.0F,
                        1.1D,
                        1.4D,
                        livingEntity ->
                                livingEntity instanceof Player player
                                        && !this.isTamed()
                                        && !isHoldingSmoothStone(player)
                )
        );

        this.goalSelector.addGoal(
                6,
                new FollowParentGoal(
                        this,
                        1.1D
                )
        );

        /*
         * A wild sable without a cache chooses a tree location and travels
         * toward it before creating the cache.
         */
        this.goalSelector.addGoal(
                7,
                new SableCreateCacheGoal(
                        this,
                        1.0D,
                        12
                )
        );

        this.goalSelector.addGoal(
                8,
                new WaterAvoidingRandomStrollGoal(
                        this,
                        1.0D
                )
        );

        this.goalSelector.addGoal(
                9,
                new LookAtPlayerGoal(
                        this,
                        Player.class,
                        8.0F
                )
        );

        this.goalSelector.addGoal(
                10,
                new RandomLookAroundGoal(this)
        );
    }

    @Override
    protected void defineSynchedData(
            SynchedEntityData.Builder builder
    ) {
        super.defineSynchedData(builder);

        builder.define(
                DATA_TAMED,
                false
        );
    }

    private void removeWildCache(ServerLevel level) {
        if (isTamed()
                || cachePos == null
                || !level.getBlockState(cachePos)
                .is(Blocks.CHEST)) {
            return;
        }

        BlockEntity blockEntity =
                level.getBlockEntity(cachePos);

        if (blockEntity instanceof Container container) {
            Containers.dropContents(
                    level,
                    cachePos,
                    container
            );
        }

        level.setBlockAndUpdate(
                cachePos,
                Blocks.GRASS_BLOCK.defaultBlockState()
        );

        cachePos = null;
    }
    public boolean isTamed() {
        return this.entityData.get(DATA_TAMED);
    }

    public void setTamed(boolean tamed) {
        this.entityData.set(
                DATA_TAMED,
                tamed
        );

        if (tamed) {
            this.setPersistenceRequired();
        }
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public Player getOwner() {
        if (ownerUuid == null) {
            return null;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        return serverLevel.getPlayerByUUID(ownerUuid);
    }

    public BlockPos getCachePos() {
        return cachePos;
    }

    public boolean hasValidCache() {
        return cachePos != null
                && this.level()
                .getBlockState(cachePos)
                .is(Blocks.CHEST)
                && this.level()
                .getBlockEntity(cachePos)
                instanceof Container;
    }

    public boolean isCarryingPebble() {
        return this.getMainHandItem()
                .is(Items.SMOOTH_STONE);
    }

    public static boolean isHoldingSmoothStone(
            Player player
    ) {
        return player.getMainHandItem()
                .is(Items.SMOOTH_STONE)
                || player.getOffhandItem()
                .is(Items.SMOOTH_STONE);
    }

    @Override
    public InteractionResult mobInteract(
            Player player,
            InteractionHand hand
    ) {
        ItemStack heldStack =
                player.getItemInHand(hand);

        /*
         * Wild pebble exchange.
         */
        if (!isTamed()
                && heldStack.is(Items.SMOOTH_STONE)
                && !isCarryingPebble()
                && hasValidCache()) {

            if (!this.level().isClientSide) {
                consumeOne(player, heldStack);

                /*
                 * Fox rendering displays the main-hand item in its mouth.
                 */
                this.setItemSlot(
                        EquipmentSlot.MAINHAND,
                        new ItemStack(Items.SMOOTH_STONE)
                );

                pendingPebbleGiver =
                        player.getUUID();

                giveForestGift(player);

                /*
                 * Preserve a sable that a player has begun interacting
                 * with, even before the fourth stone is deposited.
                 */
                this.setPersistenceRequired();
            }

            return InteractionResult.sidedSuccess(
                    this.level().isClientSide
            );
        }

        /*
         * Brushing a tamed adult sable produces fur once its cooldown
         * is ready.
         */
        if (isTamed()
                && !isBaby()
                && heldStack.is(Items.BRUSH)) {

            if (furCooldown > 0) {
                return InteractionResult.sidedSuccess(
                        this.level().isClientSide
                );
            }

            if (!this.level().isClientSide) {
                this.spawnAtLocation(
                        new ItemStack(ModItems.SABLE_FUR)
                );

                EquipmentSlot usedSlot =
                        hand == InteractionHand.MAIN_HAND
                                ? EquipmentSlot.MAINHAND
                                : EquipmentSlot.OFFHAND;

                heldStack.hurtAndBreak(
                        1,
                        player,
                        usedSlot
                );

                furCooldown =
                        FUR_COOLDOWN_TICKS;
            }

            return InteractionResult.sidedSuccess(
                    this.level().isClientSide
            );
        }

        /*
         * Meat either restores health or prepares a healthy adult for
         * breeding.
         */
        if (isTamed()
                && heldStack.is(ModItemTags.SABLE_FOOD)) {

            if (getHealth() < getMaxHealth()) {
                if (!this.level().isClientSide) {
                    consumeOne(player, heldStack);
                    heal(4.0F);
                }

                return InteractionResult.sidedSuccess(
                        this.level().isClientSide
                );
            }

            if (!isBaby() && canFallInLove()) {
                if (!this.level().isClientSide) {
                    consumeOne(player, heldStack);
                    setInLove(player);
                }

                return InteractionResult.sidedSuccess(
                        this.level().isClientSide
                );
            }
        }

        return super.mobInteract(player, hand);
    }

    private static void consumeOne(
            Player player,
            ItemStack stack
    ) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    private void giveForestGift(
            Player player
    ) {
        Item giftItem =
                FOREST_GIFTS[
                        this.getRandom().nextInt(
                                FOREST_GIFTS.length
                        )
                        ];

        ItemStack gift =
                new ItemStack(giftItem);

        /*
         * Try the player's inventory first. If it is full, place the
         * gift in the world near the player.
         */
        if (!player.addItem(gift)) {
            player.drop(
                    gift,
                    false
            );
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        if (furCooldown > 0) {
            furCooldown--;
        }

        /*
         * Detect a cache that has been broken or replaced.
         */
        if (cachePos != null
                && !this.level()
                .getBlockState(cachePos)
                .is(Blocks.CHEST)) {
            cachePos = null;
        }

    }

    private boolean hasReachableCacheApproach(
            BlockPos cacheCandidate
    ) {
        /*
         * Check all four sides of the future cache.
         */
        for (Direction direction
                : Direction.Plane.HORIZONTAL) {

            /*
             * The cache replaces a ground block. The sable stands one block
             * above an adjacent ground block.
             */
            BlockPos feetPosition =
                    cacheCandidate
                            .relative(direction)
                            .above();

            /*
             * The sable is only 0.7 blocks tall, so one empty block is enough.
             */
            if (!this.level()
                    .isEmptyBlock(feetPosition)) {
                continue;
            }

            BlockPos floorPosition =
                    feetPosition.below();

            BlockState floorState =
                    this.level()
                            .getBlockState(floorPosition);

            if (!floorState.isFaceSturdy(
                    this.level(),
                    floorPosition,
                    Direction.UP
            )) {
                continue;
            }

            /*
             * Confirm that Minecraft can actually navigate from the sable's
             * current location to this side of the cache.
             */
            Path path =
                    this.getNavigation()
                            .createPath(
                                    feetPosition,
                                    0
                            );

            if (path != null && path.canReach()) {
                return true;
            }
        }

        return false;
    }

    public BlockPos findPreferredCacheSite(
            ServerLevel level,
            int searchRadius
    ) {
        BlockPos origin = this.blockPosition();

        /*
         * Search at the same ground level as the sable.
         */
        int groundY = origin.getY() - 1;

        for (int radius = 0;
             radius <= searchRadius;
             radius++) {

            for (int xOffset = -radius;
                 xOffset <= radius;
                 xOffset++) {

                for (int zOffset = -radius;
                     zOffset <= radius;
                     zOffset++) {

                    /*
                     * Only inspect the outer edge of each radius.
                     */
                    if (Math.max(
                            Math.abs(xOffset),
                            Math.abs(zOffset)
                    ) != radius) {
                        continue;
                    }

                    BlockPos candidate =
                            new BlockPos(
                                    origin.getX() + xOffset,
                                    groundY,
                                    origin.getZ() + zOffset
                            );

                    if (canCreateCacheAt(
                            level,
                            candidate
                    )) {
                        return candidate.immutable();
                    }
                }
            }
        }

        return null;
    }
    public boolean canCreateCacheAt(
            ServerLevel level,
            BlockPos candidate
    ) {
        if (!level.getBlockState(candidate)
                .is(Blocks.GRASS_BLOCK)
                && !level.getBlockState(candidate)
                .is(Blocks.DIRT)) {
            return false;
        }

        if (!level.isEmptyBlock(candidate.above())
                || !level.isEmptyBlock(
                candidate.above(2)
        )) {
            return false;
        }

        /*
         * Prevent double chests.
         */
        for (Direction direction
                : Direction.Plane.HORIZONTAL) {

            if (level.getBlockState(
                    candidate.relative(direction)
            ).is(Blocks.CHEST)) {
                return false;
            }
        }

        if (!isBeneathTree(
                level,
                candidate
        )) {
            return false;
        }

        return !isClaimedByAnotherSable(
                level,
                candidate
        );
    }
    public void createCacheAt(
            ServerLevel level,
            BlockPos position
    ) {
        /*
         * Recheck immediately before changing the world. The terrain may
         * have changed while the sable was walking.
         */
        if (!canCreateCacheAt(
                level,
                position
        )) {
            return;
        }

        level.setBlockAndUpdate(
                position,
                Blocks.CHEST.defaultBlockState()
        );

        cachePos = position.immutable();

        this.getNavigation().stop();
    }
    public boolean canTouchCachePosition(
            BlockPos position
    ) {
        /*
         * A block directly beside or beneath the sable is comfortably
         * within this distance.
         */
        return this.distanceToSqr(
                position.getX() + 0.5D,
                position.getY() + 0.5D,
                position.getZ() + 0.5D
        ) <= 2.25D;
    }
    public BlockPos findTouchableCacheSite(
            ServerLevel level,
            BlockPos preferredPosition
    ) {
        /*
         * Prefer the original target when it has become reachable.
         */
        if (preferredPosition != null
                && canTouchCachePosition(
                preferredPosition
        )
                && canCreateCacheAt(
                level,
                preferredPosition
        )) {

            return preferredPosition.immutable();
        }

        BlockPos feetPosition =
                this.blockPosition();

        /*
         * Examine the eight ground blocks surrounding the sable.
         *
         * These are blocks immediately beside its feet and therefore within
         * normal interaction reach.
         */
        for (int xOffset = -1;
             xOffset <= 1;
             xOffset++) {

            for (int zOffset = -1;
                 zOffset <= 1;
                 zOffset++) {

                if (xOffset == 0
                        && zOffset == 0) {
                    continue;
                }

                BlockPos candidate =
                        new BlockPos(
                                feetPosition.getX()
                                        + xOffset,
                                feetPosition.getY() - 1,
                                feetPosition.getZ()
                                        + zOffset
                        );

                if (canCreateCacheAt(
                        level,
                        candidate
                )) {
                    return candidate.immutable();
                }
            }
        }

        /*
         * Finally allow the block directly beneath the sable. Replacing it
         * with a chest leaves the sable standing safely on top of the chest.
         */
        BlockPos directlyBelow =
                feetPosition.below();

        if (canCreateCacheAt(
                level,
                directlyBelow
        )) {
            return directlyBelow.immutable();
        }

        return null;
    }

    private static boolean isBeneathTree(
            ServerLevel level,
            BlockPos candidate
    ) {
        boolean foundLog = false;
        boolean foundLeaves = false;

        /*
         * Look for a nearby trunk and canopy rather than requiring the
         * chest to be directly beneath the trunk itself.
         */
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = 1; y <= 6; y++) {
                    BlockPos inspected =
                            candidate.offset(
                                    x,
                                    y,
                                    z
                            );

                    if (level.getBlockState(inspected)
                            .is(BlockTags.LOGS)) {
                        foundLog = true;
                    }

                    if (level.getBlockState(inspected)
                            .is(BlockTags.LEAVES)) {
                        foundLeaves = true;
                    }

                    if (foundLog && foundLeaves) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isClaimedByAnotherSable(
            ServerLevel level,
            BlockPos candidate
    ) {
        AABB searchArea =
                new AABB(candidate)
                        .inflate(24.0D);

        return !level.getEntitiesOfClass(
                SableEntity.class,
                searchArea,
                sable ->
                        sable != this
                                && candidate.equals(
                                sable.getCachePos()
                        )
        ).isEmpty();
    }

    /**
     * Called by SableReturnToCacheGoal when the sable reaches its chest.
     */
    public boolean depositCarriedPebble() {
        if (!isCarryingPebble()
                || !hasValidCache()) {
            return false;
        }

        BlockEntity blockEntity =
                this.level()
                        .getBlockEntity(cachePos);

        if (!(blockEntity
                instanceof Container container)) {
            return false;
        }

        if (!insertOneSmoothStone(container)) {
            /*
             * The cache is full. Keep carrying the pebble and try again
             * later rather than deleting it.
             */
            return false;
        }

        this.setItemSlot(
                EquipmentSlot.MAINHAND,
                ItemStack.EMPTY
        );

        acceptedPebbles =
                Math.min(
                        PEBBLES_REQUIRED_TO_TAME,
                        acceptedPebbles + 1
                );

        if (!isTamed()
                && acceptedPebbles
                >= PEBBLES_REQUIRED_TO_TAME
                && pendingPebbleGiver != null) {

            setTamed(true);
            setOwnerUuid(
                    pendingPebbleGiver
            );

            /*
             * Animal's entity event 18 produces heart particles.
             */
            this.level()
                    .broadcastEntityEvent(
                            this,
                            (byte) 18
                    );
        }

        pendingPebbleGiver = null;

        return true;
    }

    private static boolean insertOneSmoothStone(
            Container container
    ) {
        for (int slot = 0;
             slot < container.getContainerSize();
             slot++) {

            ItemStack existing =
                    container.getItem(slot);

            if (existing.isEmpty()) {
                container.setItem(
                        slot,
                        new ItemStack(
                                Items.SMOOTH_STONE
                        )
                );

                container.setChanged();
                return true;
            }

            if (existing.is(Items.SMOOTH_STONE)
                    && existing.getCount()
                    < existing.getMaxStackSize()) {

                existing.grow(1);
                container.setChanged();
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isFood(
            ItemStack stack
    ) {
        return isTamed()
                && stack.is(
                ModItemTags.SABLE_FOOD
        );
    }

    @Override
    public boolean canMate(
            Animal other
    ) {
        if (!(other
                instanceof SableEntity otherSable)) {
            return false;
        }

        return this.isTamed()
                && otherSable.isTamed()
                && super.canMate(other);
    }

    @Override
    public SableEntity getBreedOffspring(
            ServerLevel level,
            AgeableMob otherParent
    ) {
        SableEntity baby =
                ModEntities.SABLE.create(level);

        if (baby == null) {
            return null;
        }

        /*
         * Only tamed sables can breed, so their offspring begins tamed.
         * Prefer this parent's owner, then the other parent's owner.
         */
        UUID inheritedOwner =
                this.ownerUuid;

        if (inheritedOwner == null
                && otherParent
                instanceof SableEntity otherSable) {
            inheritedOwner =
                    otherSable.ownerUuid;
        }

        baby.setTamed(true);
        baby.setOwnerUuid(inheritedOwner);
        baby.setPersistenceRequired();

        return baby;
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide
                && this.level()
                instanceof ServerLevel serverLevel) {
            removeWildCache(serverLevel);
        }

        super.die(damageSource);
    }

    @Override
    public void addAdditionalSaveData(
            CompoundTag tag
    ) {
        super.addAdditionalSaveData(tag);

        tag.putBoolean(
                "LapidaryTamed",
                isTamed()
        );

        tag.putInt(
                "LapidaryAcceptedPebbles",
                acceptedPebbles
        );

        tag.putInt(
                "LapidaryFurCooldown",
                furCooldown
        );

        if (cachePos != null) {
            tag.putBoolean(
                    "LapidaryHasCache",
                    true
            );

            tag.putInt(
                    "LapidaryCacheX",
                    cachePos.getX()
            );

            tag.putInt(
                    "LapidaryCacheY",
                    cachePos.getY()
            );

            tag.putInt(
                    "LapidaryCacheZ",
                    cachePos.getZ()
            );
        }

        if (ownerUuid != null) {
            tag.putUUID(
                    "LapidaryOwner",
                    ownerUuid
            );
        }

        if (pendingPebbleGiver != null) {
            tag.putUUID(
                    "LapidaryPendingPebbleGiver",
                    pendingPebbleGiver
            );
        }
    }

    @Override
    public void readAdditionalSaveData(
            CompoundTag tag
    ) {
        super.readAdditionalSaveData(tag);

        setTamed(
                tag.getBoolean(
                        "LapidaryTamed"
                )
        );

        acceptedPebbles =
                tag.getInt(
                        "LapidaryAcceptedPebbles"
                );

        furCooldown =
                tag.getInt(
                        "LapidaryFurCooldown"
                );

        if (tag.getBoolean(
                "LapidaryHasCache"
        )) {
            cachePos =
                    new BlockPos(
                            tag.getInt(
                                    "LapidaryCacheX"
                            ),
                            tag.getInt(
                                    "LapidaryCacheY"
                            ),
                            tag.getInt(
                                    "LapidaryCacheZ"
                            )
                    );
        } else {
            cachePos = null;
        }

        if (tag.hasUUID(
                "LapidaryOwner"
        )) {
            ownerUuid =
                    tag.getUUID(
                            "LapidaryOwner"
                    );
        } else {
            ownerUuid = null;
        }

        if (tag.hasUUID(
                "LapidaryPendingPebbleGiver"
        )) {
            pendingPebbleGiver =
                    tag.getUUID(
                            "LapidaryPendingPebbleGiver"
                    );
        } else {
            pendingPebbleGiver = null;
        }
    }
}