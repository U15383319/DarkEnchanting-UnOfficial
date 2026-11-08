package io.github.frqnny.darkenchanting.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class WallDarkConduitBlock extends WallTorchBlock {
    public WallDarkConduitBlock(Properties properties, ParticleOptions particle) {
        super(properties, particle);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {
        Direction direction = state.getValue(FACING);
        double d = (double) pos.getX() + 0.5D;
        double e = (double) pos.getY() + 0.8D;
        double f = (double) pos.getZ() + 0.5D;
        Direction direction2 = direction.getOpposite();
        level.addParticle(ParticleTypes.ENCHANT, d + 0.27D * (double) direction2.getStepX(), e + 0.22D, f + 0.27D * (double) direction2.getStepZ(), 0.0D, -0.3D, 0.0D);
        level.addParticle(this.flameParticle, d + 0.27D * (double) direction2.getStepX(), e + 0.22D, f + 0.27D * (double) direction2.getStepZ(), 0.0D, 0.0D, 0.0D);
    }
}
