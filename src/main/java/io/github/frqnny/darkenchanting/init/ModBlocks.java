package io.github.frqnny.darkenchanting.init;

import io.github.frqnny.darkenchanting.DarkEnchanting;
import io.github.frqnny.darkenchanting.block.DarkConduitBlock;
import io.github.frqnny.darkenchanting.block.DarkEnchanterBlock;
import io.github.frqnny.darkenchanting.block.WallDarkConduitBlock;
import io.github.frqnny.darkenchanting.blockentity.DarkEnchanterBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, DarkEnchanting.MODID);
    public static final DeferredRegister<TileEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, DarkEnchanting.MODID);

    // 1.16.5 has no mineable/* tags: the pickaxe requirement is set with harvestTool/harvestLevel instead.
    public static final RegistryObject<DarkEnchanterBlock> DARK_ENCHANTER = BLOCKS.register("dark_enchanter",
            () -> new DarkEnchanterBlock(AbstractBlock.Properties.copy(Blocks.ENCHANTING_TABLE)
                    .requiresCorrectToolForDrops().harvestTool(ToolType.PICKAXE).harvestLevel(0)));
    public static final RegistryObject<DarkConduitBlock> DARK_TORCH = BLOCKS.register("dark_conduit",
            () -> new DarkConduitBlock(AbstractBlock.Properties.copy(Blocks.TORCH), ParticleTypes.DRAGON_BREATH));
    public static final RegistryObject<WallDarkConduitBlock> DARK_TORCH_WALL = BLOCKS.register("wall_dark_conduit",
            () -> new WallDarkConduitBlock(AbstractBlock.Properties.copy(Blocks.WALL_TORCH), ParticleTypes.DRAGON_BREATH));

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<TileEntityType<DarkEnchanterBlockEntity>> DE_BLOCK_ENTITY = BLOCK_ENTITIES.register("dark_enchanter",
            () -> TileEntityType.Builder.of(DarkEnchanterBlockEntity::new, DARK_ENCHANTER.get()).build(null));

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }
}
