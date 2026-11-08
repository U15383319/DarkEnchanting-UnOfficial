package io.github.frqnny.darkenchanting.util;

import io.github.frqnny.darkenchanting.init.ModTags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

public class TagUtils {

    public static boolean isEnchantmentDisabled(Enchantment enchantment) {
        var tags = ForgeRegistries.ENCHANTMENTS.tags();
        return tags != null && tags.getTag(ModTags.DISABLED).contains(enchantment);
    }
}
