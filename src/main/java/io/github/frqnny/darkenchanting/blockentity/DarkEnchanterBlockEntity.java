package io.github.frqnny.darkenchanting.blockentity;

import io.github.frqnny.darkenchanting.init.ModBlocks;
import io.github.frqnny.darkenchanting.menu.DarkEnchanterMenu;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class DarkEnchanterBlockEntity extends BlockEntityWithBook implements INamedContainerProvider {
    public DarkEnchanterBlockEntity() {
        super(ModBlocks.DE_BLOCK_ENTITY.get());
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("block.dark_enchanting.dark_enchanter");
    }

    @Nullable
    @Override
    public Container createMenu(int containerId, PlayerInventory inventory, PlayerEntity player) {
        return new DarkEnchanterMenu(containerId, inventory, IWorldPosCallable.create(this.level, this.worldPosition));
    }
}
