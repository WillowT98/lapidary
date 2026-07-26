package name.lapidary.screen;

import name.lapidary.block.ModBlocks;
import name.lapidary.item.CustomStainedGlassItem;
import name.lapidary.item.ModItems;
import name.lapidary.window.WindowDesign;
import name.lapidary.window.WindowMaterials;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class StainedGlassFabricatorMenu
        extends AbstractContainerMenu {

    private static final int TEMPLATE_SLOT =
            0;

    private static final int PLAYER_INVENTORY_START =
            1;

    private static final int PLAYER_INVENTORY_END =
            28;

    private static final int HOTBAR_START =
            28;

    private static final int HOTBAR_END =
            37;

    private static final int TEMPLATE_SLOT_X =
            323;

    private static final int TEMPLATE_SLOT_Y =
            31;

    private static final int PLAYER_INVENTORY_X =
            104;

    private static final int PLAYER_INVENTORY_Y =
            221;

    private static final int HOTBAR_Y =
            279;

    private final ContainerLevelAccess access;

    private final Container templateContainer =
            new SimpleContainer(1);

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

        addSlot(
                new Slot(
                        templateContainer,
                        0,
                        TEMPLATE_SLOT_X,
                        TEMPLATE_SLOT_Y
                ) {
                    @Override
                    public boolean mayPlace(
                            ItemStack stack
                    ) {
                        return stack.is(
                                ModItems.CUSTOM_STAINED_GLASS
                        );
                    }

                    @Override
                    public int getMaxStackSize() {
                        return 1;
                    }
                }
        );

        addPlayerInventory(
                inventory
        );
    }

    public ItemStack getTemplateStack() {
        return templateContainer.getItem(
                0
        );
    }

    public int getContainerIdValue() {
        return this.containerId;
    }

    public boolean fabricate(
            ServerPlayer player,
            WindowDesign design
    ) {
        if (!stillValid(player)) {
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

        player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable(
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
        Slot slot =
                this.slots.get(
                        slotIndex
                );

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack =
                slot.getItem();

        ItemStack originalStack =
                sourceStack.copy();

        if (slotIndex == TEMPLATE_SLOT) {
            if (!moveItemStackTo(
                    sourceStack,
                    PLAYER_INVENTORY_START,
                    HOTBAR_END,
                    true
            )) {
                return ItemStack.EMPTY;
            }
        } else if (sourceStack.is(
                ModItems.CUSTOM_STAINED_GLASS
        )) {
            if (!moveItemStackTo(
                    sourceStack,
                    TEMPLATE_SLOT,
                    TEMPLATE_SLOT + 1,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex
                < PLAYER_INVENTORY_END) {

            if (!moveItemStackTo(
                    sourceStack,
                    HOTBAR_START,
                    HOTBAR_END,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(
                sourceStack,
                PLAYER_INVENTORY_START,
                PLAYER_INVENTORY_END,
                false
        )) {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            slot.set(
                    ItemStack.EMPTY
            );
        } else {
            slot.setChanged();
        }

        if (sourceStack.getCount()
                == originalStack.getCount()) {

            return ItemStack.EMPTY;
        }

        slot.onTake(
                player,
                sourceStack
        );

        return originalStack;
    }

    @Override
    public void removed(
            Player player
    ) {
        super.removed(
                player
        );

        this.access.execute(
                (
                        level,
                        pos
                ) -> clearContainer(
                        player,
                        templateContainer
                )
        );
    }

    private void addPlayerInventory(
            Inventory inventory
    ) {
        for (int row = 0;
             row < 3;
             row++) {

            for (int column = 0;
                 column < 9;
                 column++) {

                addSlot(
                        new Slot(
                                inventory,
                                column
                                        + row * 9
                                        + 9,
                                PLAYER_INVENTORY_X
                                        + column * 18,
                                PLAYER_INVENTORY_Y
                                        + row * 18
                        )
                );
            }
        }

        for (int column = 0;
             column < 9;
             column++) {

            addSlot(
                    new Slot(
                            inventory,
                            column,
                            PLAYER_INVENTORY_X
                                    + column * 18,
                            HOTBAR_Y
                    )
            );
        }
    }
}
