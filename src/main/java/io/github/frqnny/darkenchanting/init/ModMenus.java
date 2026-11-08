package io.github.frqnny.darkenchanting.init;

import io.github.frqnny.darkenchanting.DarkEnchanting;
import io.github.frqnny.darkenchanting.menu.DarkEnchanterMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, DarkEnchanting.MODID);

    // The client receives the block position written by NetworkHooks.openScreen.
    public static final RegistryObject<MenuType<DarkEnchanterMenu>> DARK_ENCHANTER = MENUS.register("dark_enchanter_gui",
            () -> IForgeMenuType.create((containerId, inventory, buf) ->
                    new DarkEnchanterMenu(containerId, inventory, ContainerLevelAccess.create(inventory.player.level(), buf.readBlockPos()))));

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
