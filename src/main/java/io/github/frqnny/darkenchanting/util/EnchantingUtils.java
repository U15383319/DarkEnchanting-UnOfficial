package io.github.frqnny.darkenchanting.util;

import io.github.frqnny.darkenchanting.config.ConfigEnchantment;
import io.github.frqnny.darkenchanting.config.DarkEnchantingConfig;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnchantingUtils {
    /**
     * Enchantments the Dark Enchanter offers for {@code stack}, in registry order: enabled in config and not in the
     * disabled tag, applicable to the item, and compatible with every enchantment that is kept on the stack or about
     * to be applied.
     *
     * @param enchantmentsToApply levels currently selected in the GUI
     * @param enchantmentsOnStack levels currently on the item
     */
    public static List<Enchantment> getAvailableEnchantments(ItemStack stack, Object2IntMap<Enchantment> enchantmentsToApply, Object2IntMap<Enchantment> enchantmentsOnStack) {
        List<Enchantment> result = new ArrayList<>();
        if (stack.isEmpty()) {
            return result;
        }

        for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS) {
            Optional<ConfigEnchantment> configEnchantment = ConfigEnchantment.getConfigEnchantmentFor(enchantment);
            if (configEnchantment.isPresent() && !configEnchantment.get().activated) {
                continue;
            }

            if (TagUtils.isEnchantmentDisabled(enchantment) || enchantment.getMaxLevel() < 1 || !enchantment.canEnchant(stack)) {
                continue;
            }

            if (isCompatible(enchantment, enchantmentsToApply, enchantmentsOnStack)) {
                result.add(enchantment);
            }
        }
        return result;
    }

    private static boolean isCompatible(Enchantment enchantment, Object2IntMap<Enchantment> enchantmentsToApply, Object2IntMap<Enchantment> enchantmentsOnStack) {
        for (Enchantment onStack : enchantmentsOnStack.keySet()) {
            boolean removed = enchantmentsToApply.getInt(onStack) <= 0;
            if (!removed && !onStack.equals(enchantment) && !onStack.isCompatibleWith(enchantment)) {
                return false;
            }
        }

        for (var entry : enchantmentsToApply.object2IntEntrySet()) {
            Enchantment other = entry.getKey();
            if (entry.getIntValue() > 0 && !other.equals(enchantment) && !other.isCompatibleWith(enchantment)) {
                return false;
            }
        }
        return true;
    }

    public static boolean applyEnchantXP(PlayerEntity player, Object2IntMap<Enchantment> enchantmentsToApply, Object2IntMap<Enchantment> enchantmentsOnStack, double discount) {
        int totalExperience = PlayerUtils.syncAndGetTotalExperience(player);
        int xpCost = BookcaseUtils.applyDiscount(CostUtils.getExperienceCost(enchantmentsToApply, enchantmentsOnStack), discount);

        boolean canApplyXp = totalExperience >= xpCost || player.isCreative();
        if (canApplyXp) {
            PlayerUtils.modifyExperience(player, -xpCost);
        }

        return canApplyXp;
    }

    public static boolean applyRepairXP(PlayerEntity player, ItemStack stack, double discount) {
        int totalExperience = PlayerUtils.syncAndGetTotalExperience(player);
        int xpCost = BookcaseUtils.applyDiscount(CostUtils.getRepairCost(stack), discount);

        boolean canApplyXp = totalExperience >= xpCost || player.isCreative();
        if (canApplyXp) {
            PlayerUtils.modifyExperience(player, -xpCost);
        }

        return canApplyXp;
    }

    public static void set(Object2IntMap<Enchantment> enchantments, ItemStack stack) {
        ListNBT nbtList = new ListNBT();

        for (var entry : enchantments.object2IntEntrySet()) {
            Enchantment enchantment = entry.getKey();
            if (enchantment != null) {
                int i = entry.getIntValue();
                ResourceLocation id = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);

                boolean shouldCheckMaxLevelEnch = DarkEnchantingConfig.shouldRejectEnchantmentAttemptsAboveMaxValue();
                if (id != null && i > 0 && (!shouldCheckMaxLevelEnch || i <= enchantment.getMaxLevel())) {
                    nbtList.add(storeEnchantment(id, i));
                }

            }
        }

        if (nbtList.isEmpty()) {
            stack.removeTagKey("Enchantments");
        } else if (stack.getItem() != Items.ENCHANTED_BOOK) {
            stack.getOrCreateTag().put("Enchantments", nbtList);
        }

    }

    /**
     * Same entry format as vanilla's ItemStack#enchant (1.16.5 has no EnchantmentHelper#storeEnchantment).
     */
    private static CompoundNBT storeEnchantment(ResourceLocation id, int level) {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("id", String.valueOf(id));
        tag.putShort("lvl", (short) level);
        return tag;
    }
}
