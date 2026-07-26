package name.lapidary.screen;

import name.lapidary.block.ModBlocks;
import name.lapidary.item.CustomStainedGlassItem;
import name.lapidary.window.WindowDesign;
import name.lapidary.window.WindowMaterials;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class StainedGlassFabricatorMenu
        extends AbstractContainerMenu {

    private static final int NO_BACKGROUND =
            -1;

    private final ContainerLevelAccess access;

    /*
     * The selected background is synchronized as a numeric block registry
     * ID. No inventory slot is needed: the server validates that the player
     * actually carries the selected block whenever selection changes and
     * again when fabrication occurs.
     */
    private final DataSlot selectedBackgroundId =
            DataSlot.standalone();

    public StainedGlassFabricatorMenu(
            int syncId,
            Inventory inventory
    ) {
        this(
                syncId,
                inventory,
                ContainerLevelAccess.NULL
        );
    }

    public StainedGlassFabricatorMenu(
            int syncId,
            Inventory inventory,
            ContainerLevelAccess access
    ) {
        super(
                ModMenus.STAINED_GLASS_FABRICATOR,
                syncId
        );

        this.access =
                access;

        selectedBackgroundId.set(
                NO_BACKGROUND
        );

        addDataSlot(
                selectedBackgroundId
        );
    }

    public int getContainerIdValue() {
        return this.containerId;
    }

    public Block getSelectedBackgroundBlock() {
        int registryId =
                selectedBackgroundId.get();

        if (registryId < 0) {
            return Blocks.AIR;
        }

        Block block =
                BuiltInRegistries.BLOCK
                        .byId(
                                registryId
                        );

        return isUsableBackground(block)
                ? block
                : Blocks.AIR;
    }

    public boolean selectBackground(
            ServerPlayer player,
            int registryId
    ) {
        if (!stillValid(player)) {
            return false;
        }

        Block block =
                BuiltInRegistries.BLOCK
                        .byId(
                                registryId
                        );

        if (!isUsableBackground(block)
                || !playerCarries(
                player,
                block
        )) {
            return false;
        }

        selectedBackgroundId.set(
                BuiltInRegistries.BLOCK
                        .getId(block)
        );

        broadcastChanges();

        return true;
    }

    public boolean fabricate(
            ServerPlayer player,
            int blockWidth,
            int blockHeight,
            byte[] pixels
    ) {
        if (!stillValid(player)) {
            return false;
        }

        Block background =
                getSelectedBackgroundBlock();

        if (!isUsableBackground(background)
                || !playerCarries(
                player,
                background
        )) {
            player.displayClientMessage(
                    Component.translatable(
                            "message.lapidary.window.choose_background"
                    ),
                    true
            );

            selectedBackgroundId.set(
                    NO_BACKGROUND
            );

            broadcastChanges();

            return false;
        }

        WindowDesign design;

        try {
            design =
                    new WindowDesign(
                            blockWidth,
                            blockHeight,
                            BuiltInRegistries.BLOCK
                                    .getKey(background)
                                    .toString(),
                            pixels
                    );
        } catch (IllegalArgumentException exception) {
            return false;
        }

        if (!WindowMaterials.canAfford(
                player,
                design
        )) {
            player.displayClientMessage(
                    WindowMaterials.firstMissingMaterial(
                            player,
                            design
                    ),
                    true
            );

            return false;
        }

        WindowMaterials.consume(
                player,
                design
        );

        ItemStack output =
                CustomStainedGlassItem.create(
                        design
                );

        if (!player.getInventory()
                .add(output)) {

            player.drop(
                    output,
                    false
            );
        }

        /*
         * Consuming the last copy of a background block invalidates the
         * current selection. Clear it so the screen cannot imply that the
         * player still has material available.
         */
        if (!playerCarries(
                player,
                background
        )) {
            selectedBackgroundId.set(
                    NO_BACKGROUND
            );
        }

        broadcastChanges();

        player.displayClientMessage(
                Component.translatable(
                        "message.lapidary.window.fabricated"
                ),
                true
        );

        return true;
    }

    @Override
    public boolean stillValid(
            Player player
    ) {
        return stillValid(
                access,
                player,
                ModBlocks.STAINED_GLASS_FABRICATOR
        );
    }

    @Override
    public ItemStack quickMoveStack(
            Player player,
            int slotIndex
    ) {
        return ItemStack.EMPTY;
    }

    private static boolean isUsableBackground(
            Block block
    ) {
        return block != Blocks.AIR
                && block.asItem()
                instanceof BlockItem;
    }

    private static boolean playerCarries(
            Player player,
            Block block
    ) {
        for (ItemStack stack :
                player.getInventory()
                        .items) {

            if (stack.is(
                    block.asItem()
            )) {
                return true;
            }
        }

        for (ItemStack stack :
                player.getInventory()
                        .offhand) {

            if (stack.is(
                    block.asItem()
            )) {
                return true;
            }
        }

        return false;
    }
}
