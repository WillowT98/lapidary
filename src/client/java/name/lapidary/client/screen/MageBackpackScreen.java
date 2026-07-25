package name.lapidary.client.screen;

import name.lapidary.inventory.MageBackpackMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class MageBackpackScreen
        extends AbstractContainerScreen<MageBackpackMenu> {

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.withDefaultNamespace(
                    "textures/gui/container/generic_54.png"
            );

    private static final int VANILLA_WIDTH = 176;

    private static final int CANISTER_PANEL_WIDTH = 36;

    public MageBackpackScreen(
            MageBackpackMenu menu,
            Inventory playerInventory,
            Component title
    ) {
        super(
                menu,
                playerInventory,
                title
        );

        this.imageWidth =
                VANILLA_WIDTH
                        + CANISTER_PANEL_WIDTH;

        this.imageHeight =
                114 + 2 * 18;

        this.inventoryLabelY =
                this.imageHeight - 94;
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
        int upperHeight =
                2 * 18 + 17;

        /*
         * Two-row chest section.
         */
        graphics.blit(
                BACKGROUND,
                this.leftPos,
                this.topPos,
                0,
                0,
                VANILLA_WIDTH,
                upperHeight
        );

        /*
         * Player inventory section.
         */
        graphics.blit(
                BACKGROUND,
                this.leftPos,
                this.topPos + upperHeight,
                0,
                126,
                VANILLA_WIDTH,
                96
        );

        renderCanisterPanel(graphics);
    }

    private void renderCanisterPanel(
            GuiGraphics graphics
    ) {
        int panelLeft =
                this.leftPos + VANILLA_WIDTH;

        int panelTop =
                this.topPos;

        int panelBottom =
                this.topPos + 53;

        /*
         * Temporary panel matching the broad vanilla container
         * palette. It can later be replaced with custom artwork.
         */
        graphics.fill(
                panelLeft,
                panelTop,
                panelLeft + CANISTER_PANEL_WIDTH,
                panelBottom,
                0xFFC6C6C6
        );

        graphics.fill(
                panelLeft + 2,
                panelTop + 2,
                panelLeft + CANISTER_PANEL_WIDTH - 2,
                panelBottom - 2,
                0xFF8B8B8B
        );

        int slotLeft =
                this.leftPos
                        + MageBackpackMenu.CANISTER_SLOT_X;

        int slotTop =
                this.topPos
                        + MageBackpackMenu.CANISTER_SLOT_Y;

        /*
         * Vanilla-style recessed slot.
         */
        graphics.fill(
                slotLeft - 1,
                slotTop - 1,
                slotLeft + 17,
                slotTop + 17,
                0xFF373737
        );

        graphics.fill(
                slotLeft,
                slotTop,
                slotLeft + 16,
                slotTop + 16,
                0xFF8B8B8B
        );

        graphics.fill(
                slotLeft + 1,
                slotTop + 1,
                slotLeft + 16,
                slotTop + 16,
                0xFF373737
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

        int slotLeft =
                this.leftPos
                        + MageBackpackMenu.CANISTER_SLOT_X;

        int slotTop =
                this.topPos
                        + MageBackpackMenu.CANISTER_SLOT_Y;

        boolean hovering =
                mouseX >= slotLeft
                        && mouseX < slotLeft + 16
                        && mouseY >= slotTop
                        && mouseY < slotTop + 16;

        if (hovering
                && !this.menu
                .getSlot(
                        MageBackpackMenu
                                .CANISTER_MENU_SLOT
                )
                .hasItem()) {

            graphics.renderTooltip(
                    this.font,
                    Component.translatable(
                            "container.lapidary."
                                    + "mage_backpack.canister"
                    ),
                    mouseX,
                    mouseY
            );
        }
    }
}