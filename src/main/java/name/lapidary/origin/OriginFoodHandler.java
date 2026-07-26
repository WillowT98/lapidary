package name.lapidary.origin;

import name.lapidary.tag.ModItemTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class OriginFoodHandler {

    private OriginFoodHandler() {
    }

    public static InteractionResultHolder<ItemStack> interceptUse(
            ItemStack stack,
            Level level,
            Player player,
            InteractionHand hand
    ) {
        OriginKind kind =
                OriginKind.of(
                        player
                );

        if (kind == OriginKind.NONE) {
            return null;
        }

        boolean vanillaFood =
                stack.has(
                        DataComponents.FOOD
                );

        switch (kind) {
            case FELINE -> {
                if (vanillaFood
                        && !stack.is(
                        ModItemTags.FELINE_FOODS
                )) {
                    return InteractionResultHolder.fail(
                            stack
                    );
                }
            }

            case MOTH -> {
                if (stack.is(
                        ModItemTags.MOTH_FOODS
                )) {
                    return consumeSpecialFood(
                            stack,
                            level,
                            player,
                            3,
                            0.4F
                    );
                }

                if (vanillaFood) {
                    return InteractionResultHolder.fail(
                            stack
                    );
                }
            }

            case FAIRY -> {
                if (stack.is(
                        ModItemTags.FAIRY_FOODS
                )) {
                    /*
                     * Honey and honey bottles already have vanilla food
                     * behavior. Only replace non-food sweets such as sugar.
                     */
                    if (vanillaFood) {
                        return null;
                    }

                    return consumeSpecialFood(
                            stack,
                            level,
                            player,
                            2,
                            0.5F
                    );
                }

                if (vanillaFood) {
                    return InteractionResultHolder.fail(
                            stack
                    );
                }
            }

            case NONE -> {
            }
        }

        return null;
    }

    private static InteractionResultHolder<ItemStack>
    consumeSpecialFood(
            ItemStack stack,
            Level level,
            Player player,
            int nutrition,
            float saturationModifier
    ) {
        if (!player.canEat(false)) {
            return InteractionResultHolder.fail(
                    stack
            );
        }

        if (!level.isClientSide()) {
            player.getFoodData()
                    .eat(
                            nutrition,
                            saturationModifier
                    );

            level.playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.GENERIC_EAT,
                    SoundSource.PLAYERS,
                    0.8F,
                    0.9F
                            + player.getRandom()
                            .nextFloat()
                            * 0.2F
            );

            if (!player.getAbilities()
                    .instabuild) {

                stack.shrink(1);
            }
        }

        return InteractionResultHolder.consume(
                stack
        );
    }
}
