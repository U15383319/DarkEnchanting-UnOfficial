package io.github.frqnny.darkenchanting.gametest;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

/**
 * Minimal stand-in for the GameTest helper of newer Minecraft versions (1.16.5 has no GameTest framework):
 * a level, an origin for relative positions, and a recorded failure.
 */
public final class TestHelper {
    private final ServerWorld level;
    private final BlockPos origin;
    private String failure;

    TestHelper(ServerWorld level, BlockPos origin) {
        this.level = level;
        this.origin = origin;
    }

    public ServerWorld getLevel() {
        return this.level;
    }

    public BlockPos absolutePos(BlockPos relative) {
        return this.origin.offset(relative.getX(), relative.getY(), relative.getZ());
    }

    public void setBlock(BlockPos relative, Block block) {
        this.level.setBlockAndUpdate(this.absolutePos(relative), block.defaultBlockState());
    }

    /** Records the first failure; the test method returns right after calling this. */
    public void fail(String message) {
        if (this.failure == null) {
            this.failure = message;
        }
    }

    public void succeed() {
    }

    String failure() {
        return this.failure;
    }
}
