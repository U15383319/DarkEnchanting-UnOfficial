package io.github.frqnny.darkenchanting.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.function.IntConsumer;

/**
 * Discrete 0..maxLevel slider for a single enchantment. Replaces LibGui's WLabeledSlider.
 */
public class EnchantmentSlider extends AbstractSliderButton {
    public final Enchantment enchantment;
    private final int maxLevel;
    private final IntConsumer onLevelChanged;
    private int level;

    public EnchantmentSlider(int x, int y, int width, int height, Enchantment enchantment, int level, IntConsumer onLevelChanged) {
        super(x, y, width, height, Component.empty(), toValue(level, enchantment.getMaxLevel()));
        this.enchantment = enchantment;
        this.maxLevel = enchantment.getMaxLevel();
        this.onLevelChanged = onLevelChanged;
        this.level = level;
        this.updateMessage();
    }

    private static double toValue(int level, int maxLevel) {
        return maxLevel <= 0 ? 0.0D : Math.min(1.0D, Math.max(0.0D, (double) level / (double) maxLevel));
    }

    public static Component getLabel(Enchantment enchantment, int level) {
        MutableComponent text = Component.translatable(enchantment.getDescriptionId());
        if (enchantment.isCurse()) {
            text.withStyle(ChatFormatting.RED);
        } else if (enchantment.isTreasureOnly()) {
            text.withStyle(ChatFormatting.BLUE);
        } else {
            text.withStyle(ChatFormatting.GRAY);
        }

        if (level > 0) {
            text.append(" ").append(Component.translatable("enchantment.level." + level));
        }

        return text;
    }

    public int getLevel() {
        return this.level;
    }

    @Override
    protected void updateMessage() {
        // enchantment is still null while the super constructor runs
        if (this.enchantment != null) {
            this.setMessage(getLabel(this.enchantment, this.level));
        }
    }

    @Override
    protected void applyValue() {
        int newLevel = (int) Math.round(this.value * this.maxLevel);
        this.value = toValue(newLevel, this.maxLevel);
        if (newLevel != this.level) {
            this.level = newLevel;
            this.onLevelChanged.accept(newLevel);
        }
    }
}
