package brachy.modularui.drawable.schema;

import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.Nullable;

/** A preview lightmap using the schema level's environment, not the player's active level. */
public final class DummyLightTexture implements AutoCloseable {
    private @Nullable Lightmap lightmap;

    /** Capture on the extraction thread; no GPU resources are allocated here. */
    public static LightmapRenderState extract(LevelReader level) {
        var attributes = level.environmentAttributes();
        var state = new LightmapRenderState();
        state.needsUpdate = true;
        state.blockFactor = 1.5f;
        state.blockLightTint = ARGB.vector3fFromRGB24(attributes.getDimensionValue(EnvironmentAttributes.BLOCK_LIGHT_TINT));
        state.skyFactor = attributes.getDimensionValue(EnvironmentAttributes.SKY_LIGHT_FACTOR);
        state.skyLightColor = ARGB.vector3fFromRGB24(attributes.getDimensionValue(EnvironmentAttributes.SKY_LIGHT_COLOR));
        state.ambientColor = ARGB.vector3fFromRGB24(attributes.getDimensionValue(EnvironmentAttributes.AMBIENT_LIGHT_COLOR));
        state.brightness = Minecraft.getInstance().options.gamma().get().floatValue();
        return state;
    }

    /** Called on the rendering thread, with a previously extracted snapshot. */
    public void render(LightmapRenderState state) {
        if (lightmap == null) lightmap = new Lightmap();
        lightmap.render(state);
    }

    public void update(LevelReader level) {
        render(extract(level));
    }

    /** The scene's render pass must bind this view as its lightmap. */
    public GpuTextureView getTextureView() {
        if (lightmap == null) throw new IllegalStateException("The preview lightmap has not been rendered yet");
        return lightmap.getTextureView();
    }

    @Override
    public void close() {
        if (lightmap != null) {
            lightmap.close();
            lightmap = null;
        }
    }
}
