package name.lapidary.item;

import name.lapidary.Lapidary;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.PatchouliAPI;

public final class LapidaryGuidebookItem extends Item {

    private static final ResourceLocation BOOK_ID =
            Lapidary.id("lapidary_guidebook");

    public LapidaryGuidebookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()
                && player instanceof ServerPlayer serverPlayer) {
            PatchouliAPI.get().openBookGUI(
                    serverPlayer,
                    BOOK_ID
            );
        }

        return InteractionResultHolder.sidedSuccess(
                stack,
                level.isClientSide()
        );
    }
}