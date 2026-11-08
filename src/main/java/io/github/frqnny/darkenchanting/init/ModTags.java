package io.github.frqnny.darkenchanting.init;

import io.github.frqnny.darkenchanting.DarkEnchanting;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.registries.ForgeRegistries;

public class ModTags {
    // dark_enchanting:bookshelves contains minecraft:bookshelf plus the optional #forge:bookshelves and #c:bookshelves tags.
    public static final ITag.INamedTag<Block> BOOKSHELVES = BlockTags.createOptional(DarkEnchanting.id("bookshelves"));
    // Forge 1.16.5 gives the enchantment registry the tag folder "enchantments":
    // loaded from data/dark_enchanting/tags/enchantments/disabled.json
    public static final ITag.INamedTag<Enchantment> DISABLED = ForgeTagHandler.createOptionalTag(ForgeRegistries.ENCHANTMENTS, DarkEnchanting.id("disabled"));
    public static final ITag.INamedTag<Block> CRYING_OBSIDIAN = BlockTags.createOptional(DarkEnchanting.id("crying_obsidian"));
}
