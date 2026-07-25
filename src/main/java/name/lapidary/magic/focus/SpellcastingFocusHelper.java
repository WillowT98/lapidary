package name.lapidary.magic.focus;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class SpellcastingFocusHelper {

    private SpellcastingFocusHelper() {
    }

    public static boolean isFocus(ItemStack stack) {
        return !stack.isEmpty()
                && stack.getItem()
                instanceof SpellcastingFocus;
    }

    public static boolean isHoldingFocus(Player player) {
        return isFocus(player.getMainHandItem());
    }
}