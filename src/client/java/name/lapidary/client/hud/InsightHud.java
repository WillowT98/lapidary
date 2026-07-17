package name.lapidary.client.hud;

import name.lapidary.client.progression.ClientInsightData;
import name.lapidary.progression.LapidaryInsight;
import name.lapidary.tag.ModItemTags;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

public final class InsightHud {

    /*
     * Vanilla's experience bar is 182 pixels wide.
     *
     * This entire display uses the same total width, but reserves the
     * rightmost 24 pixels for the number.
     */
    private static final int TOTAL_WIDTH = 182;
    private static final int BAR_WIDTH = 158;
    private static final int BAR_HEIGHT = 5;

    /*
     * Distance between the display and the bottom of the screen.
     *
     * Increasing this number moves the display upward.
     * Decreasing it moves the display downward.
     */
    private static final int BOTTOM_OFFSET = 44;

    /*
     * ARGB colors.
     */
    private static final int BORDER_COLOR = 0xFF3A0A32;
    private static final int EMPTY_COLOR = 0xCC140511;
    private static final int FILL_COLOR = 0xFFD629B5;
    private static final int HIGHLIGHT_COLOR = 0xFFFF6FDC;
    private static final int TEXT_COLOR = 0xFFFFA3E8;

    private InsightHud() {
    }

    public static void initialize() {
        HudRenderCallback.EVENT.register(
                (guiGraphics, tickCounter) ->
                        render(guiGraphics)
        );
    }

    private static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null) {
            return;
        }

        if (!shouldShow(player)) {
            return;
        }

        int insight = ClientInsightData.get();

        /*
         * Center the full 182-pixel display on the screen.
         */
        int x = (graphics.guiWidth() - TOTAL_WIDTH) / 2;
        int y = graphics.guiHeight() - BOTTOM_OFFSET;

        /*
         * The one-pixel border leaves this much usable interior space.
         */
        int innerWidth = BAR_WIDTH - 2;

        double progress =
                insight
                        / (double) LapidaryInsight.MAX_INSIGHT;

        int filledWidth = (int) Math.round(
                innerWidth * progress
        );

        /*
         * Draw the dark outer border.
         */
        graphics.fill(
                x,
                y,
                x + BAR_WIDTH,
                y + BAR_HEIGHT,
                BORDER_COLOR
        );

        /*
         * Draw the unfilled interior.
         */
        graphics.fill(
                x + 1,
                y + 1,
                x + BAR_WIDTH - 1,
                y + BAR_HEIGHT - 1,
                EMPTY_COLOR
        );

        /*
         * Draw the filled portion.
         */
        if (filledWidth > 0) {
            graphics.fill(
                    x + 1,
                    y + 1,
                    x + 1 + filledWidth,
                    y + BAR_HEIGHT - 1,
                    FILL_COLOR
            );

            /*
             * A one-pixel brighter upper edge gives the otherwise simple
             * rectangle a slightly polished appearance.
             */
            graphics.fill(
                    x + 1,
                    y + 1,
                    x + 1 + filledWidth,
                    y + 2,
                    HIGHLIGHT_COLOR
            );
        }

        /*
         * Right-align the exact total in the reserved space.
         */
        String valueText = Integer.toString(insight);

        int textX =
                x
                        + TOTAL_WIDTH
                        - minecraft.font.width(valueText);

        int textY = y - 2;

        graphics.drawString(
                minecraft.font,
                valueText,
                textX,
                textY,
                TEXT_COLOR,
                true
        );
    }

    private static boolean shouldShow(Player player) {
        return player.getMainHandItem()
                .is(ModItemTags.SHOWS_INSIGHT_BAR)
                || player.getOffhandItem()
                .is(ModItemTags.SHOWS_INSIGHT_BAR);
    }
}