package io.github.frqnny.darkenchanting.util;

import io.github.frqnny.darkenchanting.init.ModTags;
import net.minecraft.enchantment.Enchantment;

public class TagUtils {

    public static boolean isEnchantmentDisabled(Enchantment enchantment) {
        return ModTags.DISABLED.contains(enchantment);
    }
}
