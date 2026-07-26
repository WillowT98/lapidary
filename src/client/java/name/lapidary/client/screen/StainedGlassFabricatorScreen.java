package name.lapidary.client.screen;

import name.lapidary.item.CustomStainedGlassItem;
import name.lapidary.network.FabricateWindowPayload;
import name.lapidary.network.SelectWindowBackgroundPayload;
import name.lapidary.screen.StainedGlassFabricatorMenu;
import name.lapidary.window.WindowDesign;
import name.lapidary.window.WindowMaterials;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StainedGlassFabricatorScreen
        extends AbstractContainerScreen<
        StainedGlassFabricatorMenu
        > {

    private static final int GUI_WIDTH =
            350;

    private static final int GUI_HEIGHT =
            188;

    private static final int CANVAS_X =
            8;

    private static final int CANVAS_Y =
            20;

    private static final int CANVAS_MAX_SIZE =
            160;

    private static final int PANEL_X =
            178;

    private static final int BACKGROUND_TILE_X =
            178;

    private static final int BACKGROUND_TILE_Y =
            20;

    private static final int TEMPLATE_TILE_X =
            214;

    private static final int TEMPLATE_TILE_Y =
            20;

    private static final int TILE_SIZE =
            32;

    private static final int PALETTE_X =
            178;

    private static final int PALETTE_Y =
            62;

    private static final int PALETTE_CELL_SIZE =
            17;

    private static final int PICKER_X =
            178;

    private static final int PICKER_Y =
            54;

    private static final int PICKER_WIDTH =
            164;

    private static final int PICKER_ROW_HEIGHT =
            20;

    private static final int PICKER_VISIBLE_ROWS =
            6;

    private static final int MIN_BRUSH_SIZE =
            1;

    private static final int MAX_BRUSH_SIZE =
            8;

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

    private byte selectedColor =
            0;

    private int brushSize =
            1;

    private byte[] pixels =
            WindowDesign.blank(
                    1,
                    1,
                    Blocks.STONE_BRICKS
            ).pixels();

    private PickerMode pickerMode =
            PickerMode.NONE;

    private int pickerScroll;

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
    }

    @Override
    protected void init() {
        super.init();

        addRenderableWidget(
                Button.builder(
                                Component.literal("-"),
                                button -> resizeDesign(
                                        blockWidth - 1,
                                        blockHeight
                                )
                        )
                        .bounds(
                                leftPos + 252,
                                topPos + 20,
                                20,
                                16
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
                                leftPos + 322,
                                topPos + 20,
                                20,
                                16
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
                                leftPos + 252,
                                topPos + 38,
                                20,
                                16
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
                                leftPos + 322,
                                topPos + 38,
                                20,
                                16
                        )
                        .build()
        );

        addRenderableWidget(
                Button.builder(
                                Component.literal("-"),
                                button -> setBrushSize(
                                        brushSize - 1
                                )
                        )
                        .bounds(
                                leftPos + PANEL_X,
                                topPos + 100,
                                20,
                                18
                        )
                        .build()
        );

        addRenderableWidget(
                Button.builder(
                                Component.literal("+"),
                                button -> setBrushSize(
                                        brushSize + 1
                                )
                        )
                        .bounds(
                                leftPos + PANEL_X + 144,
                                topPos + 100,
                                20,
                                18
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
                                leftPos + PANEL_X,
                                topPos + 122,
                                72,
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
                                leftPos + PANEL_X + 76,
                                topPos + 122,
                                88,
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

        if (pickerMode != PickerMode.NONE) {
            renderPicker(
                    graphics,
                    mouseX,
                    mouseY
            );
        } else {
            renderTooltip(
                    graphics,
                    mouseX,
                    mouseY
            );

            renderTileTooltip(
                    graphics,
                    mouseX,
                    mouseY
            );
        }
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
                topPos + 17,
                leftPos + imageWidth - 4,
                topPos + imageHeight - 4,
                0xFF242A31
        );

        renderCanvas(
                graphics
        );

        renderSelectionTiles(
                graphics
        );

        renderPalette(
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
                5,
                0xF0F0F0,
                false
        );

        graphics.drawString(
                font,
                Component.translatable(
                        "label.lapidary.window.width",
                        blockWidth
                ),
                276,
                24,
                0xFFFFFF,
                false
        );

        graphics.drawString(
                font,
                Component.translatable(
                        "label.lapidary.window.height",
                        blockHeight
                ),
                276,
                42,
                0xFFFFFF,
                false
        );

        graphics.drawString(
                font,
                Component.translatable(
                        "label.lapidary.window.palette"
                ),
                PALETTE_X,
                53,
                0xD8D8D8,
                false
        );

        graphics.drawCenteredString(
                font,
                Component.translatable(
                        "label.lapidary.window.brush_size",
                        brushSize
                ),
                PANEL_X + 82,
                105,
                0xFFFFFF
        );
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button != 0
                && pickerMode != PickerMode.NONE) {

            closePicker();

            return true;
        }

        if (button == 0
                && pickerMode != PickerMode.NONE) {

            if (clickPicker(
                    mouseX,
                    mouseY
            )) {
                return true;
            }

            closePicker();

            return true;
        }

        if (button == 0
                && isInside(
                mouseX,
                mouseY,
                BACKGROUND_TILE_X,
                BACKGROUND_TILE_Y,
                TILE_SIZE,
                TILE_SIZE
        )) {
            openPicker(
                    PickerMode.BACKGROUND
            );

            return true;
        }

        if (button == 0
                && isInside(
                mouseX,
                mouseY,
                TEMPLATE_TILE_X,
                TEMPLATE_TILE_Y,
                TILE_SIZE,
                TILE_SIZE
        )) {
            openPicker(
                    PickerMode.TEMPLATE
            );

            return true;
        }

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
        if (pickerMode != PickerMode.NONE) {
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

        return super.mouseDragged(
                mouseX,
                mouseY,
                button,
                dragX,
                dragY
        );
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount
    ) {
        if (pickerMode == PickerMode.NONE) {
            return super.mouseScrolled(
                    mouseX,
                    mouseY,
                    horizontalAmount,
                    verticalAmount
            );
        }

        int entryCount =
                pickerMode == PickerMode.BACKGROUND
                        ? getBackgroundEntries().size()
                        : getTemplateEntries().size();

        int maxScroll =
                Math.max(
                        0,
                        entryCount
                                - PICKER_VISIBLE_ROWS
                );

        pickerScroll =
                Mth.clamp(
                        pickerScroll
                                - (int) Math.signum(
                                verticalAmount
                        ),
                        0,
                        maxScroll
                );

        return true;
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

        Block background =
                menu.getSelectedBackgroundBlock();

        if (menu.hasSelectedBackground()
                && background != Blocks.AIR) {

            renderTiledBackground(
                    graphics,
                    canvasLeft,
                    canvasTop,
                    scale,
                    background
            );
        } else {
            /*
             * The checkerboard represents either an explicit Air
             * background or a design that has not chosen a background yet.
             * The selection tile distinguishes those two states.
             */
            renderCheckerboard(
                    graphics,
                    canvasLeft,
                    canvasTop,
                    canvasWidth,
                    canvasHeight,
                    scale
            );
        }

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

    private void renderSelectionTiles(
            GuiGraphics graphics
    ) {
        renderTileBackground(
                graphics,
                BACKGROUND_TILE_X,
                BACKGROUND_TILE_Y
        );

        renderTileBackground(
                graphics,
                TEMPLATE_TILE_X,
                TEMPLATE_TILE_Y
        );

        Block background =
                menu.getSelectedBackgroundBlock();

        if (!menu.hasSelectedBackground()) {
            graphics.drawCenteredString(
                    font,
                    Component.literal("+"),
                    leftPos
                            + BACKGROUND_TILE_X
                            + TILE_SIZE / 2,
                    topPos
                            + BACKGROUND_TILE_Y
                            + 11,
                    0xAAB1B9
            );
        } else if (background == Blocks.AIR) {
            renderAirIcon(
                    graphics,
                    leftPos
                            + BACKGROUND_TILE_X
                            + 8,
                    topPos
                            + BACKGROUND_TILE_Y
                            + 8,
                    16
            );
        } else {
            graphics.renderItem(
                    new ItemStack(
                            background.asItem()
                    ),
                    leftPos
                            + BACKGROUND_TILE_X
                            + 8,
                    topPos
                            + BACKGROUND_TILE_Y
                            + 8
            );
        }

        graphics.drawCenteredString(
                font,
                Component.literal("T"),
                leftPos
                        + TEMPLATE_TILE_X
                        + TILE_SIZE / 2,
                topPos
                        + TEMPLATE_TILE_Y
                        + 11,
                0xAAB1B9
        );
    }

    private void renderAirIcon(
            GuiGraphics graphics,
            int left,
            int top,
            int size
    ) {
        int half =
                size / 2;

        graphics.fill(
                left,
                top,
                left + half,
                top + half,
                0xFF3B4148
        );

        graphics.fill(
                left + half,
                top,
                left + size,
                top + half,
                0xFF272C32
        );

        graphics.fill(
                left,
                top + half,
                left + half,
                top + size,
                0xFF272C32
        );

        graphics.fill(
                left + half,
                top + half,
                left + size,
                top + size,
                0xFF3B4148
        );
    }

    private void renderTileBackground(
            GuiGraphics graphics,
            int tileX,
            int tileY
    ) {
        int left =
                leftPos + tileX;

        int top =
                topPos + tileY;

        graphics.fill(
                left,
                top,
                left + TILE_SIZE,
                top + TILE_SIZE,
                0xFF090B0D
        );

        graphics.fill(
                left + 2,
                top + 2,
                left + TILE_SIZE - 2,
                top + TILE_SIZE - 2,
                0xFF4A5159
        );
    }

    private void renderPicker(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        List<? extends PickerEntry> entries =
                pickerMode == PickerMode.BACKGROUND
                        ? getBackgroundEntries()
                        : getTemplateEntries();

        int left =
                leftPos + PICKER_X;

        int top =
                topPos + PICKER_Y;

        int height =
                PICKER_VISIBLE_ROWS
                        * PICKER_ROW_HEIGHT
                        + 4;

        graphics.fill(
                left - 2,
                top - 2,
                left + PICKER_WIDTH + 2,
                top + height,
                0xFF07090B
        );

        graphics.fill(
                left,
                top,
                left + PICKER_WIDTH,
                top + height - 2,
                0xFF30363D
        );

        if (entries.isEmpty()) {
            graphics.drawCenteredString(
                    font,
                    pickerMode == PickerMode.BACKGROUND
                            ? Component.translatable(
                            "message.lapidary.window.no_background_blocks"
                    )
                            : Component.translatable(
                            "message.lapidary.window.no_templates"
                    ),
                    left + PICKER_WIDTH / 2,
                    top + 9,
                    0xD8D8D8
            );

            return;
        }

        int first =
                pickerScroll;

        int last =
                Math.min(
                        entries.size(),
                        first + PICKER_VISIBLE_ROWS
                );

        for (int index = first;
             index < last;
             index++) {

            int visibleRow =
                    index - first;

            int rowTop =
                    top
                            + visibleRow
                            * PICKER_ROW_HEIGHT;

            PickerEntry entry =
                    entries.get(index);

            boolean hovered =
                    mouseX >= left
                            && mouseX < left
                            + PICKER_WIDTH
                            && mouseY >= rowTop
                            && mouseY < rowTop
                            + PICKER_ROW_HEIGHT;

            if (hovered) {
                graphics.fill(
                        left + 1,
                        rowTop + 1,
                        left + PICKER_WIDTH - 1,
                        rowTop + PICKER_ROW_HEIGHT - 1,
                        0xFF56616C
                );
            }

            ItemStack iconStack =
                    entry.stack();

            if (iconStack.isEmpty()) {
                renderAirIcon(
                        graphics,
                        left + 2,
                        rowTop + 2,
                        16
                );
            } else {
                graphics.renderItem(
                        iconStack,
                        left + 2,
                        rowTop + 2
                );
            }

            graphics.drawString(
                    font,
                    entry.label(),
                    left + 22,
                    rowTop + 6,
                    0xFFFFFF,
                    false
            );
        }
    }

    private boolean clickPicker(
            double mouseX,
            double mouseY
    ) {
        int left =
                leftPos + PICKER_X;

        int top =
                topPos + PICKER_Y;

        if (!isInside(
                mouseX,
                mouseY,
                PICKER_X,
                PICKER_Y,
                PICKER_WIDTH,
                PICKER_VISIBLE_ROWS
                        * PICKER_ROW_HEIGHT
        )) {
            return false;
        }

        int row =
                (
                        (int) mouseY
                                - top
                ) / PICKER_ROW_HEIGHT;

        int index =
                pickerScroll + row;

        if (pickerMode == PickerMode.BACKGROUND) {
            List<BackgroundEntry> entries =
                    getBackgroundEntries();

            if (index >= 0
                    && index < entries.size()) {

                BackgroundEntry entry =
                        entries.get(index);

                ClientPlayNetworking.send(
                        new SelectWindowBackgroundPayload(
                                menu.getContainerIdValue(),
                                BuiltInRegistries.BLOCK
                                        .getId(
                                                entry.block()
                                        )
                        )
                );

                closePicker();

                return true;
            }
        } else if (pickerMode
                == PickerMode.TEMPLATE) {

            List<TemplateEntry> entries =
                    getTemplateEntries();

            if (index >= 0
                    && index < entries.size()) {

                loadTemplate(
                        entries.get(index)
                                .design()
                );

                closePicker();

                return true;
            }
        }

        return false;
    }

    private List<BackgroundEntry> getBackgroundEntries() {
        Minecraft minecraft =
                Minecraft.getInstance();

        if (minecraft.player == null) {
            return List.of(
                    new BackgroundEntry(
                            Blocks.AIR,
                            0
                    )
            );
        }

        Map<Block, Integer> counts =
                new LinkedHashMap<>();

        Inventory inventory =
                minecraft.player
                        .getInventory();

        for (int slot = 0;
             slot < inventory.getContainerSize();
             slot++) {

            addBackgroundStack(
                    counts,
                    inventory.getItem(slot)
            );
        }

        List<BackgroundEntry> physicalEntries =
                new ArrayList<>();

        for (Map.Entry<Block, Integer> entry :
                counts.entrySet()) {

            physicalEntries.add(
                    new BackgroundEntry(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        physicalEntries.sort(
                Comparator.comparing(
                        entry ->
                                entry.block()
                                        .getName()
                                        .getString(),
                        String.CASE_INSENSITIVE_ORDER
                )
        );

        List<BackgroundEntry> entries =
                new ArrayList<>();

        /*
         * Air is always the first option and never depends on inventory.
         */
        entries.add(
                new BackgroundEntry(
                        Blocks.AIR,
                        0
                )
        );

        entries.addAll(
                physicalEntries
        );

        return entries;
    }

    private void addBackgroundStack(
            Map<Block, Integer> counts,
            ItemStack stack
    ) {
        if (!(stack.getItem()
                instanceof BlockItem blockItem)
                || blockItem.getBlock()
                == Blocks.AIR) {

            return;
        }

        counts.merge(
                blockItem.getBlock(),
                stack.getCount(),
                Integer::sum
        );
    }

    private List<TemplateEntry> getTemplateEntries() {
        Minecraft minecraft =
                Minecraft.getInstance();

        if (minecraft.player == null) {
            return List.of();
        }

        List<TemplateEntry> entries =
                new ArrayList<>();

        Inventory inventory =
                minecraft.player
                        .getInventory();

        for (int slot = 0;
             slot < inventory.getContainerSize();
             slot++) {

            ItemStack stack =
                    inventory.getItem(slot);

            CustomStainedGlassItem.readDesign(
                    stack
            ).ifPresent(
                    design ->
                            entries.add(
                                    new TemplateEntry(
                                            stack.copyWithCount(1),
                                            design
                                    )
                            )
            );
        }

        return entries;
    }

    private void loadTemplate(
            WindowDesign design
    ) {
        blockWidth =
                design.blockWidth();

        blockHeight =
                design.blockHeight();

        pixels =
                design.pixels();

        /*
         * Select the template's background automatically only when the
         * player currently carries that block. Otherwise the artwork
         * loads and the player can choose a replacement background.
         */
        for (BackgroundEntry entry :
                getBackgroundEntries()) {

            if (entry.block()
                    == design.backgroundBlock()) {

                ClientPlayNetworking.send(
                        new SelectWindowBackgroundPayload(
                                menu.getContainerIdValue(),
                                BuiltInRegistries.BLOCK
                                        .getId(
                                                entry.block()
                                        )
                        )
                );

                break;
            }
        }
    }

    private void renderTiledBackground(
            GuiGraphics graphics,
            int canvasLeft,
            int canvasTop,
            int scale,
            Block backgroundBlock
    ) {
        TextureAtlasSprite sprite =
                getSideSprite(
                        backgroundBlock
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

    private TextureAtlasSprite getSideSprite(
            Block block
    ) {
        BlockState state =
                block.defaultBlockState();

        BakedModel model =
                Minecraft.getInstance()
                        .getBlockRenderer()
                        .getBlockModel(
                                state
                        );

        List<BakedQuad> sideQuads =
                model.getQuads(
                        state,
                        Direction.NORTH,
                        RandomSource.create(0L)
                );

        for (BakedQuad quad : sideQuads) {
            if (quad.getTintIndex() < 0) {
                return quad.getSprite();
            }
        }

        if (!sideQuads.isEmpty()) {
            return sideQuads.getFirst()
                    .getSprite();
        }

        List<BakedQuad> unculledQuads =
                model.getQuads(
                        state,
                        null,
                        RandomSource.create(0L)
                );

        if (!unculledQuads.isEmpty()) {
            return unculledQuads.getFirst()
                    .getSprite();
        }

        return model.getParticleIcon();
    }

    private void renderCheckerboard(
            GuiGraphics graphics,
            int canvasLeft,
            int canvasTop,
            int canvasWidth,
            int canvasHeight,
            int scale
    ) {
        for (int y = 0;
             y < canvasHeight;
             y += scale) {

            for (int x = 0;
                 x < canvasWidth;
                 x += scale) {

                int checker =
                        (
                                x / scale
                                        + y / scale
                        ) & 1;

                graphics.fill(
                        canvasLeft + x,
                        canvasTop + y,
                        canvasLeft + x + scale,
                        canvasTop + y + scale,
                        checker == 0
                                ? 0xFF3B4148
                                : 0xFF272C32
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
                    left + 15,
                    top + 15,
                    borderColor
            );

            graphics.fill(
                    left + 2,
                    top + 2,
                    left + 13,
                    top + 13,
                    0xFF000000
                            | COLOR_RGB[colorIndex]
            );
        }
    }

    private void renderRequirements(
            GuiGraphics graphics
    ) {
        Block background =
                menu.getSelectedBackgroundBlock();

        if (!menu.hasSelectedBackground()) {
            graphics.drawString(
                    font,
                    Component.translatable(
                            "message.lapidary.window.choose_background"
                    ),
                    leftPos + PANEL_X,
                    topPos + 148,
                    0xD99A72,
                    false
            );

            return;
        }

        WindowDesign design =
                new WindowDesign(
                        blockWidth,
                        blockHeight,
                        BuiltInRegistries.BLOCK
                                .getKey(background)
                                .toString(),
                        pixels
                );

        Map<Item, Integer> requirements =
                WindowMaterials.requirements(
                        design
                );

        int line =
                0;

        for (Map.Entry<Item, Integer> entry :
                requirements.entrySet()) {

            if (line >= 3) {
                graphics.drawString(
                        font,
                        Component.translatable(
                                "label.lapidary.window.more_materials",
                                requirements.size() - line
                        ),
                        leftPos + PANEL_X,
                        topPos + 148 + line * 10,
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
                    leftPos + PANEL_X,
                    topPos + 148 + line * 10,
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

        paintBrush(
                pixelX,
                pixelY,
                button == 1
                        ? WindowDesign.BACKGROUND_PIXEL
                        : selectedColor
        );

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

        Block background =
                menu.hasSelectedBackground()
                        ? menu.getSelectedBackgroundBlock()
                        : Blocks.STONE_BRICKS;

        WindowDesign resized =
                new WindowDesign(
                        blockWidth,
                        blockHeight,
                        BuiltInRegistries.BLOCK
                                .getKey(background)
                                .toString(),
                        pixels
                ).resized(
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

    private void setBrushSize(
            int requestedSize
    ) {
        brushSize =
                Mth.clamp(
                        requestedSize,
                        MIN_BRUSH_SIZE,
                        MAX_BRUSH_SIZE
                );
    }

    /**
     * Paints a square brush centered as closely as possible on the cursor.
     * For even sizes, the extra row and column extend right and downward.
     * Pixels outside the current canvas are simply clipped.
     */
    private void paintBrush(
            int centerX,
            int centerY,
            byte value
    ) {
        int pixelWidth =
                blockWidth
                        * WindowDesign.PIXELS_PER_BLOCK;

        int pixelHeight =
                blockHeight
                        * WindowDesign.PIXELS_PER_BLOCK;

        int before =
                (brushSize - 1) / 2;

        int startX =
                centerX - before;

        int startY =
                centerY - before;

        for (int offsetY = 0;
             offsetY < brushSize;
             offsetY++) {

            int y =
                    startY + offsetY;

            if (y < 0
                    || y >= pixelHeight) {
                continue;
            }

            for (int offsetX = 0;
                 offsetX < brushSize;
                 offsetX++) {

                int x =
                        startX + offsetX;

                if (x < 0
                        || x >= pixelWidth) {
                    continue;
                }

                pixels[
                        y * pixelWidth + x
                        ] = value;
            }
        }
    }

    private void clearDesign() {
        Arrays.fill(
                pixels,
                WindowDesign.BACKGROUND_PIXEL
        );
    }

    private void fabricate() {
        if (!menu.hasSelectedBackground()) {

            if (minecraft != null
                    && minecraft.player != null) {

                minecraft.player
                        .displayClientMessage(
                                Component.translatable(
                                        "message.lapidary.window.choose_background"
                                ),
                                true
                        );
            }

            return;
        }

        ClientPlayNetworking.send(
                new FabricateWindowPayload(
                        menu.getContainerIdValue(),
                        blockWidth,
                        blockHeight,
                        pixels
                )
        );
    }

    private void openPicker(
            PickerMode mode
    ) {
        pickerMode =
                mode;

        pickerScroll =
                0;
    }

    private void closePicker() {
        pickerMode =
                PickerMode.NONE;

        pickerScroll =
                0;
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

    private void renderTileTooltip(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        if (isInside(
                mouseX,
                mouseY,
                BACKGROUND_TILE_X,
                BACKGROUND_TILE_Y,
                TILE_SIZE,
                TILE_SIZE
        )) {
            Block background =
                    menu.getSelectedBackgroundBlock();

            Component tooltip =
                    !menu.hasSelectedBackground()
                            ? Component.translatable(
                            "label.lapidary.window.choose_background_tile"
                    )
                            : background == Blocks.AIR
                            ? Component.translatable(
                            "label.lapidary.window.air"
                    )
                            : background.getName();

            graphics.renderTooltip(
                    font,
                    tooltip,
                    mouseX,
                    mouseY
            );

            return;
        }

        if (isInside(
                mouseX,
                mouseY,
                TEMPLATE_TILE_X,
                TEMPLATE_TILE_Y,
                TILE_SIZE,
                TILE_SIZE
        )) {
            graphics.renderTooltip(
                    font,
                    Component.translatable(
                            "label.lapidary.window.choose_template_tile"
                    ),
                    mouseX,
                    mouseY
            );
        }
    }

    private boolean isInside(
            double mouseX,
            double mouseY,
            int localX,
            int localY,
            int width,
            int height
    ) {
        int left =
                leftPos + localX;

        int top =
                topPos + localY;

        return mouseX >= left
                && mouseX < left + width
                && mouseY >= top
                && mouseY < top + height;
    }

    private enum PickerMode {
        NONE,
        BACKGROUND,
        TEMPLATE
    }

    private interface PickerEntry {

        ItemStack stack();

        Component label();
    }

    private record BackgroundEntry(
            Block block,
            int count
    ) implements PickerEntry {

        @Override
        public ItemStack stack() {
            if (block == Blocks.AIR) {
                return ItemStack.EMPTY;
            }

            return new ItemStack(
                    block.asItem(),
                    count
            );
        }

        @Override
        public Component label() {
            if (block == Blocks.AIR) {
                return Component.translatable(
                        "label.lapidary.window.air"
                );
            }

            return block.getName()
                    .copy()
                    .append(
                            Component.literal(
                                    " ×" + count
                            )
                    );
        }
    }

    private record TemplateEntry(
            ItemStack stack,
            WindowDesign design
    ) implements PickerEntry {

        @Override
        public Component label() {
            return Component.translatable(
                    "label.lapidary.window.template_entry",
                    design.blockWidth(),
                    design.blockHeight(),
                    design.backgroundBlock()
                            .getName()
            );
        }
    }
}
