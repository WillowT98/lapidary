package name.lapidary.client.screen;

import name.lapidary.client.magic.ClientMagicData;
import name.lapidary.magic.PlayerMagicData;
import name.lapidary.magic.focus.SpellcastingFocusHelper;
import name.lapidary.magic.spell.ModSpells;
import name.lapidary.magic.spell.SpellDefinition;
import name.lapidary.network.SelectPreparedSpellPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import org.lwjgl.glfw.GLFW;

public final class SpellRadialScreen
        extends Screen {

    private static final int SLOT_COUNT =
            PlayerMagicData.PREPARED_SLOT_COUNT;

    private static final double FULL_CIRCLE =
            Math.PI * 2.0D;

    private static final double WEDGE_ANGLE =
            FULL_CIRCLE / SLOT_COUNT;

    private static final int SLOT_SIZE =
            38;

    private static final int DEAD_ZONE_RADIUS =
            27;
    /**
     * If the opening packet arrives after the player has already released
     * right-click, briefly display the radial and then cancel it.
     */
    private static final int UNARMED_CLOSE_TICKS =
            8;

    private static final int SCREEN_DARKENING =
            0x60000000;

    private static final int RADIAL_BACKGROUND =
            0xD817121C;

    private static final int CENTER_BACKGROUND =
            0xEE241B2C;

    private static final int SLOT_BACKGROUND =
            0xEE312638;

    private static final int EMPTY_SLOT_BACKGROUND =
            0xDD211B25;

    private static final int BORDER_COLOR =
            0xFF735078;

    private static final int HIGHLIGHT_COLOR =
            0xFFFFB8F2;

    private static final int SELECTED_COLOR =
            0xFF53D69A;

    private static final int TEXT_COLOR =
            0xFFF3E7F5;

    private static final int MUTED_TEXT_COLOR =
            0xFFB9A8BD;

    private int ticksOpen;
    /**
     * True after the radial has observed the physical right mouse button
     * in its pressed state.
     *
     * This prevents the screen from mistaking its opening transition for
     * the player's intentional release.
     */
    private boolean sawRightButtonDown;

    private int lastHighlightedSlot =
            -1;

    private boolean completingSelection;

    public SpellRadialScreen() {
        super(
                Component.translatable(
                        "screen.lapidary.spell_radial.title"
                )
        );
    }

    @Override
    protected void init() {
        super.init();

        /*
         * The radial normally opens while right-click is already held.
         * Record that physical state immediately rather than relying upon
         * Minecraft's key-binding state, which may have been reset while
         * switching into a GUI screen.
         */
        sawRightButtonDown =
                isRightMouseButtonDown();
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        graphics.fill(
                0,
                0,
                this.width,
                this.height,
                SCREEN_DARKENING
        );

        int centerX =
                this.width / 2;

        int centerY =
                this.height / 2;

        int ringRadius =
                getRingRadius();

        int outerRadius =
                ringRadius + SLOT_SIZE / 2 + 8;

        fillCircle(
                graphics,
                centerX,
                centerY,
                outerRadius,
                RADIAL_BACKGROUND
        );

        fillCircle(
                graphics,
                centerX,
                centerY,
                DEAD_ZONE_RADIUS,
                CENTER_BACKGROUND
        );

        PlayerMagicData magicData =
                ClientMagicData.get();

        lastHighlightedSlot =
                findHighlightedSlot(
                        mouseX,
                        mouseY,
                        centerX,
                        centerY
                );

        for (int slot = 0;
             slot < SLOT_COUNT;
             slot++) {

            renderSlot(
                    graphics,
                    magicData,
                    slot,
                    centerX,
                    centerY,
                    ringRadius,
                    slot == lastHighlightedSlot
            );
        }

        renderCenter(
                graphics,
                magicData,
                centerX,
                centerY
        );

        renderLabels(
                graphics,
                magicData,
                centerX,
                centerY,
                outerRadius
        );
    }

    private void renderSlot(
            GuiGraphics graphics,
            PlayerMagicData magicData,
            int slot,
            int centerX,
            int centerY,
            int ringRadius,
            boolean highlighted
    ) {
        double angle =
                -Math.PI / 2.0D
                        + slot * WEDGE_ANGLE;

        int slotCenterX =
                centerX
                        + (int) Math.round(
                        Math.cos(angle)
                                * ringRadius
                );

        int slotCenterY =
                centerY
                        + (int) Math.round(
                        Math.sin(angle)
                                * ringRadius
                );

        int left =
                slotCenterX
                        - SLOT_SIZE / 2;

        int top =
                slotCenterY
                        - SLOT_SIZE / 2;

        Optional<ResourceLocation> spellIdOptional =
                magicData.preparedSpell(slot);

        boolean occupied =
                spellIdOptional.isPresent();

        boolean selected =
                magicData.selectedSlot() == slot;

        int borderColor;

        if (highlighted && occupied) {
            borderColor =
                    HIGHLIGHT_COLOR;
        } else if (selected) {
            borderColor =
                    SELECTED_COLOR;
        } else {
            borderColor =
                    BORDER_COLOR;
        }

        graphics.fill(
                left - 2,
                top - 2,
                left + SLOT_SIZE + 2,
                top + SLOT_SIZE + 2,
                borderColor
        );

        graphics.fill(
                left,
                top,
                left + SLOT_SIZE,
                top + SLOT_SIZE,
                occupied
                        ? SLOT_BACKGROUND
                        : EMPTY_SLOT_BACKGROUND
        );

        graphics.drawString(
                this.font,
                Integer.toString(slot + 1),
                left + 3,
                top + 3,
                MUTED_TEXT_COLOR,
                false
        );

        if (selected) {
            graphics.fill(
                    left + SLOT_SIZE - 7,
                    top + 3,
                    left + SLOT_SIZE - 3,
                    top + 7,
                    SELECTED_COLOR
            );
        }

        if (spellIdOptional.isEmpty()) {
            graphics.drawCenteredString(
                    this.font,
                    "—",
                    slotCenterX,
                    slotCenterY - 4,
                    MUTED_TEXT_COLOR
            );

            return;
        }

        ResourceLocation spellId =
                spellIdOptional.get();

        Optional<SpellDefinition> definitionOptional =
                ModSpells.get(spellId);

        if (definitionOptional.isEmpty()) {
            graphics.drawCenteredString(
                    this.font,
                    "?",
                    slotCenterX,
                    slotCenterY - 4,
                    TEXT_COLOR
            );

            return;
        }

        ItemStack icon =
                definitionOptional
                        .get()
                        .iconStack();

        if (!icon.isEmpty()) {
            graphics.renderItem(
                    icon,
                    slotCenterX - 8,
                    slotCenterY - 8
            );
        } else {
            graphics.drawCenteredString(
                    this.font,
                    getInitials(
                            definitionOptional
                                    .get()
                                    .displayName()
                                    .getString()
                    ),
                    slotCenterX,
                    slotCenterY - 4,
                    TEXT_COLOR
            );
        }
    }

    private void renderCenter(
            GuiGraphics graphics,
            PlayerMagicData magicData,
            int centerX,
            int centerY
    ) {
        int selectedSlot =
                magicData.selectedSlot();

        graphics.drawCenteredString(
                this.font,
                Integer.toString(
                        selectedSlot + 1
                ),
                centerX,
                centerY - 4,
                TEXT_COLOR
        );
    }

    private void renderLabels(
            GuiGraphics graphics,
            PlayerMagicData magicData,
            int centerX,
            int centerY,
            int outerRadius
    ) {
        graphics.drawCenteredString(
                this.font,
                this.title,
                centerX,
                centerY - outerRadius - 22,
                TEXT_COLOR
        );

        Component spellLabel =
                getDisplayedSpellLabel(
                        magicData
                );

        graphics.drawCenteredString(
                this.font,
                spellLabel,
                centerX,
                centerY + outerRadius + 9,
                TEXT_COLOR
        );

        Component instruction;

        if (lastHighlightedSlot >= 0
                && magicData.preparedSpell(
                lastHighlightedSlot
        ).isPresent()) {

            instruction =
                    Component.translatable(
                            "screen.lapidary.spell_radial.release"
                    );
        } else {
            instruction =
                    Component.translatable(
                            "screen.lapidary.spell_radial.cancel"
                    );
        }

        graphics.drawCenteredString(
                this.font,
                instruction,
                centerX,
                centerY + outerRadius + 21,
                MUTED_TEXT_COLOR
        );
    }

    private Component getDisplayedSpellLabel(
            PlayerMagicData magicData
    ) {
        if (lastHighlightedSlot >= 0) {
            Optional<ResourceLocation> highlighted =
                    magicData.preparedSpell(
                            lastHighlightedSlot
                    );

            if (highlighted.isEmpty()) {
                return Component.translatable(
                        "screen.lapidary.spell_radial.empty"
                );
            }

            return getSpellName(
                    highlighted.get()
            );
        }

        Optional<ResourceLocation> selected =
                magicData.selectedSpell();

        if (selected.isEmpty()) {
            return Component.translatable(
                    "screen.lapidary.spell_radial.none_selected"
            );
        }

        return Component.translatable(
                "screen.lapidary.spell_radial.selected",
                getSpellName(
                        selected.get()
                )
        );
    }

    private static Component getSpellName(
            ResourceLocation spellId
    ) {
        return ModSpells.get(spellId)
                .map(
                        SpellDefinition::displayName
                )
                .orElseGet(
                        () -> Component.literal(
                                spellId.toString()
                        )
                );
    }

    private int findHighlightedSlot(
            double mouseX,
            double mouseY,
            int centerX,
            int centerY
    ) {
        double offsetX =
                mouseX - centerX;

        double offsetY =
                mouseY - centerY;

        double distanceSquared =
                offsetX * offsetX
                        + offsetY * offsetY;

        if (distanceSquared
                < DEAD_ZONE_RADIUS
                * DEAD_ZONE_RADIUS) {

            return -1;
        }

        /*
         * Convert atan2's right-facing zero into a top-facing zero,
         * then offset by half a wedge so each cardinal direction lies
         * at the center of its wedge.
         */
        double angle =
                Math.atan2(
                        offsetY,
                        offsetX
                )
                        + Math.PI / 2.0D;

        angle =
                (
                        angle
                                + WEDGE_ANGLE / 2.0D
                                + FULL_CIRCLE
                ) % FULL_CIRCLE;

        return (int) Math.floor(
                angle / WEDGE_ANGLE
        ) % SLOT_COUNT;
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        /*
         * The screen is controlled by cursor direction and releasing
         * the original right click. Other mouse presses are consumed.
         */
        return true;
    }

    @Override
    public boolean mouseReleased(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button
                == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {

            int highlightedSlot =
                    findHighlightedSlot(
                            mouseX,
                            mouseY,
                            this.width / 2,
                            this.height / 2
                    );

            completeSelection(
                    highlightedSlot
            );

            return true;
        }

        return true;
    }

    @Override
    public void tick() {
        super.tick();

        ticksOpen++;

        if (this.minecraft == null
                || this.minecraft.player == null) {

            onClose();
            return;
        }

        /*
         * The radial is relevant only while the player continues holding
         * a staff or another spellcasting focus in the main hand.
         */
        if (!SpellcastingFocusHelper
                .isHoldingFocus(
                        this.minecraft.player
                )) {

            onClose();
            return;
        }

        boolean rightButtonDown =
                isRightMouseButtonDown();

        if (rightButtonDown) {
            /*
             * The player is physically holding right-click. Keep the
             * radial open and arm it for release-based selection.
             */
            sawRightButtonDown =
                    true;

            return;
        }

        if (sawRightButtonDown) {
            /*
             * We previously observed the button being held and now observe
             * it released. This is the intentional selection gesture.
             *
             * mouseReleased() normally handles this first, but polling here
             * is a reliable fallback if the release event is not delivered
             * to the newly opened screen.
             */
            completeSelection(
                    lastHighlightedSlot
            );

            return;
        }

        /*
         * The opening packet may occasionally arrive after a very quick
         * right-click has already been released. In that case there is no
         * held gesture to complete, so close without changing the selected
         * spell after a short grace period.
         */
        if (ticksOpen
                > UNARMED_CLOSE_TICKS) {

            onClose();
        }
    }

    private void completeSelection(
            int slot
    ) {
        if (completingSelection) {
            return;
        }

        completingSelection =
                true;

        PlayerMagicData magicData =
                ClientMagicData.get();

        if (PlayerMagicData.isValidSlot(slot)
                && magicData.preparedSpell(
                slot
        ).isPresent()) {

            /*
             * Update immediately for responsive client presentation.
             * The server synchronization remains authoritative.
             */
            ClientMagicData.set(
                    magicData.withSelectedSlot(
                            slot
                    )
            );

            ClientPlayNetworking.send(
                    new SelectPreparedSpellPayload(
                            slot
                    )
            );
        }

        onClose();
    }
    private boolean isRightMouseButtonDown() {
        if (this.minecraft == null) {
            return false;
        }

        long windowHandle =
                this.minecraft
                        .getWindow()
                        .getWindow();

        return GLFW.glfwGetMouseButton(
                windowHandle,
                GLFW.GLFW_MOUSE_BUTTON_RIGHT
        ) == GLFW.GLFW_PRESS;
    }

    private int getRingRadius() {
        int availableRadius =
                Math.min(
                        this.width,
                        this.height
                ) / 4;

        return Math.max(
                58,
                Math.min(
                        82,
                        availableRadius
                )
        );
    }

    private static String getInitials(
            String name
    ) {
        String[] words =
                name.trim()
                        .split("\\s+");

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

        return result.isEmpty()
                ? "?"
                : result.toString();
    }

    private static void fillCircle(
            GuiGraphics graphics,
            int centerX,
            int centerY,
            int radius,
            int color
    ) {
        int radiusSquared =
                radius * radius;

        for (int y = -radius;
             y <= radius;
             y++) {

            int horizontalRadius =
                    (int) Math.floor(
                            Math.sqrt(
                                    radiusSquared
                                            - y * y
                            )
                    );

            graphics.fill(
                    centerX - horizontalRadius,
                    centerY + y,
                    centerX + horizontalRadius + 1,
                    centerY + y + 1,
                    color
            );
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}