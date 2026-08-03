package name.lapidary.client.screen;

import name.lapidary.screen.DisplayStorageMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public abstract class DisplayStorageScreen<
        T extends DisplayStorageMenu
        >
        extends AbstractContainerScreen<T> {

    protected DisplayStorageScreen(
            T menu,
            Inventory playerInventory,
            Component title
    ) {
        super(
                menu,
                playerInventory,
                title
        );

        imageWidth = 176;
        imageHeight =
                menu.getScreenHeight();

        inventoryLabelY =
                menu.getPlayerInventoryY()
                        - 11;

    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        renderTooltip(
                graphics,
                mouseX,
                mouseY
        );
    }

    @Override
    protected void renderLabels(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        graphics.drawString(
                font,
                title,
                titleLabelX,
                titleLabelY,
                0x2F261E,
                false
        );

        graphics.drawString(
                font,
                playerInventoryTitle,
                inventoryLabelX,
                inventoryLabelY,
                0x2F261E,
                false
        );
    }

    @Override
    protected void renderBg(
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        int left =
                leftPos;

        int top =
                topPos;

        graphics.fill(
                left,
                top,
                left + imageWidth,
                top + imageHeight,
                0xFFF0E4CE
        );

        graphics.fill(
                left + 4,
                top + 4,
                left + imageWidth - 4,
                top + imageHeight - 4,
                0xFFD2BFA2
        );

        for (Slot slot : menu.slots) {
            int x =
                    left + slot.x;

            int y =
                    top + slot.y;

            graphics.fill(
                    x - 1,
                    y - 1,
                    x + 17,
                    y + 17,
                    0xFF6D5B48
            );

            graphics.fill(
                    x,
                    y,
                    x + 16,
                    y + 16,
                    0xFFE8D7BC
            );
        }
    }
}
