package name.lapidary.client.screen;

import name.lapidary.screen.JewelersTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class JewelersTableScreen
        extends AbstractContainerScreen<
        JewelersTableMenu
        > {

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.withDefaultNamespace(
                    "textures/gui/container/anvil.png"
            );

    private static final int GEM_X = 27;
    private static final int GEM_Y = 47;
    private static final int JEWELRY_X = 76;
    private static final int JEWELRY_Y = 47;

    public JewelersTableScreen(
            JewelersTableMenu menu,
            Inventory playerInventory,
            Component title
    ) {
        super(
                menu,
                playerInventory,
                title
        );

        imageWidth = 176;
        imageHeight = 166;
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
    protected void renderBg(
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        graphics.blit(
                BACKGROUND,
                leftPos,
                topPos,
                0,
                0,
                imageWidth,
                imageHeight
        );

        renderRemotePreview(
                graphics,
                menu.getRemoteGemPreview(),
                GEM_X,
                GEM_Y
        );

        renderRemotePreview(
                graphics,
                menu.getRemoteJewelryPreview(),
                JEWELRY_X,
                JEWELRY_Y
        );
    }

    private void renderRemotePreview(
            GuiGraphics graphics,
            ItemStack stack,
            int relativeX,
            int relativeY
    ) {
        if (stack.isEmpty()) {
            return;
        }

        int x =
                leftPos + relativeX;

        int y =
                topPos + relativeY;

        graphics.fill(
                x - 1,
                y - 1,
                x + 17,
                y + 17,
                0x8046E5D0
        );

        graphics.renderItem(
                stack,
                x,
                y
        );
    }

    @Override
    protected void renderTooltip(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        super.renderTooltip(
                graphics,
                mouseX,
                mouseY
        );

        ItemStack remoteGem =
                menu.getRemoteGemPreview();

        if (isInside(
                mouseX,
                mouseY,
                GEM_X,
                GEM_Y
        ) && !remoteGem.isEmpty()) {

            graphics.renderTooltip(
                    font,
                    remoteGem,
                    mouseX,
                    mouseY
            );

            return;
        }

        ItemStack remoteJewelry =
                menu.getRemoteJewelryPreview();

        if (isInside(
                mouseX,
                mouseY,
                JEWELRY_X,
                JEWELRY_Y
        ) && !remoteJewelry.isEmpty()) {

            graphics.renderTooltip(
                    font,
                    remoteJewelry,
                    mouseX,
                    mouseY
            );
        }
    }

    private boolean isInside(
            int mouseX,
            int mouseY,
            int relativeX,
            int relativeY
    ) {
        int x =
                leftPos + relativeX;

        int y =
                topPos + relativeY;

        return mouseX >= x
                && mouseX < x + 16
                && mouseY >= y
                && mouseY < y + 16;
    }
}
