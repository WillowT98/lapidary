package name.lapidary.origin;

import name.lapidary.Lapidary;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import virtuoel.pehkui.api.ScaleTypes;

public final class OriginAttributeManager {

    private static final ResourceLocation FELINE_HEALTH =
            Lapidary.id("origin/feline_health");

    private static final ResourceLocation FELINE_SPEED =
            Lapidary.id("origin/feline_speed");

    private static final ResourceLocation FELINE_JUMP =
            Lapidary.id("origin/feline_jump");

    private static final ResourceLocation FELINE_BLOCK_REACH =
            Lapidary.id("origin/feline_block_reach");

    private static final ResourceLocation FELINE_ENTITY_REACH =
            Lapidary.id("origin/feline_entity_reach");

    private static final ResourceLocation FELINE_CLAWS =
            Lapidary.id("origin/feline_claws");

    private static final ResourceLocation FELINE_TRANSFORM_DAMAGE =
            Lapidary.id("origin/feline_transform_damage");

    private static final ResourceLocation FELINE_TRANSFORM_SPEED =
            Lapidary.id("origin/feline_transform_speed");

    private static final ResourceLocation MOTH_HEALTH =
            Lapidary.id("origin/moth_health");

    private static final ResourceLocation FAIRY_HEALTH =
            Lapidary.id("origin/fairy_health");

    private OriginAttributeManager() {
    }

    public static void reconcile(
            Player player,
            OriginKind kind,
            OriginAbilityData data,
            long gameTime
    ) {
        removeAll(
                player
        );

        float scale =
                1.0F;

        switch (kind) {
            case FELINE -> {
                boolean transformed =
                        data.transformedUntil()
                                > gameTime;

                scale =
                        transformed
                                ? 2.0F
                                : 1.5F;

                add(
                        player,
                        Attributes.MAX_HEALTH,
                        FELINE_HEALTH,
                        8.0D,
                        AttributeModifier.Operation.ADD_VALUE
                );

                add(
                        player,
                        Attributes.MOVEMENT_SPEED,
                        FELINE_SPEED,
                        0.20D,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                );

                add(
                        player,
                        Attributes.JUMP_STRENGTH,
                        FELINE_JUMP,
                        0.12D,
                        AttributeModifier.Operation.ADD_VALUE
                );

                add(
                        player,
                        Attributes.BLOCK_INTERACTION_RANGE,
                        FELINE_BLOCK_REACH,
                        2.0D,
                        AttributeModifier.Operation.ADD_VALUE
                );

                add(
                        player,
                        Attributes.ENTITY_INTERACTION_RANGE,
                        FELINE_ENTITY_REACH,
                        2.0D,
                        AttributeModifier.Operation.ADD_VALUE
                );

                if (data.sharpenedUntil()
                        > gameTime) {

                    add(
                            player,
                            Attributes.ATTACK_DAMAGE,
                            FELINE_CLAWS,
                            2.0D,
                            AttributeModifier.Operation.ADD_VALUE
                    );
                }

                if (transformed) {
                    add(
                            player,
                            Attributes.ATTACK_DAMAGE,
                            FELINE_TRANSFORM_DAMAGE,
                            4.0D,
                            AttributeModifier.Operation.ADD_VALUE
                    );

                    add(
                            player,
                            Attributes.MOVEMENT_SPEED,
                            FELINE_TRANSFORM_SPEED,
                            0.25D,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    );
                }
            }

            case MOTH -> {
                scale =
                        0.65F;

                add(
                        player,
                        Attributes.MAX_HEALTH,
                        MOTH_HEALTH,
                        -8.0D,
                        AttributeModifier.Operation.ADD_VALUE
                );
            }

            case FAIRY -> {
                scale =
                        0.35F;

                add(
                        player,
                        Attributes.MAX_HEALTH,
                        FAIRY_HEALTH,
                        -14.0D,
                        AttributeModifier.Operation.ADD_VALUE
                );
            }

            case NONE -> {
            }
        }

        ScaleTypes.BASE
                .getScaleData(player)
                .setTargetScale(scale);

        if (player.getHealth()
                > player.getMaxHealth()) {

            player.setHealth(
                    player.getMaxHealth()
            );
        }
    }

    public static void clear(
            Player player
    ) {
        removeAll(
                player
        );

        ScaleTypes.BASE
                .getScaleData(player)
                .setTargetScale(1.0F);

        if (player.getHealth()
                > player.getMaxHealth()) {

            player.setHealth(
                    player.getMaxHealth()
            );
        }
    }

    private static void add(
            Player player,
            net.minecraft.core.Holder<Attribute> attribute,
            ResourceLocation id,
            double amount,
            AttributeModifier.Operation operation
    ) {
        AttributeInstance instance =
                player.getAttribute(
                        attribute
                );

        if (instance == null
                || instance.hasModifier(id)) {

            return;
        }

        instance.addTransientModifier(
                new AttributeModifier(
                        id,
                        amount,
                        operation
                )
        );
    }

    private static void removeAll(
            Player player
    ) {
        remove(
                player,
                Attributes.MAX_HEALTH,
                FELINE_HEALTH
        );

        remove(
                player,
                Attributes.MOVEMENT_SPEED,
                FELINE_SPEED
        );

        remove(
                player,
                Attributes.JUMP_STRENGTH,
                FELINE_JUMP
        );

        remove(
                player,
                Attributes.BLOCK_INTERACTION_RANGE,
                FELINE_BLOCK_REACH
        );

        remove(
                player,
                Attributes.ENTITY_INTERACTION_RANGE,
                FELINE_ENTITY_REACH
        );

        remove(
                player,
                Attributes.ATTACK_DAMAGE,
                FELINE_CLAWS
        );

        remove(
                player,
                Attributes.ATTACK_DAMAGE,
                FELINE_TRANSFORM_DAMAGE
        );

        remove(
                player,
                Attributes.MOVEMENT_SPEED,
                FELINE_TRANSFORM_SPEED
        );

        remove(
                player,
                Attributes.MAX_HEALTH,
                MOTH_HEALTH
        );

        remove(
                player,
                Attributes.MAX_HEALTH,
                FAIRY_HEALTH
        );
    }

    private static void remove(
            Player player,
            net.minecraft.core.Holder<Attribute> attribute,
            ResourceLocation id
    ) {
        AttributeInstance instance =
                player.getAttribute(
                        attribute
                );

        if (instance != null) {
            instance.removeModifier(
                    id
            );
        }
    }
}
