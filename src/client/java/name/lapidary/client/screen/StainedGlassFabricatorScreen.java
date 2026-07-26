package name.lapidary.client.screen;

import name.lapidary.network.FabricateWindowPayload;
import name.lapidary.screen.StainedGlassFabricatorMenu;
import name.lapidary.window.WindowBackground;
import name.lapidary.window.WindowDesign;
import name.lapidary.window.WindowMaterials;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;

import java.util.Arrays;
import java.util.Map;

public final class StainedGlassFabricatorScreen
        extends AbstractContainerScreen<
        StainedGlassFabricatorMenu
        > {

    private static final int GUI_WIDTH =
            370;

    private static final int GUI_HEIGHT =
            310;

    private static final int CANVAS_X =
            12;

    private static final int CANVAS_Y =
            26;

    private static final int CANVAS_MAX_SIZE =
            176;

    private static final int PALETTE_X =
            202;

    private static final int PALETTE_Y =
            84;

    private static final int PALETTE_CELL_SIZE =
            18;

    private static final int[] COLOR_RGB = {
            0xF9FFFE,
            0xF9801D,
            0xC74EBD,
            0x3AB3DA,
            0xFED83D,
            0x80C71F,
            0xF38BAA,
            0x474F52,
            0x9D9D97,
            0x169C9C,
            0x8932B8,
            0x3C44AA,
            0x835432,
            0x5E7C16,
            0xB02E26,
            0x1D1D21
    };

    private int blockWidth =
            1;

    private int blockHeight =
            1;

    private int backgroundIndex =
            0;

    private byte selectedColor =
            0;

    private byte[] pixels =
            WindowDesign.blank(
                    1,
                    1,
                    WindowBackground.byIndex(0)
            ).pixels();

    private Button backgroundButton;

    public StainedGlassFabricatorScreen(
            StainedGlassFabricatorMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(
                menu,
                inventory,
                title
        );

        this.imageWidth =
                GUI_WIDTH;

        this.imageHeight =
                GUI_HEIGHT;

        this.inventoryLabelY =
                210;
    }

    @Override
    protected void init() {
        super.init();

        int controlsX =
                leftPos + 202;

        int controlsY =
                topPos + 26;

        addRenderableWidget(
                Button.builder(
                                Component.literal("-"),
                                button -> resizeDesign(
                                        blockWidth - 1,
                                        blockHeight
                                )
                        )
                        .bounds(
                                controlsX,
                                controlsY,
                                20,
                                20
                        )
                        .build()
        );

        addRenderableWidget(
                Button.builder(
                                Component.literal("+"),
                                button -> resizeDesign(
                                        blockWidth + 1,
                                        blockHeight
                                )
                        )
                        .bounds(
                                controlsX + 86,
                                controlsY,
                                20,
                                20
                        )
                        .build()
        );

        addRenderableWidget(
                Button.builder(
                                Component.literal("-"),
                                button -> resizeDesign(
                                        blockWidth,
                                        blockHeight - 1
                                )
                        )
                        .bounds(
                                controlsX,
                                controlsY + 24,
                                20,
                                20
                        )
                        .build()
        );

        addRenderableWidget(
                Button.builder(
                                Component.literal("+"),
                                button -> resizeDesign(
                                        blockWidth,
                                        blockHeight + 1
                                )
                        )
                        .bounds(
                                controlsX + 86,
                                controlsY + 24,
                                20,
                                20
                        )
                        .build()
        );

        backgroundButton =
                addRenderableWidget(
                        Button.builder(
                                        backgroundButtonText(),
                                        button -> {
                                            backgroundIndex =
                                                    (
                                                            backgroundIndex
                                                                    + 1
                                                    )
                                                            % WindowBackground.count();

                                            button.setMessage(
                                                    backgroundButtonText()
                                            );
                                        }
                                )
                                .bounds(
                                        controlsX + 112,
                                        controlsY,
                                        48,
                                        44
                                )
                                .build()
                );

        addRenderableWidget(
                Button.builder(
                                Component.translatable(
                                        "button.lapidary.window.clear"
                                ),
                                button -> clearDesign()
                        )
                        .bounds(
                                controlsX,
                                topPos + 166,
                                75,
                                20
                        )
                        .build()
        );

        addRenderableWidget(
                Button.builder(
                                Component.translatable(
                                        "button.lapidary.window.load_template"
                                ),
                                button -> loadTemplate()
                        )
                        .bounds(
                                controlsX + 81,
                                topPos + 166,
                                79,
                                20
                        )
                        .build()
        );

        addRenderableWidget(
                Button.builder(
                                Component.translatable(
                                        "button.lapidary.window.fabricate"
                                ),
                                button -> fabricate()
                        )
                        .bounds(
                                controlsX,
                                topPos + 190,
                                160,
                                20
                        )
                        .build()
        );
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
        graphics.fill(
                leftPos,
                topPos,
                leftPos + imageWidth,
                topPos + imageHeight,
                0xFF161A1F
        );

        graphics.fill(
                leftPos + 4,
                topPos + 20,
                leftPos + imageWidth - 4,
                topPos + imageHeight - 4,
                0xFF242A31
        );

        renderCanvas(
                graphics
        );

        renderPalette(
                graphics
        );

        renderTemplateSlot(
                graphics
        );

        renderRequirements(
                graphics
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
                8,
                7,
                0xF0F0F0,
                false
        );

        graphics.drawString(
                font,
                Component.translatable(
                        "label.lapidary.window.width",
                        blockWidth
                ),
                226,
                32,
                0xFFFFFF,
                false
        );

        graphics.drawString(
                font,
                Component.translatable(
                        "label.lapidary.window.height",
                        blockHeight
                ),
                226,
                56,
                0xFFFFFF,
                false
        );

        graphics.drawString(
                font,
                Component.translatable(
                        "label.lapidary.window.palette"
                ),
                202,
                72,
                0xD8D8D8,
                false
        );

        graphics.drawString(
                font,
                Component.translatable(
                        "label.lapidary.window.template"
                ),
                300,
                21,
                0xD8D8D8,
                false
        );

        graphics.drawString(
                font,
                playerInventoryTitle,
                104,
                210,
                0xD8D8D8,
                false
        );
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button == 0
                && selectPaletteColor(
                mouseX,
                mouseY
        )) {
            return true;
        }

        if ((button == 0 || button == 1)
                && paintPixel(
                mouseX,
                mouseY,
                button
        )) {
            return true;
        }

        return super.mouseClicked(
                mouseX,
                mouseY,
                button
        );
    }

    @Override
    public boolean mouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double dragX,
            double dragY
    ) {
        if ((button == 0 || button == 1)
                && paintPixel(
                mouseX,
                mouseY,
                button
        )) {
            return true;
        }

        return super.mouseDragged(
                mouseX,
                mouseY,
                button,
                dragX,
                dragY
        );
    }

    private void renderCanvas(
            GuiGraphics graphics
    ) {
        int scale =
                canvasScale();

        int pixelWidth =
                blockWidth
                        * WindowDesign.PIXELS_PER_BLOCK;

        int pixelHeight =
                blockHeight
                        * WindowDesign.PIXELS_PER_BLOCK;

        int canvasWidth =
                pixelWidth
                        * scale;

        int canvasHeight =
                pixelHeight
                        * scale;

        int canvasLeft =
                leftPos
                        + CANVAS_X
                        + (
                        CANVAS_MAX_SIZE
                                - canvasWidth
                ) / 2;

        int canvasTop =
                topPos
                        + CANVAS_Y
                        + (
                        CANVAS_MAX_SIZE
                                - canvasHeight
                ) / 2;

        graphics.fill(
                leftPos + CANVAS_X - 2,
                topPos + CANVAS_Y - 2,
                leftPos + CANVAS_X + CANVAS_MAX_SIZE + 2,
                topPos + CANVAS_Y + CANVAS_MAX_SIZE + 2,
                0xFF090B0D
        );

        renderTiledBackground(
                graphics,
                canvasLeft,
                canvasTop,
                scale
        );

        for (int y = 0;
             y < pixelHeight;
             y++) {

            for (int x = 0;
                 x < pixelWidth;
                 x++) {

                int value =
                        Byte.toUnsignedInt(
                                pixels[
                                        y * pixelWidth + x
                                        ]
                        );

                /*
                 * Background pixels leave the tiled block texture visible.
                 * Colored pixels replace that texture in the editor.
                 */
                if (value
                        == WindowDesign.BACKGROUND_PIXEL) {

                    continue;
                }

                int left =
                        canvasLeft
                                + x * scale;

                int top =
                        canvasTop
                                + y * scale;

                graphics.fill(
                        left,
                        top,
                        left + scale,
                        top + scale,
                        0xFF000000
                                | COLOR_RGB[value]
                );
            }
        }

        /*
         * Strong lines every sixteen pixels show where the finished
         * artwork will cross from one placed pane block to the next.
         */
        for (int x = 0;
             x <= pixelWidth;
             x += WindowDesign.PIXELS_PER_BLOCK) {

            int lineX =
                    canvasLeft
                            + x * scale;

            graphics.fill(
                    lineX,
                    canvasTop,
                    lineX + 1,
                    canvasTop + canvasHeight,
                    0xAFFFFFFF
            );
        }

        for (int y = 0;
             y <= pixelHeight;
             y += WindowDesign.PIXELS_PER_BLOCK) {

            int lineY =
                    canvasTop
                            + y * scale;

            graphics.fill(
                    canvasLeft,
                    lineY,
                    canvasLeft + canvasWidth,
                    lineY + 1,
                    0xAFFFFFFF
            );
        }
    }

    private void renderTiledBackground(
            GuiGraphics graphics,
            int canvasLeft,
            int canvasTop,
            int scale
    ) {
        WindowBackground background =
                WindowBackground.byIndex(
                        backgroundIndex
                );

        TextureAtlasSprite sprite =
                Minecraft.getInstance()
                        .getTextureAtlas(
                                InventoryMenu.BLOCK_ATLAS
                        )
                        .apply(
                                background.texture()
                        );

        int tileSize =
                WindowDesign.PIXELS_PER_BLOCK
                        * scale;

        for (int blockY = 0;
             blockY < blockHeight;
             blockY++) {

            for (int blockX = 0;
                 blockX < blockWidth;
                 blockX++) {

                graphics.blit(
                        canvasLeft
                                + blockX * tileSize,
                        canvasTop
                                + blockY * tileSize,
                        0,
                        tileSize,
                        tileSize,
                        sprite
                );
            }
        }
    }

    private void renderPalette(
            GuiGraphics graphics
    ) {
        for (int colorIndex = 0;
             colorIndex < WindowDesign.COLOR_COUNT;
             colorIndex++) {

            int column =
                    colorIndex % 8;

            int row =
                    colorIndex / 8;

            int left =
                    leftPos
                            + PALETTE_X
                            + column
                            * PALETTE_CELL_SIZE;

            int top =
                    topPos
                            + PALETTE_Y
                            + row
                            * PALETTE_CELL_SIZE;

            int borderColor =
                    colorIndex
                            == Byte.toUnsignedInt(
                            selectedColor
                    )
                            ? 0xFFFFFFFF
                            : 0xFF080A0C;

            graphics.fill(
                    left,
                    top,
                    left + 16,
                    top + 16,
                    borderColor
            );

            graphics.fill(
                    left + 2,
                    top + 2,
                    left + 14,
                    top + 14,
                    0xFF000000
                            | COLOR_RGB[colorIndex]
            );
        }
    }

    private void renderTemplateSlot(
            GuiGraphics graphics
    ) {
        int left =
                leftPos + 322;

        int top =
                topPos + 30;

        graphics.fill(
                left,
                top,
                left + 20,
                top + 20,
                0xFF090B0D
        );

        graphics.fill(
                left + 1,
                top + 1,
                left + 19,
                top + 19,
                0xFF565C63
        );
    }

    private void renderRequirements(
            GuiGraphics graphics
    ) {
        WindowDesign design =
                currentDesign();

        Map<Item, Integer> requirements =
                WindowMaterials.requirements(
                        design
                );

        int line =
                0;

        int x =
                leftPos + 202;

        int y =
                topPos + 124;

        for (Map.Entry<Item, Integer> entry :
                requirements.entrySet()) {

            if (line >= 4) {
                graphics.drawString(
                        font,
                        Component.translatable(
                                "label.lapidary.window.more_materials",
                                requirements.size() - line
                        ),
                        x,
                        y + line * 10,
                        0xBFC5CC,
                        false
                );

                break;
            }

            graphics.drawString(
                    font,
                    Component.literal(
                            entry.getValue()
                                    + " × "
                    ).append(
                            entry.getKey()
                                    .getDescription()
                    ),
                    x,
                    y + line * 10,
                    0xBFC5CC,
                    false
            );

            line++;
        }
    }

    private boolean selectPaletteColor(
            double mouseX,
            double mouseY
    ) {
        int localX =
                (int) mouseX
                        - (
                        leftPos
                                + PALETTE_X
                );

        int localY =
                (int) mouseY
                        - (
                        topPos
                                + PALETTE_Y
                );

        if (localX < 0
                || localY < 0
                || localX >= 8
                * PALETTE_CELL_SIZE
                || localY >= 2
                * PALETTE_CELL_SIZE) {

            return false;
        }

        int column =
                localX
                        / PALETTE_CELL_SIZE;

        int row =
                localY
                        / PALETTE_CELL_SIZE;

        int color =
                row * 8 + column;

        if (color < 0
                || color >= WindowDesign.COLOR_COUNT) {

            return false;
        }

        selectedColor =
                (byte) color;

        return true;
    }

    private boolean paintPixel(
            double mouseX,
            double mouseY,
            int button
    ) {
        int scale =
                canvasScale();

        int pixelWidth =
                blockWidth
                        * WindowDesign.PIXELS_PER_BLOCK;

        int pixelHeight =
                blockHeight
                        * WindowDesign.PIXELS_PER_BLOCK;

        int canvasWidth =
                pixelWidth
                        * scale;

        int canvasHeight =
                pixelHeight
                        * scale;

        int canvasLeft =
                leftPos
                        + CANVAS_X
                        + (
                        CANVAS_MAX_SIZE
                                - canvasWidth
                ) / 2;

        int canvasTop =
                topPos
                        + CANVAS_Y
                        + (
                        CANVAS_MAX_SIZE
                                - canvasHeight
                ) / 2;

        int localX =
                (int) mouseX
                        - canvasLeft;

        int localY =
                (int) mouseY
                        - canvasTop;

        if (localX < 0
                || localY < 0
                || localX >= canvasWidth
                || localY >= canvasHeight) {

            return false;
        }

        int pixelX =
                localX
                        / scale;

        int pixelY =
                localY
                        / scale;

        pixels[
                pixelY * pixelWidth
                        + pixelX
                ] =
                button == 1
                        ? WindowDesign.BACKGROUND_PIXEL
                        : selectedColor;

        return true;
    }

    private void resizeDesign(
            int requestedWidth,
            int requestedHeight
    ) {
        int newWidth =
                Mth.clamp(
                        requestedWidth,
                        WindowDesign.MIN_BLOCK_SIZE,
                        WindowDesign.MAX_BLOCK_SIZE
                );

        int newHeight =
                Mth.clamp(
                        requestedHeight,
                        WindowDesign.MIN_BLOCK_SIZE,
                        WindowDesign.MAX_BLOCK_SIZE
                );

        if (newWidth == blockWidth
                && newHeight == blockHeight) {

            return;
        }

        WindowDesign resized =
                currentDesign()
                        .resized(
                                newWidth,
                                newHeight
                        );

        blockWidth =
                resized.blockWidth();

        blockHeight =
                resized.blockHeight();

        pixels =
                resized.pixels();
    }

    private void clearDesign() {
        Arrays.fill(
                pixels,
                WindowDesign.BACKGROUND_PIXEL
        );
    }

    private void loadTemplate() {
        name.lapidary.item.CustomStainedGlassItem
                .readDesign(
                        menu.getTemplateStack()
                )
                .ifPresent(
                        design -> {
                            blockWidth =
                                    design.blockWidth();

                            blockHeight =
                                    design.blockHeight();

                            backgroundIndex =
                                    design.background()
                                            .index();

                            pixels =
                                    design.pixels();

                            if (backgroundButton != null) {
                                backgroundButton.setMessage(
                                        backgroundButtonText()
                                );
                            }
                        }
                );
    }

    private void fabricate() {
        ClientPlayNetworking.send(
                new FabricateWindowPayload(
                        menu.getContainerIdValue(),
                        blockWidth,
                        blockHeight,
                        backgroundIndex,
                        pixels
                )
        );
    }

    private WindowDesign currentDesign() {
        return new WindowDesign(
                blockWidth,
                blockHeight,
                WindowBackground.byIndex(
                        backgroundIndex
                ).id(),
                pixels
        );
    }

    private int canvasScale() {
        int pixelWidth =
                blockWidth
                        * WindowDesign.PIXELS_PER_BLOCK;

        int pixelHeight =
                blockHeight
                        * WindowDesign.PIXELS_PER_BLOCK;

        return Math.max(
                2,
                Math.min(
                        8,
                        Math.min(
                                CANVAS_MAX_SIZE
                                        / pixelWidth,
                                CANVAS_MAX_SIZE
                                        / pixelHeight
                        )
                )
        );
    }

    private Component backgroundButtonText() {
        return WindowBackground.byIndex(
                backgroundIndex
        ).displayName();
    }
}
