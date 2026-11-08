package io.github.frqnny.darkenchanting.item;

import io.github.frqnny.darkenchanting.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class TableUpgradeItem extends Item {
    public TableUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.ENCHANTING_TABLE)) {
            level.setBlockAndUpdate(pos, ModBlocks.DARK_ENCHANTER.get().defaultBlockState());
            context.getItemInHand().shrink(1);
            return InteractionResult.SUCCESS;
        } else if (context.getPlayer() != null) {
            context.getPlayer().displayClientMessage(Component.translatable("message.dark_enchanting.table_upgrade"), true);
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.displayClientMessage(Component.translatable("message.dark_enchanting.table_upgrade"), true);
        return super.use(level, player, hand);
    }
}
