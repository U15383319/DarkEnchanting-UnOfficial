package io.github.frqnny.darkenchanting.init;

import io.github.frqnny.darkenchanting.DarkEnchanting;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;

public class ModTags {
    // dark_enchanting:bookshelves contains minecraft:bookshelf plus the optional #forge:bookshelves and #c:bookshelves tags.
    public static final TagKey<Block> BOOKSHELVES = TagKey.create(Registries.BLOCK, DarkEnchanting.id("bookshelves"));
    // Loaded from data/dark_enchanting/tags/enchantment/disabled.json
    public static final TagKey<Enchantment> DISABLED = TagKey.create(Registries.ENCHANTMENT, DarkEnchanting.id("disabled"));
    public static final TagKey<Block> CRYING_OBSIDIAN = TagKey.create(Registries.BLOCK, DarkEnchanting.id("crying_obsidian"));
}
