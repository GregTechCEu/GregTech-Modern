package com.lowdragmc.lowdraglib.client.scene;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public abstract class WorldSceneRenderer {

    public final Level world;
    protected int maxProgress;
    protected int progress;
    private Set<BlockPos> blocked;
    private Consumer<WorldSceneRenderer> afterWorldRender;
    private float fov = 45;

    public WorldSceneRenderer(Level world) {
        this.world = world;
    }

    public WorldSceneRenderer addRenderedBlocks(Collection<BlockPos> blocks, ISceneBlockRenderHook hook) {
        return this;
    }

    public WorldSceneRenderer setAfterWorldRender(Consumer<WorldSceneRenderer> consumer) {
        this.afterWorldRender = consumer;
        return this;
    }

    public Consumer<WorldSceneRenderer> getAfterWorldRender() {
        return afterWorldRender;
    }

    public void setBlocked(Set<BlockPos> blocked) {
        this.blocked = blocked == null ? null : new HashSet<>(blocked);
    }

    public Set<BlockPos> getBlocked() {
        return blocked;
    }

    public BlockHitResult getLastTraceResult() {
        return null;
    }

    public Vector3f getEyePos() {
        return new Vector3f();
    }

    public WorldSceneRenderer setCameraLookAt(Vector3f center, float zoom, double pitch, double yaw) {
        return this;
    }

    public WorldSceneRenderer setFov(float fov) {
        this.fov = fov;
        return this;
    }

    public float getFov() {
        return fov;
    }

    public interface VertexConsumerWrapper extends VertexConsumer {

        default void addOffset(int x, int y, int z) {}

        default void clearOffset() {}

        default void clearColor() {}
    }
}
