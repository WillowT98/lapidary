package name.lapidary.origin;

import name.lapidary.fluid.CanisterFluidStorage;
import name.lapidary.magic.ManaAccess;
import name.lapidary.network.OriginActionPayload;
import name.lapidary.network.OriginStatePayload;
import name.lapidary.tag.ModItemTags;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class OriginManager {

    public static final int MOTH_MAX_FLAPS =
            6;

    public static final int FAIRY_MAX_FLIGHT_CHARGE =
            20 * 10;

    public static final long FELINE_TRANSFORM_MANA_COST =
            2L * CanisterFluidStorage.BUCKET;

    public static final long FAIRY_REGEN_MANA_COST =
            CanisterFluidStorage.BUCKET;

    private static final int ACTIVE_COOLDOWN =
            10;

    private static final int MAGIC_COOLDOWN =
            20;

    private static final int VOICE_COOLDOWN =
            10;

    private static final Map<Block, Block> STRIPPED_LOGS =
            Map.ofEntries(
                    Map.entry(
                            Blocks.OAK_LOG,
                            Blocks.STRIPPED_OAK_LOG
                    ),
                    Map.entry(
                            Blocks.OAK_WOOD,
                            Blocks.STRIPPED_OAK_WOOD
                    ),
                    Map.entry(
                            Blocks.SPRUCE_LOG,
                            Blocks.STRIPPED_SPRUCE_LOG
                    ),
                    Map.entry(
                            Blocks.SPRUCE_WOOD,
                            Blocks.STRIPPED_SPRUCE_WOOD
                    ),
                    Map.entry(
                            Blocks.BIRCH_LOG,
                            Blocks.STRIPPED_BIRCH_LOG
                    ),
                    Map.entry(
                            Blocks.BIRCH_WOOD,
                            Blocks.STRIPPED_BIRCH_WOOD
                    ),
                    Map.entry(
                            Blocks.JUNGLE_LOG,
                            Blocks.STRIPPED_JUNGLE_LOG
                    ),
                    Map.entry(
                            Blocks.JUNGLE_WOOD,
                            Blocks.STRIPPED_JUNGLE_WOOD
                    ),
                    Map.entry(
                            Blocks.ACACIA_LOG,
                            Blocks.STRIPPED_ACACIA_LOG
                    ),
                    Map.entry(
                            Blocks.ACACIA_WOOD,
                            Blocks.STRIPPED_ACACIA_WOOD
                    ),
                    Map.entry(
                            Blocks.DARK_OAK_LOG,
                            Blocks.STRIPPED_DARK_OAK_LOG
                    ),
                    Map.entry(
                            Blocks.DARK_OAK_WOOD,
                            Blocks.STRIPPED_DARK_OAK_WOOD
                    ),
                    Map.entry(
                            Blocks.MANGROVE_LOG,
                            Blocks.STRIPPED_MANGROVE_LOG
                    ),
                    Map.entry(
                            Blocks.MANGROVE_WOOD,
                            Blocks.STRIPPED_MANGROVE_WOOD
                    ),
                    Map.entry(
                            Blocks.CHERRY_LOG,
                            Blocks.STRIPPED_CHERRY_LOG
                    ),
                    Map.entry(
                            Blocks.CHERRY_WOOD,
                            Blocks.STRIPPED_CHERRY_WOOD
                    ),
                    Map.entry(
                            Blocks.CRIMSON_STEM,
                            Blocks.STRIPPED_CRIMSON_STEM
                    ),
                    Map.entry(
                            Blocks.CRIMSON_HYPHAE,
                            Blocks.STRIPPED_CRIMSON_HYPHAE
                    ),
                    Map.entry(
                            Blocks.WARPED_STEM,
                            Blocks.STRIPPED_WARPED_STEM
                    ),
                    Map.entry(
                            Blocks.WARPED_HYPHAE,
                            Blocks.STRIPPED_WARPED_HYPHAE
                    )
            );

    private OriginManager() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(
                OriginManager::tickServer
        );

        ServerPlayConnectionEvents.JOIN.register(
                (
                        handler,
                        sender,
                        server
                ) -> server.execute(
                        () -> {
                            reconcile(
                                    handler.player
                            );

                            sync(
                                    handler.player
                            );
                        }
                )
        );

        AttackEntityCallback.EVENT.register(
                (
                        player,
                        level,
                        hand,
                        target,
                        hitResult
                ) -> {
                    if (!(player
                            instanceof ServerPlayer serverPlayer)
                            || OriginKind.of(
                            player
                    ) != OriginKind.MOTH) {

                        return InteractionResult.PASS;
                    }

                    OriginAbilityData data =
                            OriginPlayerData.get(
                                    serverPlayer
                            );

                    if (!data.mothDiving()
                            || serverPlayer
                            .getDeltaMovement().y
                            >= -0.15D) {

                        return InteractionResult.PASS;
                    }

                    if (target
                            instanceof LivingEntity) {

                        serverPlayer
                                .getAttribute(
                                        net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE
                                )
                                .addTransientModifier(
                                        new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                                                name.lapidary.Lapidary.id(
                                                        "origin/moth_dive_damage"
                                                ),
                                                5.0D,
                                                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE
                                        )
                                );

                        OriginPlayerData.set(
                                serverPlayer,
                                data.withMothDiving(
                                        false
                                )
                        );

                        serverPlayer.setDeltaMovement(
                                serverPlayer
                                        .getDeltaMovement()
                                        .x,
                                0.35D,
                                serverPlayer
                                        .getDeltaMovement()
                                        .z
                        );
                    }

                    return InteractionResult.PASS;
                }
        );

        ServerLivingEntityEvents.ALLOW_DAMAGE.register(
                (
                        entity,
                        source,
                        amount
                ) -> {
                    if (!(entity
                            instanceof ServerPlayer player)
                            || OriginKind.of(
                            player
                    ) != OriginKind.FELINE) {

                        return true;
                    }

                    if (source.is(
                            DamageTypeTags.IS_FALL
                    )) {
                        /*
                         * First pass: feline fall resistance is complete
                         * immunity. This is deliberately centralized so it
                         * can be changed to partial resistance later.
                         */
                        return false;
                    }

                    return true;
                }
        );
    }

    public static void handleAction(
            ServerPlayer player,
            int action
    ) {
        OriginKind kind =
                OriginKind.of(
                        player
                );

        if (kind == OriginKind.NONE) {
            return;
        }

        long gameTime =
                player.level()
                        .getGameTime();

        OriginAbilityData data =
                OriginPlayerData.get(
                        player
                );

        switch (action) {
            case OriginActionPayload.ACTIVE -> {
                if (data.activeCooldownUntil()
                        > gameTime) {

                    return;
                }

                OriginAbilityData updated =
                        switch (kind) {
                            case FELINE ->
                                    sharpenClaws(
                                            player,
                                            data
                                    );

                            case MOTH ->
                                    toggleDive(
                                            player,
                                            data
                                    );

                            case FAIRY ->
                                    toggleFairySeat(
                                            player,
                                            data
                                    );

                            case NONE ->
                                    data;
                        };

                OriginPlayerData.set(
                        player,
                        updated.withActiveCooldownUntil(
                                gameTime
                                        + ACTIVE_COOLDOWN
                        )
                );
            }

            case OriginActionPayload.MAGIC -> {
                if (data.magicCooldownUntil()
                        > gameTime) {

                    return;
                }

                OriginAbilityData updated =
                        switch (kind) {
                            case FELINE ->
                                    castFelineTransformation(
                                            player,
                                            data
                                    );

                            case FAIRY ->
                                    castFairyRegeneration(
                                            player,
                                            data
                                    );

                            case MOTH -> {
                                player.displayClientMessage(
                                        Component.translatable(
                                                "message.lapidary.origin.moth_magic_unset"
                                        ),
                                        true
                                );

                                yield data;
                            }

                            case NONE ->
                                    data;
                        };

                OriginPlayerData.set(
                        player,
                        updated.withMagicCooldownUntil(
                                gameTime
                                        + MAGIC_COOLDOWN
                        )
                );
            }

            case OriginActionPayload.VOCALIZE -> {
                if (data.voiceCooldownUntil()
                        > gameTime) {

                    return;
                }

                vocalize(
                        player,
                        kind
                );

                OriginPlayerData.set(
                        player,
                        data.withVoiceCooldownUntil(
                                gameTime
                                        + VOICE_COOLDOWN
                        )
                );
            }

            case OriginActionPayload.FLAP -> {
                if (kind == OriginKind.MOTH) {
                    flap(
                            player,
                            data
                    );
                }
            }

            default -> {
            }
        }
    }

    public static void reconcile(
            ServerPlayer player
    ) {
        OriginKind kind =
                OriginKind.of(
                        player
                );

        OriginAbilityData data =
                OriginPlayerData.get(
                        player
                );

        if (kind != OriginKind.FAIRY
                && !data.fairyHostUuid()
                .isBlank()) {

            stopFairySeat(
                    player,
                    data
            );

            data =
                    data.withFairyHostUuid(
                            ""
                    );
        }

        if (kind != OriginKind.MOTH
                && data.mothDiving()) {

            data =
                    data.withMothDiving(
                            false
                    );
        }

        if (kind == OriginKind.NONE) {
            OriginAttributeManager.clear(
                    player
            );

            disableFairyFlight(
                    player
            );
        } else {
            OriginAttributeManager.reconcile(
                    player,
                    kind,
                    data,
                    player.level()
                            .getGameTime()
            );
        }

        player.setAttached(
                name.lapidary.progression.ModAttachments.ORIGIN_ABILITIES,
                data
        );
    }

    public static void sync(
            ServerPlayer player
    ) {
        if (!ServerPlayNetworking.canSend(
                player,
                OriginStatePayload.TYPE
        )) {
            return;
        }

        OriginKind kind =
                OriginKind.of(
                        player
                );

        OriginAbilityData data =
                OriginPlayerData.get(
                        player
                );

        int resource =
                switch (kind) {
                    case MOTH ->
                            data.mothFlaps();

                    case FAIRY ->
                            data.fairyFlightCharge();

                    default ->
                            0;
                };

        int maximum =
                switch (kind) {
                    case MOTH ->
                            MOTH_MAX_FLAPS;

                    case FAIRY ->
                            FAIRY_MAX_FLIGHT_CHARGE;

                    default ->
                            0;
                };

        int cameraEntityId =
                resolveFairyHost(
                        player,
                        data
                ).map(
                        Entity::getId
                ).orElse(-1);

        ServerPlayNetworking.send(
                player,
                new OriginStatePayload(
                        kind.ordinal(),
                        resource,
                        maximum,
                        data.mothDiving()
                                || data.transformedUntil()
                                > player.level()
                                .getGameTime(),
                        cameraEntityId
                )
        );
    }

    private static void tickServer(
            MinecraftServer server
    ) {
        for (ServerPlayer player :
                server.getPlayerList()
                        .getPlayers()) {

            tickPlayer(
                    player
            );
        }
    }

    private static void tickPlayer(
            ServerPlayer player
    ) {
        OriginKind kind =
                OriginKind.of(
                        player
                );

        OriginAbilityData data =
                OriginPlayerData.get(
                        player
                );

        long gameTime =
                player.level()
                        .getGameTime();

        if (kind == OriginKind.NONE) {
            OriginAttributeManager.clear(
                    player
            );

            disableFairyFlight(
                    player
            );

            return;
        }

        if (kind != OriginKind.FELINE
                && (data.sharpenedUntil() > 0L
                || data.transformedUntil() > 0L)) {

            data =
                    data.withSharpenedUntil(
                            0L
                    ).withTransformedUntil(
                            0L
                    );
        }

        if (kind == OriginKind.FELINE) {
            tickFeline(
                    player
            );
        } else if (kind == OriginKind.MOTH) {
            data =
                    tickMoth(
                            player,
                            data
                    );
        } else if (kind == OriginKind.FAIRY) {
            data =
                    tickFairy(
                            player,
                            data
                    );
        }

        OriginAttributeManager.reconcile(
                player,
                kind,
                data,
                gameTime
        );

        player.setAttached(
                name.lapidary.progression.ModAttachments.ORIGIN_ABILITIES,
                data
        );

        if (player.tickCount % 10 == 0) {
            sync(
                    player
            );
        }
    }

    private static void tickFeline(
            ServerPlayer player
    ) {
        player.causeFoodExhaustion(
                0.006F
        );

        if (player.isInWaterOrBubble()) {
            player.addEffect(
                    new MobEffectInstance(
                            MobEffects.WEAKNESS,
                            40,
                            0,
                            true,
                            false,
                            true
                    )
            );

            player.addEffect(
                    new MobEffectInstance(
                            MobEffects.DIG_SLOWDOWN,
                            40,
                            0,
                            true,
                            false,
                            true
                    )
            );
        }
    }

    private static OriginAbilityData tickMoth(
            ServerPlayer player,
            OriginAbilityData data
    ) {
        removeHeavyArmor(
                player
        );

        /*
         * The dive callback installs this modifier immediately before
         * vanilla performs the attack. Once the dive state is cleared,
         * remove it on the following server tick so later attacks do not
         * inherit the bonus.
         */
        if (!data.mothDiving()) {
            var attackDamage =
                    player.getAttribute(
                            net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE
                    );

            if (attackDamage != null) {
                attackDamage.removeModifier(
                        name.lapidary.Lapidary.id(
                                "origin/moth_dive_damage"
                        )
                );
            }
        }

        if (player.onGround()) {
            data =
                    data.withMothFlaps(
                            MOTH_MAX_FLAPS
                    ).withMothDiving(
                            false
                    );
        }

        if (!data.mothDiving()
                && player.getDeltaMovement().y
                < 0.0D) {

            player.addEffect(
                    new MobEffectInstance(
                            MobEffects.SLOW_FALLING,
                            10,
                            0,
                            true,
                            false,
                            false
                    )
            );
        }

        if (data.mothDiving()) {
            player.setDeltaMovement(
                    player.getDeltaMovement()
                            .add(
                                    0.0D,
                                    -0.08D,
                                    0.0D
                            )
            );
        }

        if (player.tickCount % 20 == 0
                && player.getHealth()
                < player.getMaxHealth()
                && player.getFoodData()
                .getFoodLevel() > 0
                && player.level()
                .getMaxLocalRawBrightness(
                        player.blockPosition()
                ) >= 12) {

            player.heal(
                    1.0F
            );

            player.causeFoodExhaustion(
                    1.0F
            );
        }

        return data;
    }

    private static OriginAbilityData tickFairy(
            ServerPlayer player,
            OriginAbilityData data
    ) {
        removeHeavyArmor(
                player
        );

        Optional<ServerPlayer> host =
                resolveFairyHost(
                        player,
                        data
                );

        if (host.isPresent()) {
            if (!player.isPassenger()
                    || player.getVehicle()
                    != host.get()
                    || !host.get()
                    .isAlive()) {

                stopFairySeat(
                        player,
                        data
                );

                data =
                        data.withFairyHostUuid(
                                ""
                        );
            } else {
                disableFairyFlight(
                        player
                );

                return data;
            }
        }

        boolean onGround =
                player.onGround();

        int charge =
                data.fairyFlightCharge();

        if (onGround) {
            charge =
                    FAIRY_MAX_FLIGHT_CHARGE;
        }

        boolean support =
                hasSupportWithinFour(
                        player
                );

        boolean mayFly =
                charge > 0
                        && support;

        if (mayFly) {
            enableFairyFlight(
                    player
            );

            if (player.getAbilities()
                    .flying) {

                charge =
                        Math.max(
                                0,
                                charge - 1
                        );
            }
        } else {
            disableFairyFlight(
                    player
            );
        }

        if (player.tickCount % 10 == 0) {
            for (LivingEntity entity :
                    player.serverLevel()
                            .getEntitiesOfClass(
                                    LivingEntity.class,
                                    player.getBoundingBox()
                                            .inflate(
                                                    8.0D
                                            ),
                                    entity ->
                                            entity.getType()
                                                    .getCategory()
                                            == MobCategory.MONSTER
                            )) {

                entity.addEffect(
                        new MobEffectInstance(
                                MobEffects.GLOWING,
                                30,
                                0,
                                true,
                                false,
                                false
                        ),
                        player
                );
            }
        }

        return data.withFairyFlightCharge(
                charge
        );
    }

    private static OriginAbilityData sharpenClaws(
            ServerPlayer player,
            OriginAbilityData data
    ) {
        HitResult hitResult =
                player.pick(
                        player.blockInteractionRange(),
                        0.0F,
                        false
                );

        if (!(hitResult
                instanceof BlockHitResult hit)
                || hitResult.getType()
                != HitResult.Type.BLOCK) {

            player.displayClientMessage(
                    Component.translatable(
                            "message.lapidary.origin.no_log"
                    ),
                    true
            );

            return data;
        }

        BlockPos pos =
                hit.getBlockPos();

        BlockState state =
                player.level()
                        .getBlockState(
                                pos
                        );

        Block stripped =
                STRIPPED_LOGS.get(
                        state.getBlock()
                );

        if (stripped == null) {
            player.displayClientMessage(
                    Component.translatable(
                            "message.lapidary.origin.no_log"
                    ),
                    true
            );

            return data;
        }

        BlockState replacement =
                stripped.defaultBlockState();

        for (var property :
                state.getProperties()) {

            if (replacement.hasProperty(
                    property
            )) {
                replacement =
                        copyProperty(
                                state,
                                replacement,
                                property
                        );
            }
        }

        player.level()
                .setBlockAndUpdate(
                        pos,
                        replacement
                );

        player.level()
                .playSound(
                        null,
                        pos,
                        SoundEvents.AXE_STRIP,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F
                );

        long dayTime =
                player.level()
                        .getDayTime();

        long until =
                player.level()
                        .getGameTime()
                        + (
                        24000L
                                - Math.floorMod(
                                dayTime,
                                24000L
                        )
                );

        player.displayClientMessage(
                Component.translatable(
                        "message.lapidary.origin.claws_sharpened"
                ),
                true
        );

        return data.withSharpenedUntil(
                until
        );
    }

    @SuppressWarnings({
            "rawtypes",
            "unchecked"
    })
    private static BlockState copyProperty(
            BlockState source,
            BlockState target,
            net.minecraft.world.level.block.state.properties.Property property
    ) {
        return target.setValue(
                property,
                source.getValue(
                        property
                )
        );
    }

    private static OriginAbilityData toggleDive(
            ServerPlayer player,
            OriginAbilityData data
    ) {
        if (player.onGround()
                || player.isInWaterOrBubble()) {

            return data;
        }

        boolean diving =
                !data.mothDiving();

        player.displayClientMessage(
                Component.translatable(
                        diving
                                ? "message.lapidary.origin.dive_started"
                                : "message.lapidary.origin.dive_cancelled"
                ),
                true
        );

        return data.withMothDiving(
                diving
        );
    }

    private static OriginAbilityData toggleFairySeat(
            ServerPlayer player,
            OriginAbilityData data
    ) {
        if (!data.fairyHostUuid()
                .isBlank()
                || player.isPassenger()) {

            stopFairySeat(
                    player,
                    data
            );

            return data.withFairyHostUuid(
                    ""
            );
        }

        Optional<ServerPlayer> target =
                findLookedAtPlayer(
                        player
                );

        if (target.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable(
                            "message.lapidary.origin.no_head_target"
                    ),
                    true
            );

            return data;
        }

        if (!player.startRiding(
                target.get(),
                true
        )) {
            return data;
        }

        player.displayClientMessage(
                Component.translatable(
                        "message.lapidary.origin.sitting_on",
                        target.get()
                                .getDisplayName()
                ),
                true
        );

        return data.withFairyHostUuid(
                target.get()
                        .getUUID()
                        .toString()
        );
    }

    private static OriginAbilityData castFelineTransformation(
            ServerPlayer player,
            OriginAbilityData data
    ) {
        if (player.level()
                .isDay()) {

            player.displayClientMessage(
                    Component.translatable(
                            "message.lapidary.origin.night_only"
                    ),
                    true
            );

            return data;
        }

        if (data.transformedUntil()
                > player.level()
                .getGameTime()) {

            return data;
        }

        ManaAccess.Result result =
                ManaAccess.tryConsume(
                        player,
                        FELINE_TRANSFORM_MANA_COST
                );

        if (result != ManaAccess.Result.SUCCESS) {
            showManaFailure(
                    player,
                    result
            );

            return data;
        }

        long dayTime =
                player.level()
                        .getDayTime();

        long untilDawn =
                24000L
                        - Math.floorMod(
                        dayTime,
                        24000L
                );

        player.level()
                .playSound(
                        null,
                        player.blockPosition(),
                        SoundEvents.RAVAGER_ROAR,
                        SoundSource.PLAYERS,
                        0.7F,
                        0.8F
                );

        player.displayClientMessage(
                Component.translatable(
                        "message.lapidary.origin.feline_transformed"
                ),
                true
        );

        return data.withTransformedUntil(
                player.level()
                        .getGameTime()
                        + untilDawn
        );
    }

    private static OriginAbilityData castFairyRegeneration(
            ServerPlayer player,
            OriginAbilityData data
    ) {
        ManaAccess.Result result =
                ManaAccess.tryConsume(
                        player,
                        FAIRY_REGEN_MANA_COST
                );

        if (result != ManaAccess.Result.SUCCESS) {
            showManaFailure(
                    player,
                    result
            );

            return data;
        }

        for (ServerPlayer target :
                player.serverLevel()
                        .getPlayers(
                                target ->
                                        target != player
                                                && target.distanceToSqr(
                                                player
                                        ) <= 64.0D
                        )) {

            target.addEffect(
                    new MobEffectInstance(
                            MobEffects.REGENERATION,
                            20 * 8,
                            1,
                            false,
                            true,
                            true
                    ),
                    player
            );
        }

        player.serverLevel()
                .sendParticles(
                        net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                        player.getX(),
                        player.getY()
                                + 0.8D,
                        player.getZ(),
                        40,
                        4.0D,
                        2.0D,
                        4.0D,
                        0.05D
                );

        player.level()
                .playSound(
                        null,
                        player.blockPosition(),
                        SoundEvents.AMETHYST_BLOCK_CHIME,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.4F
                );

        return data;
    }

    private static void vocalize(
            ServerPlayer player,
            OriginKind kind
    ) {
        switch (kind) {
            case FELINE ->
                    player.level()
                            .playSound(
                                    null,
                                    player.blockPosition(),
                                    SoundEvents.CAT_PURR,
                                    SoundSource.PLAYERS,
                                    1.0F,
                                    1.0F
                            );

            case MOTH ->
                    player.level()
                            .playSound(
                                    null,
                                    player.blockPosition(),
                                    SoundEvents.BAT_AMBIENT,
                                    SoundSource.PLAYERS,
                                    0.9F,
                                    1.6F
                            );

            case FAIRY ->
                    player.level()
                            .playSound(
                                    null,
                                    player.blockPosition(),
                                    SoundEvents.AMETHYST_BLOCK_CHIME,
                                    SoundSource.PLAYERS,
                                    0.8F,
                                    1.8F
                            );

            case NONE -> {
            }
        }
    }

    private static void flap(
            ServerPlayer player,
            OriginAbilityData data
    ) {
        if (player.onGround()
                || player.isInWaterOrBubble()
                || data.mothFlaps() <= 0) {

            return;
        }

        Vec3 movement =
                player.getDeltaMovement();

        player.setDeltaMovement(
                movement.x,
                Math.max(
                        movement.y,
                        0.42D
                ),
                movement.z
        );

        player.hasImpulse =
                true;

        player.level()
                .playSound(
                        null,
                        player.blockPosition(),
                        SoundEvents.PHANTOM_FLAP,
                        SoundSource.PLAYERS,
                        0.5F,
                        1.8F
                );

        OriginPlayerData.set(
                player,
                data.withMothFlaps(
                        data.mothFlaps()
                                - 1
                )
        );
    }

    private static void removeHeavyArmor(
            ServerPlayer player
    ) {
        for (EquipmentSlot slot :
                new EquipmentSlot[]{
                        EquipmentSlot.HEAD,
                        EquipmentSlot.CHEST,
                        EquipmentSlot.LEGS,
                        EquipmentSlot.FEET
                }) {

            ItemStack stack =
                    player.getItemBySlot(
                            slot
                    );

            if (!stack.is(
                    ModItemTags.HEAVY_ARMOR
            )) {
                continue;
            }

            player.setItemSlot(
                    slot,
                    ItemStack.EMPTY
            );

            if (!player.getInventory()
                    .add(stack)) {

                player.drop(
                        stack,
                        false
                );
            }

            player.displayClientMessage(
                    Component.translatable(
                            "message.lapidary.origin.armor_too_heavy"
                    ),
                    true
            );
        }
    }

    private static boolean hasSupportWithinFour(
            ServerPlayer player
    ) {
        BlockPos origin =
                player.blockPosition();

        for (int depth = 1;
             depth <= 4;
             depth++) {

            BlockPos tested =
                    origin.below(
                            depth
                    );

            BlockState state =
                    player.level()
                            .getBlockState(
                                    tested
                            );

            if (!state.getCollisionShape(
                    player.level(),
                    tested
            ).isEmpty()) {

                return true;
            }
        }

        return false;
    }

    private static void enableFairyFlight(
            ServerPlayer player
    ) {
        if (player.isCreative()
                || player.isSpectator()) {

            return;
        }

        if (!player.getAbilities()
                .mayfly) {

            player.getAbilities()
                    .mayfly =
                    true;

            player.onUpdateAbilities();
        }
    }

    private static void disableFairyFlight(
            ServerPlayer player
    ) {
        if (player.isCreative()
                || player.isSpectator()) {

            return;
        }

        boolean changed =
                player.getAbilities()
                        .mayfly
                        || player.getAbilities()
                        .flying;

        player.getAbilities()
                .mayfly =
                false;

        player.getAbilities()
                .flying =
                false;

        if (changed) {
            player.onUpdateAbilities();
        }
    }

    private static Optional<ServerPlayer> findLookedAtPlayer(
            ServerPlayer player
    ) {
        Vec3 eye =
                player.getEyePosition();

        Vec3 look =
                player.getLookAngle()
                        .normalize();

        return player.serverLevel()
                .getPlayers(
                        target ->
                                target != player
                                        && target.isAlive()
                                        && target.distanceToSqr(
                                        player
                                ) <= 25.0D
                )
                .stream()
                .filter(
                        target -> {
                            Vec3 direction =
                                    target.getEyePosition()
                                            .subtract(
                                                    eye
                                            )
                                            .normalize();

                            return direction.dot(
                                    look
                            ) >= 0.92D;
                        }
                )
                .min(
                        Comparator.comparingDouble(
                                player::distanceToSqr
                        )
                );
    }

    private static Optional<ServerPlayer> resolveFairyHost(
            ServerPlayer player,
            OriginAbilityData data
    ) {
        if (data.fairyHostUuid()
                .isBlank()) {

            return Optional.empty();
        }

        try {
            UUID uuid =
                    UUID.fromString(
                            data.fairyHostUuid()
                    );

            return Optional.ofNullable(
                    player.server
                            .getPlayerList()
                            .getPlayer(
                                    uuid
                            )
            );
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private static void stopFairySeat(
            ServerPlayer player,
            OriginAbilityData data
    ) {
        if (player.isPassenger()) {
            player.stopRiding();
        }

        sync(
                player
        );
    }

    private static void showManaFailure(
            ServerPlayer player,
            ManaAccess.Result result
    ) {
        String key =
                switch (result) {
                    case NO_BACKPACK ->
                            "message.lapidary.magic.no_backpack";

                    case NO_CANISTER ->
                            "message.lapidary.magic.no_canister";

                    case WRONG_LIQUID ->
                            "message.lapidary.magic.wrong_liquid";

                    case NOT_ENOUGH_MANA ->
                            "message.lapidary.magic.not_enough_mana";

                    case SUCCESS ->
                            "";
                };

        if (!key.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable(
                            key
                    ).withStyle(
                            ChatFormatting.RED
                    ),
                    true
            );
        }
    }
}
