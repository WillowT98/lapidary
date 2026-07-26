package name.lapidary.item;

import name.lapidary.window.WindowDesign;
import name.lapidary.window.WindowDesignData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

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
