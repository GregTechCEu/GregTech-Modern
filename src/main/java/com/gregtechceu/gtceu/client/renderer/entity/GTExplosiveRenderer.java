package com.gregtechceu.gtceu.client.renderer.entity;

import com.gregtechceu.gtceu.common.entity.GTExplosiveEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public class GTExplosiveRenderer<T extends GTExplosiveEntity> extends EntityRenderer<T> {
    private final BlockRenderDispatcher blockRenderer;

    public GTExplosiveRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(@NotNull T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate((float) entity.getX(), (float) entity.getY() + 0.5F, (float) entity.getZ());
        float f2;
        if ((float) entity.getFuse() - partialTicks + 1.0F < 10.0F) {
            f2 = 1.0F - ((float) entity.getFuse() - partialTicks + 1.0F) / 10.0F;
            f2 = Mth.clamp(f2, 0.0F, 1.0F);
            f2 *= f2;
            f2 *= f2;
            float f1 = 1.0F + f2 * 0.3F;
            poseStack.scale(f1, f1, f1);
        }

        f2 = (1.0F - ((float) entity.getFuse() - partialTicks + 1.0F) / 100.0F) * 0.8F;
        poseStack.mulPose(new Quaternionf(-90.0F, 0.0F, 1.0F, 0.0F));
        poseStack.translate(-0.5F, -0.5F, 0.5F);

        BlockState state = entity.getExplosiveState();
        int overlay = entity.getFuse() / 5 % 2 == 0 ? OverlayTexture.pack(OverlayTexture.u(1.0F), 10) : OverlayTexture.NO_OVERLAY;
        poseStack.translate(0.0F, 0.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f2);
        RenderSystem.polygonOffset(-3.0F, -3.0F);
        RenderSystem.enablePolygonOffset();
        blockRenderer.renderSingleBlock(state, poseStack, buffer, packedLight, overlay, ModelData.EMPTY, RenderType.entityCutout(this.getTextureLocation(entity)));
        RenderSystem.polygonOffset(0.0F, 0.0F);
        RenderSystem.disablePolygonOffset();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}