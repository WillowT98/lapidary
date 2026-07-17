package name.lapidary.item;

import name.lapidary.tag.ModBlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class SieveItem extends Item {
    /*
     * Lower values make the process slower.
     * Start here, then adjust it after testing in-game.
     */
    private static final float SIFTING_SPEED = 0.35F;

    public SieveItem(Properties properties) {
        super(properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (state.is(ModBlockTags.SIFTABLE_BY_SIEVE)) {
            return SIFTING_SPEED;
        }

        return super.getDestroySpeed(stack, state);
    }
}