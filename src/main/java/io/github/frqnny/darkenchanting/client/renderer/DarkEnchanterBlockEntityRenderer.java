package io.github.frqnny.darkenchanting.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import io.github.frqnny.darkenchanting.DarkEnchanting;
import io.github.frqnny.darkenchanting.blockentity.DarkEnchanterBlockEntity;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;

public class DarkEnchanterBlockEntityRenderer implements BlockEntityRenderer<DarkEnchanterBlockEntity> {
    // Added to the block atlas by assets/minecraft/atlases/blocks.json
    public static final ResourceLocation BOOK_ID = DarkEnchanting.id("entity/book1");
    private static final Material BOOK_TEX = new Material(InventoryMenu.BLOCK_ATLAS, BOOK_ID);
    private final BookModel book;

    public DarkEnchanterBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.book = new BookModel(ctx.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public void render(DarkEnchanterBlockEntity blockEntity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        matrices.pushPose();
        matrices.translate(0.5D, 0.75D, 0.5D);
        float partialTicks = (float) blockEntity.ticks + tickDelta;
        matrices.translate(0.0D, (0.1F + Mth.sin(partialTicks * 0.1F) * 0.01F), 0.0D);

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
        float newPageAngle = Mth.lerp(tickDelta, blockEntity.pageAngle, blockEntity.nextPageAngle);
        float m = Mth.frac(newPageAngle + 0.25F) * 1.6F - 0.3F;
        float n = Mth.frac(newPageAngle + 0.75F) * 1.6F - 0.3F;
        float newPageTurningSpeed = Mth.lerp(tickDelta, blockEntity.pageTurningSpeed, blockEntity.nextPageTurningSpeed);
        this.book.setupAnim(partialTicks, Mth.clamp(m, 0.0F, 1.0F), Mth.clamp(n, 0.0F, 1.0F), newPageTurningSpeed);
        VertexConsumer vertexConsumer = BOOK_TEX.buffer(vertexConsumers, RenderType::entitySolid);
        this.book.render(matrices, vertexConsumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
        matrices.popPose();
    }
}
