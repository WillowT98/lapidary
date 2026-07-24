package name.lapidary.client.screen;

import name.lapidary.screen.JewelersTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class JewelersTableScreen
        extends AbstractContainerScreen<JewelersTableMenu> {

    /*
     * Temporary background until your custom interface texture arrives.
     */
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.withDefaultNamespace(
                    "textures/gui/container/anvil.png"
            );

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

        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        this.renderBackground(
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

        this.renderTooltip(
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
                this.leftPos,
                this.topPos,
                0,
                0,
                this.imageWidth,
                this.imageHeight
        );
    }
}