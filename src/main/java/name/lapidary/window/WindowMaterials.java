package name.lapidary.window;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.LinkedHashMap;
import java.util.Map;

public final class WindowMaterials {

    private static final int PIXELS_PER_MATERIAL =
            WindowDesign.PIXELS_PER_BLOCK
                    * WindowDesign.PIXELS_PER_BLOCK;

    private static final Item[] STAINED_GLASS_PANES = {
            Items.WHITE_STAINED_GLASS_PANE,
            Items.ORANGE_STAINED_GLASS_PANE,
            Items.MAGENTA_STAINED_GLASS_PANE,
            Items.LIGHT_BLUE_STAINED_GLASS_PANE,
            Items.YELLOW_STAINED_GLASS_PANE,
            Items.LIME_STAINED_GLASS_PANE,
            Items.PINK_STAINED_GLASS_PANE,
            Items.GRAY_STAINED_GLASS_PANE,
            Items.LIGHT_GRAY_STAINED_GLASS_PANE,
            Items.CYAN_STAINED_GLASS_PANE,
            Items.PURPLE_STAINED_GLASS_PANE,
            Items.BLUE_STAINED_GLASS_PANE,
            Items.BROWN_STAINED_GLASS_PANE,
            Items.GREEN_STAINED_GLASS_PANE,
            Items.RED_STAINED_GLASS_PANE,
            Items.BLACK_STAINED_GLASS_PANE
    };

    private WindowMaterials() {
    }

    public static Map<Item, Integer> requirements(
            WindowDesign design
    ) {
        int[] pixelCounts =
                new int[
                        WindowDesign.COLOR_COUNT + 1
                        ];

        for (byte pixel : design.pixels()) {
            pixelCounts[
                    Byte.toUnsignedInt(pixel)
                    ]++;
        }

        Map<Item, Integer> requirements =
                new LinkedHashMap<>();

        for (int color = 0;
             color < WindowDesign.COLOR_COUNT;
             color++) {

            int materialCount =
                    divideRoundUp(
                            pixelCounts[color],
                            PIXELS_PER_MATERIAL
                    );

            if (materialCount > 0) {
                requirements.put(
                        STAINED_GLASS_PANES[color],
                        materialCount
                );
            }
        }

        int backgroundCount =
                divideRoundUp(
                        pixelCounts[
                                WindowDesign.BACKGROUND_PIXEL
                                ],
                        PIXELS_PER_MATERIAL
                );

        if (backgroundCount > 0) {
            requirements.merge(
                    design.background()
                            .materialItem(),
                    backgroundCount,
                    Integer::sum
            );
        }

        return requirements;
    }

    public static boolean canAfford(
            ServerPlayer player,
            WindowDesign design
    ) {
        if (player.getAbilities().instabuild) {
            return true;
        }

        Inventory inventory =
                player.getInventory();

        for (Map.Entry<Item, Integer> entry :
                requirements(design)
                        .entrySet()) {

            if (countItem(
                    inventory,
                    entry.getKey()
            ) < entry.getValue()) {

                return false;
            }
        }

        return true;
    }

    public static Component firstMissingMaterial(
            ServerPlayer player,
            WindowDesign design
    ) {
        Inventory inventory =
                player.getInventory();

        for (Map.Entry<Item, Integer> entry :
                requirements(design)
                        .entrySet()) {

            int owned =
                    countItem(
                            inventory,
                            entry.getKey()
                    );

            if (owned < entry.getValue()) {
                return Component.translatable(
                        "message.lapidary.window.missing_material",
                        entry.getKey()
                                .getDescription(),
                        entry.getValue() - owned
                );
            }
        }

        return Component.empty();
    }

    public static void consume(
            ServerPlayer player,
            WindowDesign design
    ) {
        if (player.getAbilities().instabuild) {
            return;
        }

        if (!canAfford(
                player,
                design
        )) {
            throw new IllegalStateException(
                    "Attempted to consume unaffordable window materials."
            );
        }

        Inventory inventory =
                player.getInventory();

        for (Map.Entry<Item, Integer> entry :
                requirements(design)
                        .entrySet()) {

            int remaining =
                    entry.getValue();

            for (int slot = 0;
                 slot < inventory.getContainerSize()
                         && remaining > 0;
                 slot++) {

                ItemStack stack =
                        inventory.getItem(slot);

                if (!stack.is(entry.getKey())) {
                    continue;
                }

                int removed =
                        Math.min(
                                remaining,
                                stack.getCount()
                        );

                stack.shrink(
                        removed
                );

                remaining -=
                        removed;
            }
        }

        inventory.setChanged();
    }

    private static int countItem(
            Inventory inventory,
            Item item
    ) {
        int total =
                0;

        for (int slot = 0;
             slot < inventory.getContainerSize();
             slot++) {

            ItemStack stack =
                    inventory.getItem(slot);

            if (stack.is(item)) {
                total +=
                        stack.getCount();
            }
        }

        return total;
    }

    private static int divideRoundUp(
            int numerator,
            int denominator
    ) {
        if (numerator <= 0) {
            return 0;
        }

        return (
                numerator
                        + denominator
                        - 1
                ) / denominator;
    }
}
