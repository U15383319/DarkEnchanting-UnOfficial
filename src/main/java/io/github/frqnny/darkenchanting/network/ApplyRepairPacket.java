package io.github.frqnny.darkenchanting.network;

import io.github.frqnny.darkenchanting.menu.DarkEnchanterMenu;
import io.github.frqnny.darkenchanting.util.BookcaseUtils;
import io.github.frqnny.darkenchanting.util.EnchantingUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> server: repair the item in the open Dark Enchanter with XP.
 */
public final class ApplyRepairPacket {

    public static void encode(ApplyRepairPacket packet, PacketBuffer buf) {
    }

    public static ApplyRepairPacket decode(PacketBuffer buf) {
        return new ApplyRepairPacket();
    }

    public static void handle(ApplyRepairPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayerEntity player = ctx.get().getSender();
        if (player != null) {
            apply(player);
        }
    }

    /**
     * Server side: charge XP and fully repair the item in the player's open Dark Enchanter.
     * The menu stays open and the repaired item is synced back into the slot.
     */
    public static void apply(ServerPlayerEntity player) {
        if (!(player.containerMenu instanceof DarkEnchanterMenu)) {
            return;
        }
        DarkEnchanterMenu menu = (DarkEnchanterMenu) player.containerMenu;
        if (!menu.stillValid(player)) {
            return;
        }

        menu.access.execute((level, pos) -> {
            ItemStack stack = menu.getEnchantStack();
            if (stack.isDamaged() && EnchantingUtils.applyRepairXP(player, stack, BookcaseUtils.getDiscount(level, pos))) {
                stack.setDamageValue(0);
                menu.onEnchantStackModified();
                level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, level.random.nextFloat() * 0.2F + 0.9F);
            }
        });
    }
}
