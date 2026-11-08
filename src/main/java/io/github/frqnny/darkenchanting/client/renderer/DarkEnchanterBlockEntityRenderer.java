package io.github.frqnny.darkenchanting.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.frqnny.darkenchanting.DarkEnchanting;
import io.github.frqnny.darkenchanting.blockentity.DarkEnchanterBlockEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.BookModel;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class DarkEnchanterBlockEntityRenderer extends TileEntityRenderer<DarkEnchanterBlockEntity> {
    // Added to the block atlas in DarkEnchantingClient#onTextureStitch
    public static final ResourceLocation BOOK_ID = DarkEnchanting.id("entity/book1");
    private static final RenderMaterial BOOK_TEX = new RenderMaterial(PlayerContainer.BLOCK_ATLAS, BOOK_ID);
    private final BookModel book;

    public DarkEnchanterBlockEntityRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
        this.book = new BookModel();
    }

    @Override
    public void render(DarkEnchanterBlockEntity blockEntity, float tickDelta, MatrixStack matrices, IRenderTypeBuffer vertexConsumers, int light, int overlay) {
        matrices.pushPose();
        matrices.translate(0.5D, 0.75D, 0.5D);
        float partialTicks = (float) blockEntity.ticks + tickDelta;
        matrices.translate(0.0D, (0.1F + MathHelper.sin(partialTicks * 0.1F) * 0.01F), 0.0D);

        float bookRotationChange = blockEntity.bookRotation - blockEntity.bookRotationPrev;
        while (bookRotationChange >= 3.1415927F) {
            bookRotationChange -= 6.2831855F;
        }

        while (bookRotationChange < -3.1415927F) {
            bookRotationChange += 6.2831855F;
        }

        float newBookRotation = blockEntity.bookRotationPrev + bookRotationChange * tickDelta;
        matrices.mulPose(Vector3f.YP.rotation(-newBookRotation));
        matrices.mulPose(Vector3f.ZP.rotationDegrees(80.0F));
        float newPageAngle = MathHelper.lerp(tickDelta, blockEntity.pageAngle, blockEntity.nextPageAngle);
        float m = MathHelper.frac(newPageAngle + 0.25F) * 1.6F - 0.3F;
        float n = MathHelper.frac(newPageAngle + 0.75F) * 1.6F - 0.3F;
        float newPageTurningSpeed = MathHelper.lerp(tickDelta, blockEntity.pageTurningSpeed, blockEntity.nextPageTurningSpeed);
        this.book.setupAnim(partialTicks, MathHelper.clamp(m, 0.0F, 1.0F), MathHelper.clamp(n, 0.0F, 1.0F), newPageTurningSpeed);
        IVertexBuilder vertexConsumer = BOOK_TEX.buffer(vertexConsumers, RenderType::entitySolid);
        this.book.render(matrices, vertexConsumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
        matrices.popPose();
    }
}
