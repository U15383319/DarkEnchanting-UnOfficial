package io.github.frqnny.darkenchanting.block;

import io.github.frqnny.darkenchanting.DarkEnchanting;
import io.github.frqnny.darkenchanting.blockentity.DarkEnchanterBlockEntity;
import io.github.frqnny.darkenchanting.util.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ContainerBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.Random;
import javax.annotation.Nullable;

public class DarkEnchanterBlock extends ContainerBlock {
    public static final EnumProperty<BookType> BOOK_TYPE = EnumProperty.create("book_type", BookType.class);
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);

    public DarkEnchanterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BOOK_TYPE, BookType.DEFAULT));
    }

    /**
     * The block entity ticks itself (ITickableTileEntity) to animate the book; 1.16.5 has no block entity tickers.
     */
    @Nullable
    @Override
    public TileEntity newBlockEntity(IBlockReader level) {
        return new DarkEnchanterBlockEntity();
    }

    @Override
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        PlayerUtils.syncExperience(player);
        if (level.isClientSide) {
            return ActionResultType.SUCCESS;
        }

        INamedContainerProvider provider = state.getMenuProvider(level, pos);
        if (provider != null && player instanceof ServerPlayerEntity) {
            // Sends the block position to the client, replacing Fabric's ExtendedScreenHandlerFactory.
            NetworkHooks.openGui((ServerPlayerEntity) player, provider, pos);
        }
        return ActionResultType.CONSUME;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public void animateTick(BlockState state, World level, BlockPos pos, Random random) {
        super.animateTick(state, level, pos, random);

        //inner circle
        for (int x = -2; x <= 2; ++x) {
            for (int z = -2; z <= 2; ++z) {

                if (x > -2 && x < 2 && z == -1) {
                    z = 2;
                }

                if (random.nextInt(16) == 0) {
                    for (int y = 0; y <= 1; ++y) {
                        BlockPos shelfPos = pos.offset(x, y, z);
                        if (level.getBlockState(shelfPos).is(Blocks.BOOKSHELF)) {
                            if (!level.isEmptyBlock(pos.offset(x / 2, 0, z / 2))) {
                                break;
                            }

                            level.addParticle(ParticleTypes.ENCHANT, (double) pos.getX() + 0.5D, (double) pos.getY() + 2.0D, (double) pos.getZ() + 0.5D, (double) ((float) x + random.nextFloat()) - 0.5D, (float) y - random.nextFloat() - 1.5F, (double) ((float) z + random.nextFloat()) - 0.5D);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BOOK_TYPE);
    }

    @Override
    public boolean isPathfindable(BlockState state, IBlockReader level, BlockPos pos, PathType type) {
        return false;
    }

    //TODO make it render based on the ID on here
    public enum BookType implements IStringSerializable {
        DEFAULT(DarkEnchanting.id("entity/book1"), "default"),
        HELL(DarkEnchanting.id("entity/book2"), "hell");

        final ResourceLocation id;
        final String string;

        BookType(ResourceLocation id, String string) {
            this.id = id;
            this.string = string;
        }

        @Override
        public String getSerializedName() {
            return string;
        }
    }
}
