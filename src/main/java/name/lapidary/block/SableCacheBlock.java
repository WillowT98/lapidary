package name.lapidary.block;



import name.lapidary.block.entity.ModBlockEntities;
import name.lapidary.block.entity.SableCacheBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class SableCacheBlock extends ChestBlock {

    public SableCacheBlock(
            BlockBehaviour.Properties properties
    ) {
        /*
         * ChestBlock stores this supplier and uses it when checking the
         * type of block entity associated with the chest.
         */
        super(
                properties,
                () -> ModBlockEntities.SABLE_CACHE
        );
    }

    @Override
    public SableCacheBlockEntity newBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        return new SableCacheBlockEntity(
                position,
                state
        );
    }

    @Override
    public void playerDestroy(
            Level level,
            Player player,
            BlockPos position,
            BlockState state,
            BlockEntity blockEntity,
            ItemStack tool
    ) {
        /*
         * Notify the sable while the block-entity information is still
         * available.
         */
        if (!level.isClientSide
                && blockEntity
                instanceof SableCacheBlockEntity cache) {

            cache.onBrokenBy(player);
        }

        super.playerDestroy(
                level,
                player,
                position,
                state,
                blockEntity,
                tool
        );
    }
}