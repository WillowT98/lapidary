package name.lapidary.client.hud;

import name.lapidary.client.origin.ClientOriginState;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class OriginHud {

    private static final int WIDTH =
            120;

    private static final int HEIGHT =
            7;

    private OriginHud() {
    }

    public static void initialize() {
        HudRenderCallback.EVENT.register(
                (
                        graphics,
                        tickCounter
                ) -> render(
                        graphics
                )
        );
    }

    private static void render(
            GuiGraphics graphics
    ) {
        Minecraft minecraft =
                Minecraft.getInstance();

        if (minecraft.player == null
                || minecraft.options
                .hideGui) {

            return;
        }

        int maximum =
                ClientOriginState.maximum();

        if (maximum <= 0) {
            return;
        }

        int resource =
                Math.max(
                        0,
                        Math.min(
                                maximum,
                                ClientOriginState.resource()
                        )
                );

        int x =
                (
                        graphics.guiWidth()
                                - WIDTH
                ) / 2;

        int y =
                graphics.guiHeight()
                        - 58;

        int fill =
                (int) Math.round(
                        (
                                WIDTH - 2
                        )
                                * (
                                resource
                                        / (double) maximum
                        )
                );

        int fillColor =
                ClientOriginState.originKind() == 2
                        ? 0xFFE2B6FF
                        : 0xFFFFF09A;

        graphics.fill(
                x,
                y,
                x + WIDTH,
                y + HEIGHT,
                0xFF17131C
        );

        graphics.fill(
                x + 1,
                y + 1,
                x + WIDTH - 1,
                y + HEIGHT - 1,
                0xCC30273A
        );

        if (fill > 0) {
            graphics.fill(
                    x + 1,
                    y + 1,
                    x + 1 + fill,
                    y + HEIGHT - 1,
                    fillColor
            );
        }

        String text =
                resource
                        + " / "
                        + maximum;

        graphics.drawCenteredString(
                minecraft.font,
                text,
                x + WIDTH / 2,
                y - 9,
                0xFFFFFFFF
        );
    }
}
