package com.gregtechceu.gtceu.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Collection of various information for rendering purposes.
 */
@Accessors(fluent = true)
public final class EffectRenderContext {

    private static final EffectRenderContext instance = new EffectRenderContext();

    public static EffectRenderContext getInstance() {
        return instance;
    }

    @Getter
    private final Frustum camera = new Frustum(Minecraft.getInstance().levelRenderer.getFrustum());

    @Nullable
    private Entity renderViewEntity;
    @Getter
    private float partialTicks;
    @Getter
    private double cameraX;
    @Getter
    private double cameraY;
    @Getter
    private double cameraZ;
    @NotNull
    @Getter
    private Vec3 cameraViewDir = Vec3.ZERO;
    @Getter
    private float rotationY;
    @Getter
    private float rotationX;
    @Getter
    private float rotationYZ;
    @Getter
    private float rotationXY;
    @Getter
    private float rotationXZ;

    @NotNull
    public EffectRenderContext update(@NotNull Entity renderViewEntity, float partialTicks) {
        this.renderViewEntity = renderViewEntity;
        this.partialTicks = partialTicks;

        this.cameraX = renderViewEntity.xOld +
                (renderViewEntity.getX() - renderViewEntity.xOld) * partialTicks;
        this.cameraY = renderViewEntity.yOld +
                (renderViewEntity.getY() - renderViewEntity.yOld) * partialTicks;
        this.cameraZ = renderViewEntity.zOld +
                (renderViewEntity.getZ() - renderViewEntity.zOld) * partialTicks;
        this.cameraViewDir = renderViewEntity.getViewVector(partialTicks);

        this.rotationY = Minecraft.getInstance().gameRenderer.getMainCamera().getYRot();
        this.rotationX = Minecraft.getInstance().gameRenderer.getMainCamera().getXRot();
        // this.rotationYZ = ActiveRenderInfo.getRotationYZ();
        // this.rotationXY = ActiveRenderInfo.getRotationXY();
        // this.rotationXZ = ActiveRenderInfo.getRotationXZ();

        this.camera.prepare(this.cameraX, this.cameraY, this.cameraZ);

        return this;
    }

    /**
     * @return render view entity
     */
    @NotNull
    public Entity renderViewEntity() {
        return Objects.requireNonNull(renderViewEntity, "renderViewEntity not available yet");
    }
}
