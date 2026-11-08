package io.github.frqnny.darkenchanting.blockentity;

import io.github.frqnny.darkenchanting.init.ModBlocks;
import io.github.frqnny.darkenchanting.menu.DarkEnchanterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DarkEnchanterBlockEntity extends BlockEntityWithBook implements MenuProvider {
    public DarkEnchanterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.DE_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("block.dark_enchanting.dark_enchanter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new DarkEnchanterMenu(containerId, inventory, ContainerLevelAccess.create(this.level, this.worldPosition));
    }
}
