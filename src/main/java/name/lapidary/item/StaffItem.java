package name.lapidary.item;

import name.lapidary.magic.focus.SpellcastingFocus;
import net.minecraft.world.item.Item;

public final class StaffItem
        extends Item
        implements SpellcastingFocus {

    public StaffItem(Properties properties) {
        super(properties);
    }
}