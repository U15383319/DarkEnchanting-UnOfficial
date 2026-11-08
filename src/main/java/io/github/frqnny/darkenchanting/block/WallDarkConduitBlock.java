package io.github.frqnny.darkenchanting.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class WallDarkConduitBlock extends WallTorchBlock {
    public WallDarkConduitBlock(Properties properties, IParticleData particle) {
        super(properties, particle);
    }

    @Override
    public void animateTick(BlockState state, World level, BlockPos pos, Random random) {
        Direction direction = state.getValue(FACING);
        double d = (double) pos.getX() + 0.5D;
        double e = (double) pos.getY() + 0.8D;
        double f = (double) pos.getZ() + 0.5D;
        Direction direction2 = direction.getOpposite();
        level.addParticle(ParticleTypes.ENCHANT, d + 0.27D * (double) direction2.getStepX(), e + 0.22D, f + 0.27D * (double) direction2.getStepZ(), 0.0D, -0.3D, 0.0D);
        level.addParticle(this.flameParticle, d + 0.27D * (double) direction2.getStepX(), e + 0.22D, f + 0.27D * (double) direction2.getStepZ(), 0.0D, 0.0D, 0.0D);
    }
}
