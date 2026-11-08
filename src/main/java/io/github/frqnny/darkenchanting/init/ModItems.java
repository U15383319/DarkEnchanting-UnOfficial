package io.github.frqnny.darkenchanting.init;

import io.github.frqnny.darkenchanting.DarkEnchanting;
import io.github.frqnny.darkenchanting.item.TableUpgradeItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.item.WallOrFloorItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DarkEnchanting.MODID);

    // 1.16.5 creative tabs are plain objects (no registry); the title key is "itemGroup." + label.
    public static final ItemGroup DARK_ENCHANTING = new ItemGroup(DarkEnchanting.MODID + ".dark_enchanting_group") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModBlocks.DARK_ENCHANTER.get());
        }
    };

    public static final RegistryObject<Item> DARK_ENCHANTER = ITEMS.register("dark_enchanter",
            () -> new BlockItem(ModBlocks.DARK_ENCHANTER.get(), new Item.Properties().tab(DARK_ENCHANTING)));
    public static final RegistryObject<Item> DARK_TORCH = ITEMS.register("dark_conduit",
            () -> new WallOrFloorItem(ModBlocks.DARK_TORCH.get(), ModBlocks.DARK_TORCH_WALL.get(), new Item.Properties().tab(DARK_ENCHANTING)));
    public static final RegistryObject<Item> TABLE_UPGRADE = ITEMS.register("table_upgrade",
            () -> new TableUpgradeItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).tab(DARK_ENCHANTING)));

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
