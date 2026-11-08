package io.github.frqnny.darkenchanting.network;

import io.github.frqnny.darkenchanting.menu.DarkEnchanterMenu;
import io.github.frqnny.darkenchanting.util.BookcaseUtils;
import io.github.frqnny.darkenchanting.util.EnchantingUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> server: repair the item in the open Dark Enchanter with XP.
 */
public record ApplyRepairPacket() {

    public static void encode(ApplyRepairPacket packet, FriendlyByteBuf buf) {
    }

    public static ApplyRepairPacket decode(FriendlyByteBuf buf) {
        return new ApplyRepairPacket();
    }

    public static void handle(ApplyRepairPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            apply(player);
        }
    }

    /**
     * Server side: charge XP and fully repair the item in the player's open Dark Enchanter.
     * The menu stays open and the repaired item is synced back into the slot.
     */
    public static void apply(ServerPlayer player) {
        if (!(player.containerMenu instanceof DarkEnchanterMenu menu) || !menu.stillValid(player)) {
            return;
        }

        menu.access.execute((level, pos) -> {
            ItemStack stack = menu.getEnchantStack();
            if (stack.isDamaged() && EnchantingUtils.applyRepairXP(player, stack, BookcaseUtils.getDiscount(level, pos))) {
                stack.setDamageValue(0);
                menu.onEnchantStackModified();
                level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.2F + 0.9F);
            }
        });
    }
}
