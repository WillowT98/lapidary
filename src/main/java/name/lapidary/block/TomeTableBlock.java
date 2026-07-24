package name.lapidary.block;

import name.lapidary.progression.tome.TomeProgression;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class TomeTableBlock
        extends Block {

    public TomeTableBlock(
            BlockBehaviour.Properties properties
    ) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            BlockHitResult hitResult
    ) {
        if (!level.isClientSide
                && player
                instanceof ServerPlayer serverPlayer) {

            TomeProgression.openScreen(
                    serverPlayer,
                    position
            );
        }

        return InteractionResult.sidedSuccess(
                level.isClientSide
        );
    }
}