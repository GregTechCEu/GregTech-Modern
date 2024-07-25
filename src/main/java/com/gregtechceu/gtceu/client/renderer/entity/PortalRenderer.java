package com.gregtechceu.gtceu.client.renderer.entity;

import com.gregtechceu.gtceu.common.entity.PortalEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class PortalRenderer<T extends PortalEntity> extends EntityRenderer<T> {

    private static final ResourceLocation texture = new ResourceLocation("gtceu:textures/entity/gtportal.png");


    public PortalRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.3f;
        //this.model = new PortalModel(context.bakeLayer(new ModelLayerLocation(new ResourceLocation("gtceu:model/entity/portal"), "portal")));
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        //super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.0f, 0.5f + 0.5f, 0.0f);
        float scaleX = 0.0625F, scaleY = 0.0625F, scaleZ = 0.0625F;
        float translateY = 0.F;
        if (entity.isOpening()) {
            if (entity.getTimeToDespawn() <= 195) {
                scaleY *= Mth.clamp((195.0f - entity.getTimeToDespawn() + partialTick) / 5.0f, 0.05f, 1.0f);
                translateY = 0.5f * (1.0f - Mth.clamp((195.0f - entity.getTimeToDespawn() + partialTick) / 5.0f, 0.0f, 1.0f));
            } else {
                scaleX *= Mth.clamp((200.0f - entity.getTimeToDespawn() + partialTick) / 5.0f, 0.05f, 1.0f);
                scaleY *= 0.05f;
                scaleZ *= Mth.clamp((200.0f - entity.getTimeToDespawn() + partialTick) / 5.0f, 0.05f, 1.0f);
                translateY = 0.5f;
            }
        } else if(entity.isClosing()) {
            if (entity.getTimeToDespawn() >= 5) {
                scaleY *= Mth.clamp((entity.getTimeToDespawn() - partialTick - 5.0f) / 5.0f, 0.05f, 1.0f);
                translateY = 0.5f * (1.0f - Mth.clamp((entity.getTimeToDespawn() - partialTick - 5.0f) / 5.0f, 0.0f, 1.0f));
            } else {
                scaleX *= Mth.clamp((entity.getTimeToDespawn() - partialTick) / 5.0f, 0.05f, 1.0f);
                scaleY *= 0.05f;
                scaleZ *= Mth.clamp((entity.getTimeToDespawn() - partialTick) / 5.0f, 0.05f, 1.0f);
                translateY = 0.5f;
            }
        }
        poseStack.translate(0, translateY, 0);
        poseStack.scale(scaleX, scaleY, scaleZ);

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(texture));
        PoseStack.Pose pose = poseStack.last();
        consumer.vertex(pose.pose(),0, 0, 0).color(1,1,1,1).uv(0,0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0,0,1).endVertex();
        consumer.vertex(pose.pose(),1, 0, 0).color(1,1,1,1).uv(1,0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0,0,1).endVertex();
        consumer.vertex(pose.pose(),1, 1, 0).color(1,1,1,1).uv(1,1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0,0,1).endVertex();
        consumer.vertex(pose.pose(),0, 1, 0).color(1,1,1,1).uv(0,1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0,0,1).endVertex();

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }
}
