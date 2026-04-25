package com.gregtechceu.gtceu.client.bloom;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

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

    private @Nullable Entity renderViewEntity;
    @Getter
    private float partialTicks;
    @Getter
    private @UnknownNullability Vec3 camPos;
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
    public EffectRenderContext update(@NotNull Entity renderViewEntity, Vec3 camPos,
                                      Frustum frustum, float partialTicks) {
        this.renderViewEntity = renderViewEntity;
        this.camPos = camPos;
        this.partialTicks = partialTicks;

        float i = Minecraft.getInstance().options.getCameraType().isFirstPerson() ? 1 : -1;
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
    public Entity getRenderViewEntity() {
        return Objects.requireNonNull(renderViewEntity, "renderViewEntity not available yet");
    }
}
