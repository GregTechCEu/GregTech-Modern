package com.gregtechceu.gtceu.client.renderer.pipe;

import com.gregtechceu.gtceu.client.renderer.pipe.util.CacheKey;
import com.gregtechceu.gtceu.client.renderer.pipe.util.ColorData;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class PipeItemModel<K extends CacheKey> implements BakedModel {

    private static final Map<ItemDisplayContext, Matrix4f> CAMERA_TRANSFORMS = new HashMap<>();

    static {
        CAMERA_TRANSFORMS.put(ItemDisplayContext.NONE, mul(null, null, null, null));
        CAMERA_TRANSFORMS.put(ItemDisplayContext.GUI, mul(null, rotDegrees(30, -45, 0), scale(0.625f), null));
        CAMERA_TRANSFORMS.put(ItemDisplayContext.GROUND, mul(null, null, scale(0.25f), null));
        CAMERA_TRANSFORMS.put(ItemDisplayContext.FIXED, mul(null, rotDegrees(0, 90, 0), scale(0.5f), null));
        Matrix4f matrix4f = mul(null, rotDegrees(75, 45, 0), scale(0.375f), null);
        CAMERA_TRANSFORMS.put(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, matrix4f);
        CAMERA_TRANSFORMS.put(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, matrix4f);
        matrix4f = mul(null, rotDegrees(0, 45, 0), scale(0.4f), null);
        CAMERA_TRANSFORMS.put(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, matrix4f);
        CAMERA_TRANSFORMS.put(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, matrix4f);
    }

    private static Vector3f scale(float scale) {
        return new Vector3f(scale, scale, scale);
    }

    private static Quaternionf rotDegrees(float x, float y, float z) {
        return quatFromXYZDegrees(new Vector3f(x, y, z));
    }

    private final AbstractPipeModel<K> basis;
    private final K key;
    private final ColorData data;

    public PipeItemModel(AbstractPipeModel<K> basis, K key, ColorData data) {
        this.basis = basis;
        this.key = key;
        this.data = data;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) {
        byte z = 0;
        return basis.getQuads(key, (byte) 0b1100, z, z, data, null, z, z);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return basis.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return basis.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return basis.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack,
                                     boolean applyLeftHandTransform) {
        poseStack.mulPoseMatrix(CAMERA_TRANSFORMS.get(transformType));
        return BakedModel.super.applyTransform(transformType, poseStack, applyLeftHandTransform);
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return basis.getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    public static Matrix4f mul(@Nullable Vector3f translation, @Nullable Quaternionf leftRot, @Nullable Vector3f scale,
                               @Nullable Quaternionf rightRot) {
        Matrix4f res = new Matrix4f(), t = new Matrix4f();
        res.identity();
        if (leftRot != null) {
            t.set(leftRot);
            res.mul(t);
        }
        if (scale != null) {
            t.identity();
            t.m00(scale.x);
            t.m11(scale.y);
            t.m22(scale.z);
            res.mul(t);
        }
        if (rightRot != null) {
            t.set(rightRot);
            res.mul(t);
        }
        if (translation != null) res.setTranslation(translation);
        return res;
    }

    public static Quaternionf quatFromXYZDegrees(Vector3f xyz) {
        return quatFromXYZ((float) Math.toRadians(xyz.x), (float) Math.toRadians(xyz.y), (float) Math.toRadians(xyz.z));
    }

    public static Quaternionf quatFromXYZ(Vector3f xyz) {
        return quatFromXYZ(xyz.x, xyz.y, xyz.z);
    }

    public static Quaternionf quatFromXYZ(float x, float y, float z) {
        Quaternionf ret = new Quaternionf(0, 0, 0, 1), t = new Quaternionf();
        t.set((float) Math.sin(x / 2), 0, 0, (float) Math.cos(x / 2));
        ret.mul(t);
        t.set(0, (float) Math.sin(y / 2), 0, (float) Math.cos(y / 2));
        ret.mul(t);
        t.set(0, 0, (float) Math.sin(z / 2), (float) Math.cos(z / 2));
        ret.mul(t);
        return ret;
    }
}
