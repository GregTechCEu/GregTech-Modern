package brachy.modularui.utils;

import net.minecraft.util.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class MUIRenderTypes extends RenderType {

    private static final Function<Identifier, RenderType> GUI_TEXTURE = Util.memoize((texture) -> {
        return create("gui_texture", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS,
                RenderType.TRANSIENT_BUFFER_SIZE, false, true,
                CompositeState.builder()
                        .setShaderState(RENDERTYPE_TEXT_SHADER)
                        .setTextureState(new TextureStateShard(texture, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setLightmapState(LIGHTMAP)
                        .createCompositeState(false));
    });

    private static final RenderType GUI_TRIANGLE_STRIP = RenderType.create("gui_triangle_strip",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP, 256, false, false,
            CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_GUI_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .createCompositeState(false));

    private static final RenderType GUI_TRIANGLE_FAN = RenderType.create("gui_triangle_fan",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_FAN, 256, false, false,
            CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_GUI_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .createCompositeState(false));

    private static final RenderType GUI_OVERLAY_TRIANGLE_FAN = RenderType.create("gui_overlay_triangle_fan",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_FAN, 256, false, false,
            CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_GUI_OVERLAY_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false));


    private MUIRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                           boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType guiTexture(Identifier texture) {
        return GUI_TEXTURE.apply(texture);
    }

    public static RenderType guiTriangleStrip() {
        return GUI_TRIANGLE_STRIP;
    }

    public static RenderType guiTriangleFan() {
        return GUI_TRIANGLE_FAN;
    }

    public static RenderType guiOverlayTriangleFan() {
        return GUI_OVERLAY_TRIANGLE_FAN;
    }
}
