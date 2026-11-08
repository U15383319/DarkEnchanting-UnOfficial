package io.github.frqnny.darkenchanting.menu;

import io.github.frqnny.darkenchanting.init.ModBlocks;
import io.github.frqnny.darkenchanting.init.ModMenus;
import io.github.frqnny.darkenchanting.util.PlayerUtils;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Server/client container of the Dark Enchanter. Replaces the container half of LibGui's SyncedGuiDescription;
 * the UI half lives in {@link io.github.frqnny.darkenchanting.client.gui.DarkEnchanterScreen}.
 */
public class DarkEnchanterMenu extends AbstractContainerMenu {
    // Kept within 240 logical pixels of height so the screen fits the smallest "Auto" GUI scale (320x240).
    // The LibGui original was 252 tall; the enchantment list is 12 px shorter and the player inventory moved up.
    public static final int WIDTH = 235;
    public static final int HEIGHT = 240;
    public static final int ITEM_SLOT_X = 36;
    public static final int ITEM_SLOT_Y = 18;
    public static final int INVENTORY_X = 54;
    public static final int INVENTORY_LABEL_Y = 144;
    public static final int INVENTORY_Y = 156;
    public static final int HOTBAR_Y = 214;

    private static final int PLAYER_INV_START = 1;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 36;

    public final ContainerLevelAccess access;
    private final Container inv = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            DarkEnchanterMenu.this.slotsChanged(this);
        }
    };

    public DarkEnchanterMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModMenus.DARK_ENCHANTER.get(), containerId);
        PlayerUtils.syncExperience(playerInventory.player);
        this.access = access;

        this.addSlot(new Slot(this.inv, 0, ITEM_SLOT_X, ITEM_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isValidItem(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, INVENTORY_X + col * 18, INVENTORY_Y + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, INVENTORY_X + col * 18, HOTBAR_Y));
        }
    }

    public static boolean isValidItem(ItemStack stack) {
        return stack.isEnchantable() || stack.isEnchanted();
    }

    public ItemStack getEnchantStack() {
        return this.inv.getItem(0);
    }

    /**
     * Call after modifying {@link #getEnchantStack()} in place (enchant/repair). Marks the slot dirty, which syncs
     * the item to the client right away; the screen then sees the new item and refreshes its sliders and costs.
     */
    public void onEnchantStackModified() {
        this.slots.get(0).setChanged();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index == 0) {
                if (!this.moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (isValidItem(stack) && !this.slots.get(0).hasItem()) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < PLAYER_INV_START + 27) {
                if (!this.moveItemStackTo(stack, PLAYER_INV_START + 27, PLAYER_INV_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_START + 27, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.DARK_ENCHANTER.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // The client menu also has a real level access (needed for cost previews), so only return items on the server.
        if (!player.level.isClientSide) {
            this.clearContainer(player, this.inv);
        }
    }
}
