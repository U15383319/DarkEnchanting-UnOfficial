package io.github.frqnny.darkenchanting.init;

import io.github.frqnny.darkenchanting.DarkEnchanting;
import io.github.frqnny.darkenchanting.block.DarkConduitBlock;
import io.github.frqnny.darkenchanting.block.DarkEnchanterBlock;
import io.github.frqnny.darkenchanting.block.WallDarkConduitBlock;
import io.github.frqnny.darkenchanting.blockentity.DarkEnchanterBlockEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, DarkEnchanting.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, DarkEnchanting.MODID);

    public static final RegistryObject<DarkEnchanterBlock> DARK_ENCHANTER = BLOCKS.register("dark_enchanter",
            () -> new DarkEnchanterBlock(BlockBehaviour.Properties.copy(Blocks.ENCHANTING_TABLE).requiresCorrectToolForDrops()));
    public static final RegistryObject<DarkConduitBlock> DARK_TORCH = BLOCKS.register("dark_conduit",
            () -> new DarkConduitBlock(BlockBehaviour.Properties.copy(Blocks.TORCH), ParticleTypes.DRAGON_BREATH));
    public static final RegistryObject<WallDarkConduitBlock> DARK_TORCH_WALL = BLOCKS.register("wall_dark_conduit",
            () -> new WallDarkConduitBlock(BlockBehaviour.Properties.copy(Blocks.WALL_TORCH), ParticleTypes.DRAGON_BREATH));

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<DarkEnchanterBlockEntity>> DE_BLOCK_ENTITY = BLOCK_ENTITIES.register("dark_enchanter",
            () -> BlockEntityType.Builder.of(DarkEnchanterBlockEntity::new, DARK_ENCHANTER.get()).build(null));

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }
}
