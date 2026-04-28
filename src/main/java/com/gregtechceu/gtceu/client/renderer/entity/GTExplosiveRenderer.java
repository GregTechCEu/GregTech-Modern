package com.gregtechceu.gtceu.client.renderer.entity;

import com.gregtechceu.gtceu.common.entity.GTExplosiveEntity;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;

@OnlyIn(Dist.CLIENT)
public class GTExplosiveRenderer<T extends GTExplosiveEntity> extends EntityRenderer<T, EntityRenderState> {

    public GTExplosiveRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void submit(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);
    }
}
