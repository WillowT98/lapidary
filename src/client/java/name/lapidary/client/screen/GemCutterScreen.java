package name.lapidary.client.screen;

import name.lapidary.screen.GemCutterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class GemCutterScreen
        extends AbstractContainerScreen<GemCutterMenu> {

    /*
     * Temporary background.
     *
     * Replace this with:
     * lapidary:textures/gui/container/gem_cutter.png
     * when the custom UI texture is ready.
     */
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.withDefaultNamespace(
                    "textures/gui/container/stonecutter.png"
            );

    private static final int CUT_COLUMNS = 4;
    private static final int CUT_ROWS = 3;

    private static final int CUT_AREA_X = 52;
    private static final int CUT_AREA_Y = 14;

    private static final int CUT_WIDTH = 16;
    private static final int CUT_HEIGHT = 18;

    public GemCutterScreen(
            GemCutterMenu menu,
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

        renderCutChoices(
                graphics,
                mouseX,
                mouseY
        );
    }

    private void renderCutChoices(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        int cutCount =
                Math.min(
                        menu.getAvailableCutCount(),
                        CUT_COLUMNS * CUT_ROWS
                );

        for (int index = 0;
             index < cutCount;
             index++) {

            int column =
                    index % CUT_COLUMNS;

            int row =
                    index / CUT_COLUMNS;

            int x =
                    this.leftPos
                            + CUT_AREA_X
                            + column * CUT_WIDTH;

            int y =
                    this.topPos
                            + CUT_AREA_Y
                            + row * CUT_HEIGHT;

            boolean hovered =
                    mouseX >= x
                            && mouseX < x + CUT_WIDTH
                            && mouseY >= y
                            && mouseY < y + CUT_HEIGHT;

            boolean selected =
                    menu.getSelectedCut()
                            == index;

            /*
             * Temporary selection backgrounds.
             * Your eventual GUI texture can replace these rectangles.
             */
            if (selected) {
                graphics.fill(
                        x,
                        y,
                        x + CUT_WIDTH,
                        y + CUT_HEIGHT,
                        0xA0FFFFFF
                );
            } else if (hovered) {
                graphics.fill(
                        x,
                        y,
                        x + CUT_WIDTH,
                        y + CUT_HEIGHT,
                        0x60FFFFFF
                );
            }

            ItemStack result =
                    menu.getCutResult(index);

            graphics.renderItem(
                    result,
                    x,
                    y + 1
            );
        }
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        int cutCount =
                Math.min(
                        menu.getAvailableCutCount(),
                        CUT_COLUMNS * CUT_ROWS
                );

        for (int index = 0;
             index < cutCount;
             index++) {

            int column =
                    index % CUT_COLUMNS;

            int row =
                    index / CUT_COLUMNS;

            int x =
                    this.leftPos
                            + CUT_AREA_X
                            + column * CUT_WIDTH;

            int y =
                    this.topPos
                            + CUT_AREA_Y
                            + row * CUT_HEIGHT;

            boolean hovered =
                    mouseX >= x
                            && mouseX < x + CUT_WIDTH
                            && mouseY >= y
                            && mouseY < y + CUT_HEIGHT;

            if (!hovered) {
                continue;
            }

            if (this.minecraft != null
                    && this.minecraft.gameMode != null) {

                /*
                 * This sends the cut index to
                 * GemCutterMenu.clickMenuButton on the server.
                 */
                this.minecraft.gameMode
                        .handleInventoryButtonClick(
                                this.menu.containerId,
                                index
                        );

                this.minecraft.getSoundManager()
                        .play(
                                SimpleSoundInstance.forUI(
                                        SoundEvents
                                                .UI_STONECUTTER_SELECT_RECIPE,
                                        1.0F
                                )
                        );
            }

            return true;
        }

        return super.mouseClicked(
                mouseX,
                mouseY,
                button
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

        int cutCount =
                Math.min(
                        menu.getAvailableCutCount(),
                        CUT_COLUMNS * CUT_ROWS
                );

        for (int index = 0;
             index < cutCount;
             index++) {

            int column =
                    index % CUT_COLUMNS;

            int row =
                    index / CUT_COLUMNS;

            int x =
                    this.leftPos
                            + CUT_AREA_X
                            + column * CUT_WIDTH;

            int y =
                    this.topPos
                            + CUT_AREA_Y
                            + row * CUT_HEIGHT;

            boolean hovered =
                    mouseX >= x
                            && mouseX < x + CUT_WIDTH
                            && mouseY >= y
                            && mouseY < y + CUT_HEIGHT;

            if (hovered) {
                graphics.renderTooltip(
                        this.font,
                        menu.getCutResult(index),
                        mouseX,
                        mouseY
                );

                return;
            }
        }
    }
}