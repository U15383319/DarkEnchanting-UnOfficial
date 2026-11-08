package io.github.frqnny.darkenchanting.item;

import io.github.frqnny.darkenchanting.init.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class TableUpgradeItem extends Item {
    public TableUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        World level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.ENCHANTING_TABLE)) {
            level.setBlockAndUpdate(pos, ModBlocks.DARK_ENCHANTER.get().defaultBlockState());
            context.getItemInHand().shrink(1);
            return ActionResultType.SUCCESS;
        } else if (context.getPlayer() != null) {
            context.getPlayer().displayClientMessage(new TranslationTextComponent("message.dark_enchanting.table_upgrade"), true);
        }
        return ActionResultType.PASS;
    }

    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        player.displayClientMessage(new TranslationTextComponent("message.dark_enchanting.table_upgrade"), true);
        return super.use(level, player, hand);
    }
}
