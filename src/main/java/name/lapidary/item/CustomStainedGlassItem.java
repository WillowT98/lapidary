package name.lapidary.item;

import name.lapidary.window.WindowDesign;
import name.lapidary.window.WindowDesignData;
import name.lapidary.window.WindowStructure;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.List;
import java.util.Optional;

public final class CustomStainedGlassItem
        extends Item {

    public CustomStainedGlassItem(
            Properties properties
    ) {
        super(
                properties
        );
    }

    public static ItemStack create(
            WindowDesign design
    ) {
        ItemStack stack =
                new ItemStack(
                        ModItems.CUSTOM_STAINED_GLASS
                );

        WindowDesignData.write(
                stack,
                design
        );

        return stack;
    }

    public static Optional<WindowDesign> readDesign(
            ItemStack stack
    ) {
        if (!stack.is(
                ModItems.CUSTOM_STAINED_GLASS
        )) {
            return Optional.empty();
        }

        return WindowDesignData.read(
                stack
        );
    }

    @Override
    public InteractionResult useOn(
            UseOnContext context
    ) {
        ItemStack stack =
                context.getItemInHand();

        Optional<WindowDesign> optionalDesign =
                readDesign(
                        stack
                );

        if (optionalDesign.isEmpty()) {
            return InteractionResult.FAIL;
        }

        Level level =
                context.getLevel();

        /*
         * BlockPlaceContext automatically chooses the clicked position
         * itself when replaceable and the adjacent position otherwise.
         */
        BlockPlaceContext placementContext =
                new BlockPlaceContext(
                        context
                );

        BlockPos controllerPosition =
                placementContext.getClickedPos();

        Direction clickedFace =
                context.getClickedFace();

        Direction facing =
                clickedFace.getAxis()
                        .isHorizontal()
                        ? clickedFace
                        : context.getHorizontalDirection()
                        .getOpposite();

        Player player =
                context.getPlayer();

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        WindowDesign design =
                optionalDesign.get();

        if (!WindowStructure.place(
                level,
                player,
                placementContext,
                controllerPosition,
                facing,
                design
        )) {
            if (player != null) {
                player.displayClientMessage(
                        Component.translatable(
                                "message.lapidary.window.not_enough_space"
                        ),
                        true
                );
            }

            return InteractionResult.FAIL;
        }

        level.playSound(
                null,
                controllerPosition,
                SoundEvents.GLASS_PLACE,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );

        level.gameEvent(
                player,
                GameEvent.BLOCK_PLACE,
                controllerPosition
        );

        if (player == null
                || !player.getAbilities()
                .instabuild) {

            stack.shrink(
                    1
            );
        }

        if (player != null) {
            player.awardStat(
                    Stats.ITEM_USED
                            .get(this)
            );
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        readDesign(stack)
                .ifPresent(
                        design -> {
                            tooltip.add(
                                    Component.translatable(
                                                    "tooltip.lapidary.custom_stained_glass.size",
                                                    design.blockWidth(),
                                                    design.blockHeight()
                                            )
                                            .withStyle(
                                                    ChatFormatting.GRAY
                                            )
                            );

                            tooltip.add(
                                    Component.translatable(
                                                    "tooltip.lapidary.custom_stained_glass.background",
                                                    design.backgroundBlock()
                                                            .getName()
                                            )
                                            .withStyle(
                                                    ChatFormatting.GRAY
                                            )
                            );
                        }
                );

        super.appendHoverText(
                stack,
                context,
                tooltip,
                flag
        );
    }
}
