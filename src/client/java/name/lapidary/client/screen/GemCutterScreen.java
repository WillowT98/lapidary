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

    private static final int INPUT_X = 20;
    private static final int INPUT_Y = 33;

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

        renderRemoteInput(
                graphics
        );

        renderCutChoices(
                graphics,
                mouseX,
                mouseY
        );
    }

    private void renderRemoteInput(
            GuiGraphics graphics
    ) {
        ItemStack remote =
                menu.getRemoteInputPreview();

        if (remote.isEmpty()) {
            return;
        }

        int x =
                leftPos + INPUT_X;

        int y =
                topPos + INPUT_Y;

        graphics.fill(
                x - 1,
                y - 1,
                x + 17,
                y + 17,
                0x8046E5D0
        );

        graphics.renderItem(
                remote,
                x,
                y
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
                    leftPos
                            + CUT_AREA_X
                            + column * CUT_WIDTH;

            int y =
                    topPos
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
                    leftPos
                            + CUT_AREA_X
                            + column * CUT_WIDTH;

            int y =
                    topPos
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

            if (minecraft != null
                    && minecraft.gameMode != null) {

                minecraft.gameMode
                        .handleInventoryButtonClick(
                                menu.containerId,
                                index
                        );

                minecraft.getSoundManager()
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

        ItemStack remote =
                menu.getRemoteInputPreview();

        int inputX =
                leftPos + INPUT_X;

        int inputY =
                topPos + INPUT_Y;

        if (!remote.isEmpty()
                && mouseX >= inputX
                && mouseX < inputX + 16
                && mouseY >= inputY
                && mouseY < inputY + 16) {

            graphics.renderTooltip(
                    font,
                    remote,
                    mouseX,
                    mouseY
            );

            return;
        }

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
                    leftPos
                            + CUT_AREA_X
                            + column * CUT_WIDTH;

            int y =
                    topPos
                            + CUT_AREA_Y
                            + row * CUT_HEIGHT;

            boolean hovered =
                    mouseX >= x
                            && mouseX < x + CUT_WIDTH
                            && mouseY >= y
                            && mouseY < y + CUT_HEIGHT;

            if (hovered) {
                graphics.renderTooltip(
                        font,
                        menu.getCutResult(index),
                        mouseX,
                        mouseY
                );

                return;
            }
        }
    }
}
