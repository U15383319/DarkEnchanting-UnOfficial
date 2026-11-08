package io.github.frqnny.darkenchanting.block;

import io.github.frqnny.darkenchanting.DarkEnchanting;
import io.github.frqnny.darkenchanting.blockentity.BlockEntityWithBook;
import io.github.frqnny.darkenchanting.blockentity.DarkEnchanterBlockEntity;
import io.github.frqnny.darkenchanting.init.ModBlocks;
import io.github.frqnny.darkenchanting.util.PlayerUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class DarkEnchanterBlock extends BaseEntityBlock {
    public static final EnumProperty<BookType> BOOK_TYPE = EnumProperty.create("book_type", BookType.class);
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);

    public DarkEnchanterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BOOK_TYPE, BookType.DEFAULT));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DarkEnchanterBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        PlayerUtils.syncExperience(player);
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        MenuProvider provider = state.getMenuProvider(level, pos);
        if (provider != null && player instanceof ServerPlayer serverPlayer) {
            // Sends the block position to the client, replacing Fabric's ExtendedScreenHandlerFactory.
            NetworkHooks.openGui(serverPlayer, provider, pos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BOOK_TYPE);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlocks.DE_BLOCK_ENTITY.get(), (level1, pos, state1, be) -> BlockEntityWithBook.tick(level1, pos, be));
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    //TODO make it render based on the ID on here
    public enum BookType implements StringRepresentable {
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
