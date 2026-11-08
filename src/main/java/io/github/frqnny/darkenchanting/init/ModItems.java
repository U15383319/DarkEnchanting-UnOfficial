package io.github.frqnny.darkenchanting.init;

import io.github.frqnny.darkenchanting.DarkEnchanting;
import io.github.frqnny.darkenchanting.item.TableUpgradeItem;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DarkEnchanting.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DarkEnchanting.MODID);

    public static final RegistryObject<Item> DARK_ENCHANTER = ITEMS.register("dark_enchanter",
            () -> new BlockItem(ModBlocks.DARK_ENCHANTER.get(), new Item.Properties()));
    public static final RegistryObject<Item> DARK_TORCH = ITEMS.register("dark_conduit",
            () -> new StandingAndWallBlockItem(ModBlocks.DARK_TORCH.get(), ModBlocks.DARK_TORCH_WALL.get(), new Item.Properties(), Direction.DOWN));
    public static final RegistryObject<Item> TABLE_UPGRADE = ITEMS.register("table_upgrade",
            () -> new TableUpgradeItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<CreativeModeTab> DARK_ENCHANTING = TABS.register("dark_enchanting_group",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.DARK_ENCHANTER.get()))
                    .displayItems((params, output) -> {
                        output.accept(DARK_ENCHANTER.get());
                        output.accept(DARK_TORCH.get());
                        output.accept(TABLE_UPGRADE.get());
                    })
                    .title(Component.translatable("itemGroup.dark_enchanting.dark_enchanting_group"))
                    .build());

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        TABS.register(modBus);
    }
}
