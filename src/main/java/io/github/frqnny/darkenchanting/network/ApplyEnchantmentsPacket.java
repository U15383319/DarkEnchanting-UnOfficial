package io.github.frqnny.darkenchanting.network;

import io.github.frqnny.darkenchanting.menu.DarkEnchanterMenu;
import io.github.frqnny.darkenchanting.util.BookcaseUtils;
import io.github.frqnny.darkenchanting.util.EnchantingUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.stats.Stats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 * Client -> server: apply the selected enchantment levels to the item in the open Dark Enchanter.
 * The block position comes from the server-side menu instead of the packet.
 */
public final class ApplyEnchantmentsPacket {
    private final Object2IntMap<Enchantment> enchantments;

    public ApplyEnchantmentsPacket(Object2IntMap<Enchantment> enchantments) {
        this.enchantments = enchantments;
    }

    public static void encode(ApplyEnchantmentsPacket packet, PacketBuffer buf) {
        buf.writeVarInt(packet.enchantments.size());
        for (var entry : packet.enchantments.object2IntEntrySet()) {
            buf.writeResourceLocation(ForgeRegistries.ENCHANTMENTS.getKey(entry.getKey()));
            buf.writeVarInt(entry.getIntValue());
        }
    }

    public static ApplyEnchantmentsPacket decode(PacketBuffer buf) {
        int size = buf.readVarInt();
        Object2IntMap<Enchantment> enchantments = new Object2IntOpenHashMap<>(Math.min(size, 256));
        for (int i = 0; i < size; i++) {
            ResourceLocation id = buf.readResourceLocation();
            int level = buf.readVarInt();
            Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(id);
            if (enchantment != null) {
                enchantments.put(enchantment, level);
            }
        }
        return new ApplyEnchantmentsPacket(enchantments);
    }

    public static void handle(ApplyEnchantmentsPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayerEntity player = ctx.get().getSender();
        if (player != null) {
            apply(player, packet.enchantments);
        }
    }

    /**
     * Server side: charge XP and apply {@code enchantments} to the item in the player's open Dark Enchanter.
     * Like the vanilla enchanting table, the menu stays open and the updated item is synced back into the slot.
     */
    public static void apply(ServerPlayerEntity player, Object2IntMap<Enchantment> enchantments) {
        if (!(player.containerMenu instanceof DarkEnchanterMenu)) {
            return;
        }
        DarkEnchanterMenu menu = (DarkEnchanterMenu) player.containerMenu;
        if (!menu.stillValid(player)) {
            return;
        }

        menu.access.execute((level, pos) -> {
            ItemStack stack = menu.getEnchantStack();
            if (stack.isEmpty()) {
                return;
            }

            Object2IntMap<Enchantment> currentEnchantments = new Object2IntOpenHashMap<>(EnchantmentHelper.getEnchantments(stack));
            if (EnchantingUtils.applyEnchantXP(player, enchantments, currentEnchantments, BookcaseUtils.getDiscount(level, pos))) {
                EnchantingUtils.set(enchantments, stack);
                player.awardStat(Stats.ENCHANT_ITEM);
                CriteriaTriggers.ENCHANTED_ITEM.trigger(player, stack, 1);
                menu.onEnchantStackModified();
                level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, level.random.nextFloat() * 0.2F + 0.9F);
            }
        });
    }
}
