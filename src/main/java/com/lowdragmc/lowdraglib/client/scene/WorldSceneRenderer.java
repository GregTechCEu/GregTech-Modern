package com.lowdragmc.lowdraglib.client.scene;

import net.minecraft.world.level.Level;

import org.joml.Vector3f;

/**
 * Compatibility facade for legacy gtceu callers that imported the pre-26.1
 * {@code com.lowdragmc.lowdraglib.client.scene.WorldSceneRenderer}. Inherits
 * the real implementation from
 * {@link com.lowdragmc.lowdraglib2.client.scene.WorldSceneRenderer} so calls
 * like {@link #addRenderedBlocks}, {@link #getLastTraceResult},
 * {@link #setAfterWorldRender} actually do something — pre-rewire this class
 * was a no-op shim and the multiblock pattern preview rendered nothing.
 *
 * <p>
 * The only behaviour this facade still owns is the legacy four-arg
 * {@code setCameraLookAt(center, zoom, pitch, yaw)}: LDLib2's orbital
 * overload is {@code setCameraLookAt(lookAt, radius, yaw, pitch)} — same
 * semantics but yaw/pitch are reversed.
 * </p>
 */
public abstract class WorldSceneRenderer extends com.lowdragmc.lowdraglib2.client.scene.WorldSceneRenderer {

    public WorldSceneRenderer(Level world) {
        super(world);
    }

    /**
     * Legacy four-arg signature. Delegates to LDLib2's orbital overload with
     * the yaw/pitch order corrected.
     *
     * @param center world-space point the camera looks at
     * @param zoom   camera radius (smaller = closer)
     * @param pitch  pitch in radians
     * @param yaw    yaw in radians
     */
    public WorldSceneRenderer setCameraLookAt(Vector3f center, float zoom, double pitch, double yaw) {
        super.setCameraLookAt(center, zoom, yaw, pitch);
        return this;
    }

    public interface VertexConsumerWrapper extends com.mojang.blaze3d.vertex.VertexConsumer {

        default void addOffset(int x, int y, int z) {}

        default void clearOffset() {}

        default void clearColor() {}
    }
}
