package io.github.frqnny.darkenchanting;

import io.github.frqnny.darkenchanting.config.DarkEnchantingConfig;
import io.github.frqnny.darkenchanting.init.ModBlocks;
import io.github.frqnny.darkenchanting.init.ModItems;
import io.github.frqnny.darkenchanting.init.ModMenus;
import io.github.frqnny.darkenchanting.network.ModPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DarkEnchanting.MODID)
public class DarkEnchanting {
    // Forge mod ids must match [a-z][a-z0-9_]{1,63}, so the Fabric id "dark-enchanting" becomes "dark_enchanting".
    public static final String MODID = "dark_enchanting";

    public DarkEnchanting() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.register(modBus);
        ModItems.register(modBus);
        ModMenus.register(modBus);
        DarkEnchantingConfig.register(modBus);
        ModPackets.init();
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
}
