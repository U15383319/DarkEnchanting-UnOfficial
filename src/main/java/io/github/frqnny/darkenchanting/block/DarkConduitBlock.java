package io.github.frqnny.darkenchanting.block;

import io.github.frqnny.darkenchanting.config.DarkEnchantingConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.TorchBlock;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Random;

public class DarkConduitBlock extends TorchBlock {
    public DarkConduitBlock(Properties properties, IParticleData particle) {
        super(properties, particle);
    }

    @Override
    public void animateTick(BlockState state, World level, BlockPos pos, Random random) {
        if (DarkEnchantingConfig.hasFancyShrineParticle()) {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            double d = (double) i + random.nextDouble();
            double e = (double) j + 0.7D;
            double f = (double) k + random.nextDouble();
            level.addParticle(ParticleTypes.FALLING_OBSIDIAN_TEAR, d, e, f, 0.0D, 0.0D, 0.0D);
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            for (int l = 0; l < 5; ++l) {
                mutable.set(i + MathHelper.nextInt(random, -4, 4), j - random.nextInt(5), k + MathHelper.nextInt(random, -4, 4));
                BlockState blockState = level.getBlockState(mutable);
                if (!blockState.isCollisionShapeFullBlock(level, mutable)) {
                    level.addParticle(ParticleTypes.ENCHANT, (double) mutable.getX() + random.nextDouble(), (double) mutable.getY() + random.nextDouble(), (double) mutable.getZ() + random.nextDouble(), 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }
}
