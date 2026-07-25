package name.lapidary.client.screen;

import name.lapidary.client.magic.ClientMagicData;
import name.lapidary.magic.PlayerMagicData;
import name.lapidary.magic.spell.ModSpells;
import name.lapidary.magic.spell.SpellDefinition;
import name.lapidary.network.TomeClearPreparedSpellPayload;
import name.lapidary.network.TomePrepareSpellPayload;
import name.lapidary.network.TomeSwapPreparedSpellsPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class TomeCalibrationPanel {

    private static final int TEXT_COLOR =
            0xFFF3E7F5;

    private static final int MUTED_TEXT_COLOR =
            0xFFB9A8BD;

    private static final int BORDER_COLOR =
            0xFF735078;

    private static final int CARD_COLOR =
            0xFF312638;

    private static final int CARD_HOVERED_COLOR =
            0xFF5B4164;

    private static final int SLOT_COLOR =
            0xFF211A27;

    private static final int SLOT_SELECTED_COLOR =
            0xFF84558F;

    private static final int KNOWN_COLUMNS = 2;
    private static final int KNOWN_CARD_HEIGHT = 22;
    private static final int CARD_GAP = 4;

    private static final int SLOT_COLUMNS = 4;
    private static final int SLOT_SIZE = 26;
    private static final int SLOT_GAP = 4;

    private final BlockPos tablePosition;

    private PlayerMagicData data;

    private ResourceLocation draggedSpell;
    private int draggedPreparedSlot = -1;

    public TomeCalibrationPanel(
            BlockPos tablePosition,
            PlayerMagicData initialData
    ) {
        this.tablePosition =
                tablePosition.immutable();

        this.data =
                initialData;
    }

    public void updateData(
            PlayerMagicData newData
    ) {
        this.data =
                newData;
    }

    public void render(
            GuiGraphics graphics,
            Font font,
            int left,
            int top,
            int right,
            int bottom,
            int mouseX,
            int mouseY
    ) {
        int preparedLeft =
                getPreparedLeft(right);

        int knownRight =
                preparedLeft - 12;

        graphics.drawString(
                font,
                Component.translatable(
                        "screen.lapidary.tome.calibration.known"
                ),
                left,
                top,
                TEXT_COLOR,
                false
        );

        graphics.drawString(
                font,
                Component.translatable(
                        "screen.lapidary.tome.calibration.prepared"
                ),
                preparedLeft,
                top,
                TEXT_COLOR,
                false
        );

        graphics.fill(
                knownRight + 5,
                top,
                knownRight + 6,
                bottom,
                BORDER_COLOR
        );

        renderKnownSpells(
                graphics,
                font,
                left,
                top + 15,
                knownRight,
                mouseX,
                mouseY
        );

        renderPreparedSlots(
                graphics,
                font,
                preparedLeft,
                top + 20,
                mouseX,
                mouseY
        );

        graphics.drawString(
                font,
                Component.translatable(
                        "screen.lapidary.tome.calibration.hint"
                ),
                preparedLeft,
                bottom - font.lineHeight,
                MUTED_TEXT_COLOR,
                false
        );

        if (draggedSpell != null) {
            renderDraggedSpell(
                    graphics,
                    font,
                    mouseX,
                    mouseY
            );
        }
    }

    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button,
            int left,
            int top,
            int right,
            int bottom
    ) {
        int preparedLeft =
                getPreparedLeft(right);

        int knownRight =
                preparedLeft - 12;

        int preparedSlot =
                findPreparedSlotAt(
                        mouseX,
                        mouseY,
                        preparedLeft,
                        top + 20
                );

        /*
         * Right-click clears immediately.
         */
        if (button == 1
                && preparedSlot >= 0
                && data.preparedSpell(
                preparedSlot
        ).isPresent()) {

            applyOptimistic(
                    data.withoutPreparedSpell(
                            preparedSlot
                    )
            );

            ClientPlayNetworking.send(
                    new TomeClearPreparedSpellPayload(
                            tablePosition,
                            preparedSlot
                    )
            );

            return true;
        }

        if (button != 0) {
            return false;
        }

        /*
         * A prepared spell can be dragged to another prepared slot.
         */
        if (preparedSlot >= 0) {
            Optional<ResourceLocation> prepared =
                    data.preparedSpell(
                            preparedSlot
                    );

            if (prepared.isPresent()) {
                draggedSpell =
                        prepared.get();

                draggedPreparedSlot =
                        preparedSlot;

                return true;
            }
        }

        /*
         * A known spell can be dragged into any prepared slot.
         */
        ResourceLocation knownSpell =
                findKnownSpellAt(
                        mouseX,
                        mouseY,
                        left,
                        top + 15,
                        knownRight
                );

        if (knownSpell != null) {
            draggedSpell =
                    knownSpell;

            draggedPreparedSlot =
                    -1;

            return true;
        }

        return false;
    }

    public boolean mouseReleased(
            double mouseX,
            double mouseY,
            int button,
            int left,
            int top,
            int right,
            int bottom
    ) {
        if (button != 0
                || draggedSpell == null) {

            return false;
        }

        int preparedLeft =
                getPreparedLeft(right);

        int targetSlot =
                findPreparedSlotAt(
                        mouseX,
                        mouseY,
                        preparedLeft,
                        top + 20
                );

        if (targetSlot >= 0) {
            if (draggedPreparedSlot >= 0) {
                if (draggedPreparedSlot
                        != targetSlot) {

                    int sourceSlot =
                            draggedPreparedSlot;

                    applyOptimistic(
                            data.withSwappedSlots(
                                    sourceSlot,
                                    targetSlot
                            )
                    );

                    ClientPlayNetworking.send(
                            new TomeSwapPreparedSpellsPayload(
                                    tablePosition,
                                    sourceSlot,
                                    targetSlot
                            )
                    );
                }
            } else {
                ResourceLocation spellId =
                        draggedSpell;

                applyOptimistic(
                        data.withPreparedSpell(
                                targetSlot,
                                spellId
                        )
                );

                ClientPlayNetworking.send(
                        new TomePrepareSpellPayload(
                                tablePosition,
                                targetSlot,
                                spellId
                        )
                );
            }
        }

        clearDrag();

        return true;
    }

    private void renderKnownSpells(
            GuiGraphics graphics,
            Font font,
            int left,
            int top,
            int right,
            int mouseX,
            int mouseY
    ) {
        List<ResourceLocation> knownSpells =
                getRegisteredKnownSpells();

        if (knownSpells.isEmpty()) {
            graphics.drawCenteredString(
                    font,
                    Component.translatable(
                            "screen.lapidary.tome.calibration.empty"
                    ),
                    (left + right) / 2,
                    top + 20,
                    MUTED_TEXT_COLOR
            );

            return;
        }

        int cardWidth =
                getKnownCardWidth(
                        left,
                        right
                );

        for (int index = 0;
             index < knownSpells.size();
             index++) {

            int column =
                    index % KNOWN_COLUMNS;

            int row =
                    index / KNOWN_COLUMNS;

            int x =
                    left
                            + column
                            * (
                            cardWidth
                                    + CARD_GAP
                    );

            int y =
                    top
                            + row
                            * (
                            KNOWN_CARD_HEIGHT
                                    + CARD_GAP
                    );

            boolean hovered =
                    isInside(
                            mouseX,
                            mouseY,
                            x,
                            y,
                            cardWidth,
                            KNOWN_CARD_HEIGHT
                    );

            ResourceLocation spellId =
                    knownSpells.get(index);

            graphics.fill(
                    x,
                    y,
                    x + cardWidth,
                    y + KNOWN_CARD_HEIGHT,
                    BORDER_COLOR
            );

            graphics.fill(
                    x + 1,
                    y + 1,
                    x + cardWidth - 1,
                    y + KNOWN_CARD_HEIGHT - 1,
                    hovered
                            ? CARD_HOVERED_COLOR
                            : CARD_COLOR
            );

            String displayName =
                    getDisplayName(
                            spellId
                    );

            displayName =
                    font.plainSubstrByWidth(
                            displayName,
                            cardWidth - 8
                    );

            graphics.drawString(
                    font,
                    displayName,
                    x + 4,
                    y + (
                            KNOWN_CARD_HEIGHT
                                    - font.lineHeight
                    ) / 2,
                    TEXT_COLOR,
                    false
            );
        }
    }

    private void renderPreparedSlots(
            GuiGraphics graphics,
            Font font,
            int left,
            int top,
            int mouseX,
            int mouseY
    ) {
        for (int slot = 0;
             slot
                     < PlayerMagicData
                     .PREPARED_SLOT_COUNT;
             slot++) {

            int x =
                    getPreparedSlotX(
                            left,
                            slot
                    );

            int y =
                    getPreparedSlotY(
                            top,
                            slot
                    );

            boolean hovered =
                    isInside(
                            mouseX,
                            mouseY,
                            x,
                            y,
                            SLOT_SIZE,
                            SLOT_SIZE
                    );

            boolean selected =
                    data.selectedSlot()
                            == slot;

            graphics.fill(
                    x - 1,
                    y - 1,
                    x + SLOT_SIZE + 1,
                    y + SLOT_SIZE + 1,
                    selected
                            ? SLOT_SELECTED_COLOR
                            : BORDER_COLOR
            );

            graphics.fill(
                    x,
                    y,
                    x + SLOT_SIZE,
                    y + SLOT_SIZE,
                    hovered
                            ? CARD_HOVERED_COLOR
                            : SLOT_COLOR
            );

            graphics.drawString(
                    font,
                    Integer.toString(
                            slot + 1
                    ),
                    x + 2,
                    y + 2,
                    MUTED_TEXT_COLOR,
                    false
            );

            Optional<ResourceLocation> spell =
                    data.preparedSpell(slot);

            if (spell.isEmpty()) {
                continue;
            }

            graphics.drawCenteredString(
                    font,
                    getShortLabel(
                            spell.get()
                    ),
                    x + SLOT_SIZE / 2,
                    y + 12,
                    TEXT_COLOR
            );
        }
    }

    private void renderDraggedSpell(
            GuiGraphics graphics,
            Font font,
            int mouseX,
            int mouseY
    ) {
        String label =
                getDisplayName(
                        draggedSpell
                );

        int width =
                Math.min(
                        120,
                        font.width(label) + 8
                );

        label =
                font.plainSubstrByWidth(
                        label,
                        width - 8
                );

        int x =
                mouseX + 8;

        int y =
                mouseY + 8;

        graphics.fill(
                x,
                y,
                x + width,
                y + 16,
                BORDER_COLOR
        );

        graphics.fill(
                x + 1,
                y + 1,
                x + width - 1,
                y + 15,
                CARD_HOVERED_COLOR
        );

        graphics.drawString(
                font,
                label,
                x + 4,
                y + 4,
                TEXT_COLOR,
                false
        );
    }

    private ResourceLocation findKnownSpellAt(
            double mouseX,
            double mouseY,
            int left,
            int top,
            int right
    ) {
        List<ResourceLocation> knownSpells =
                getRegisteredKnownSpells();

        int cardWidth =
                getKnownCardWidth(
                        left,
                        right
                );

        for (int index = 0;
             index < knownSpells.size();
             index++) {

            int column =
                    index % KNOWN_COLUMNS;

            int row =
                    index / KNOWN_COLUMNS;

            int x =
                    left
                            + column
                            * (
                            cardWidth
                                    + CARD_GAP
                    );

            int y =
                    top
                            + row
                            * (
                            KNOWN_CARD_HEIGHT
                                    + CARD_GAP
                    );

            if (isInside(
                    mouseX,
                    mouseY,
                    x,
                    y,
                    cardWidth,
                    KNOWN_CARD_HEIGHT
            )) {
                return knownSpells.get(index);
            }
        }

        return null;
    }

    private int findPreparedSlotAt(
            double mouseX,
            double mouseY,
            int left,
            int top
    ) {
        for (int slot = 0;
             slot
                     < PlayerMagicData
                     .PREPARED_SLOT_COUNT;
             slot++) {

            int x =
                    getPreparedSlotX(
                            left,
                            slot
                    );

            int y =
                    getPreparedSlotY(
                            top,
                            slot
                    );

            if (isInside(
                    mouseX,
                    mouseY,
                    x,
                    y,
                    SLOT_SIZE,
                    SLOT_SIZE
            )) {
                return slot;
            }
        }

        return -1;
    }

    private List<ResourceLocation>
    getRegisteredKnownSpells() {
        List<ResourceLocation> result =
                new ArrayList<>();

        for (String rawId :
                data.knownSpells()) {

            ResourceLocation spellId =
                    ResourceLocation.tryParse(
                            rawId
                    );

            if (spellId == null
                    || !ModSpells.contains(
                    spellId
            )) {
                continue;
            }

            result.add(spellId);
        }

        result.sort(
                Comparator.comparing(
                        this::getDisplayName
                )
        );

        return List.copyOf(result);
    }

    private String getDisplayName(
            ResourceLocation spellId
    ) {
        return ModSpells.get(spellId)
                .map(
                        SpellDefinition
                                ::displayName
                )
                .orElseGet(
                        () ->
                                Component.literal(
                                        spellId.toString()
                                )
                )
                .getString();
    }

    private static String getShortLabel(
            ResourceLocation spellId
    ) {
        String path =
                spellId.getPath();

        String[] words =
                path.split("[_/]");

        StringBuilder result =
                new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(
                        Character.toUpperCase(
                                word.charAt(0)
                        )
                );
            }

            if (result.length() >= 3) {
                break;
            }
        }

        if (result.isEmpty()) {
            return "?";
        }

        return result.toString();
    }

    private void applyOptimistic(
            PlayerMagicData newData
    ) {
        this.data =
                newData;

        ClientMagicData.set(
                newData
        );
    }

    private void clearDrag() {
        draggedSpell =
                null;

        draggedPreparedSlot =
                -1;
    }

    private static int getPreparedLeft(
            int right
    ) {
        int preparedWidth =
                SLOT_COLUMNS
                        * SLOT_SIZE
                        + (
                        SLOT_COLUMNS - 1
                )
                        * SLOT_GAP;

        return right - preparedWidth;
    }

    private static int getKnownCardWidth(
            int left,
            int right
    ) {
        return Math.max(
                1,
                (
                        right
                                - left
                                - CARD_GAP
                ) / KNOWN_COLUMNS
        );
    }

    private static int getPreparedSlotX(
            int left,
            int slot
    ) {
        int column =
                slot % SLOT_COLUMNS;

        return left
                + column
                * (
                SLOT_SIZE
                        + SLOT_GAP
        );
    }

    private static int getPreparedSlotY(
            int top,
            int slot
    ) {
        int row =
                slot / SLOT_COLUMNS;

        return top
                + row
                * (
                SLOT_SIZE
                        + SLOT_GAP
        );
    }

    private static boolean isInside(
            double mouseX,
            double mouseY,
            int x,
            int y,
            int width,
            int height
    ) {
        return mouseX >= x
                && mouseX < x + width
                && mouseY >= y
                && mouseY < y + height;
    }
}