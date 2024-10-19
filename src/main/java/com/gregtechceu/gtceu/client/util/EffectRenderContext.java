package com.gregtechceu.gtceu.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

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
    private Frustum frustum = new Frustum(Minecraft.getInstance().levelRenderer.getFrustum());

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
    @Getter
    private float rotationX;
    @Getter
    private float rotationZ;
    @Getter
    private float rotationYZ;
    @Getter
    private float rotationXY;
    @Getter
    private float rotationXZ;

    @NotNull
    public EffectRenderContext update(@NotNull Entity renderViewEntity,
                                      double camX, double camY, double camZ,
                                      Frustum frustum, float partialTicks) {
        this.renderViewEntity = renderViewEntity;
        this.partialTicks = partialTicks;

        this.cameraX = camX;
        this.cameraY = camY;
        this.cameraZ = camZ;

        float i = 1 - (Minecraft.getInstance().options.getCameraType().isFirstPerson() ? 0 : 2);
        float pitch = renderViewEntity.getYRot();
        float yaw = renderViewEntity.getXRot();
        this.rotationX = Mth.cos(yaw * Mth.DEG_TO_RAD) * i;
        this.rotationZ = Mth.sin(yaw * Mth.DEG_TO_RAD) * i;
        this.rotationYZ = -this.rotationX * Mth.sin(pitch * Mth.DEG_TO_RAD) * i;
        this.rotationXY = this.rotationZ * Mth.sin(pitch * Mth.DEG_TO_RAD) * i;
        this.rotationXZ = Mth.cos(pitch * Mth.DEG_TO_RAD);

        this.frustum = frustum;

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
