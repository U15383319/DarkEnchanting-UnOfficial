package io.github.frqnny.darkenchanting.init;

import io.github.frqnny.darkenchanting.DarkEnchanting;
import io.github.frqnny.darkenchanting.menu.DarkEnchanterMenu;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.IWorldPosCallable;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModMenus {
    public static final DeferredRegister<ContainerType<?>> MENUS = DeferredRegister.create(ForgeRegistries.CONTAINERS, DarkEnchanting.MODID);

    // The client receives the block position written by NetworkHooks.openGui.
    public static final RegistryObject<ContainerType<DarkEnchanterMenu>> DARK_ENCHANTER = MENUS.register("dark_enchanter_gui",
            () -> IForgeContainerType.create((containerId, inventory, buf) ->
                    new DarkEnchanterMenu(containerId, inventory, IWorldPosCallable.create(inventory.player.level, buf.readBlockPos()))));

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
