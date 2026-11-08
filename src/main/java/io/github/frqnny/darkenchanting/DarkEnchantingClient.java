package io.github.frqnny.darkenchanting;

import io.github.frqnny.darkenchanting.client.gui.DarkEnchanterScreen;
import io.github.frqnny.darkenchanting.client.renderer.DarkEnchanterBlockEntityRenderer;
import io.github.frqnny.darkenchanting.init.ModBlocks;
import io.github.frqnny.darkenchanting.init.ModMenus;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client-side setup: menu screen, block entity renderer, render layers and the book texture.
 */
@Mod.EventBusSubscriber(modid = DarkEnchanting.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class DarkEnchantingClient {
    private DarkEnchantingClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ClientRegistry.bindTileEntityRenderer(ModBlocks.DE_BLOCK_ENTITY.get(), DarkEnchanterBlockEntityRenderer::new);
        event.enqueueWork(() -> {
            ScreenManager.register(ModMenus.DARK_ENCHANTER.get(), DarkEnchanterScreen::new);
            // 1.16.5 models cannot declare "render_type"; the dark conduit needs the cutout layer.
            RenderTypeLookup.setRenderLayer(ModBlocks.DARK_TORCH.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.DARK_TORCH_WALL.get(), RenderType.cutout());
        });
    }

    /**
     * The enchanter's book texture is not used by any block model, so add it to the block atlas explicitly
     * (1.19.3+ does this with assets/minecraft/atlases/blocks.json instead).
     */
    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if (event.getMap().location().equals(PlayerContainer.BLOCK_ATLAS)) {
            event.addSprite(DarkEnchanterBlockEntityRenderer.BOOK_ID);
        }
    }
}
