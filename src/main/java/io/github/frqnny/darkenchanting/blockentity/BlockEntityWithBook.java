package io.github.frqnny.darkenchanting.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class BlockEntityWithBook extends BlockEntity {
    private static final Random RANDOM = new Random();
    public int ticks;
    public float nextPageAngle;
    public float pageAngle;
    public float flipRandom;
    public float flipTurn;
    public float nextPageTurningSpeed;
    public float pageTurningSpeed;
    public float bookRotation;
    public float bookRotationPrev;
    public float offset;

    public BlockEntityWithBook(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockEntityWithBook be) {
        be.pageTurningSpeed = be.nextPageTurningSpeed;
        be.bookRotationPrev = be.bookRotation;
        Player player = level.getNearestPlayer((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, 3.0D, false);
        if (player != null) {
            double d = player.getX() - ((double) pos.getX() + 0.5D);
            double e = player.getZ() - ((double) pos.getZ() + 0.5D);
            be.offset = (float) Mth.atan2(e, d);
            be.nextPageTurningSpeed += 0.1F;
            if (be.nextPageTurningSpeed < 0.5F || RANDOM.nextInt(40) == 0) {
                float f = be.flipRandom;

                do {
                    be.flipRandom += (float) (RANDOM.nextInt(4) - RANDOM.nextInt(4));
                } while (f == be.flipRandom);
            }
        } else {
            be.offset += 0.02F;
            be.nextPageTurningSpeed -= 0.1F;
        }

        while (be.bookRotation >= 3.1415927F) {
            be.bookRotation -= 6.2831855F;
        }

        while (be.bookRotation < -3.1415927F) {
            be.bookRotation += 6.2831855F;
        }

        while (be.offset >= 3.1415927F) {
            be.offset -= 6.2831855F;
        }

        while (be.offset < -3.1415927F) {
            be.offset += 6.2831855F;
        }

        float g = be.offset - be.bookRotation;

        while (g >= 3.1415927F) {
            g -= 6.2831855F;
        }

        while (g < -3.1415927F) {
            g += 6.2831855F;
        }

        be.bookRotation += g * 0.4F;
        be.nextPageTurningSpeed = Mth.clamp(be.nextPageTurningSpeed, 0.0F, 1.0F);
        ++be.ticks;
        be.pageAngle = be.nextPageAngle;
        float h = (be.flipRandom - be.nextPageAngle) * 0.4F;
        h = Mth.clamp(h, -0.2F, 0.2F);
        be.flipTurn += (h - be.flipTurn) * 0.9F;
        be.nextPageAngle += be.flipTurn;
    }
}
