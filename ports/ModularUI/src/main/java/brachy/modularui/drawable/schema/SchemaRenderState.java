package brachy.modularui.drawable.schema;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.List;

/** A frame's scene data; neither the schema level nor live block entities are retained. */
public record SchemaRenderState(List<SchemaGeometry.Layer> layers, List<BlockEntityRenderState> blockEntities,
                                List<SchemaGeometry.Vertex> highlight, LightmapRenderState lightmap,
                                Matrix4fc view, Vector3fc camera, boolean orthographic, float orthoHeight,
                                int clearColor, Matrix3x2fc pose, int x0, int y0, int x1, int y1,
                                @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds)
        implements PictureInPictureRenderState {
    public SchemaRenderState {
        layers = List.copyOf(layers);
        blockEntities = List.copyOf(blockEntities);
        highlight = List.copyOf(highlight);
        view = new Matrix4f(view);
        camera = new Vector3f(camera);
        pose = new Matrix3x2f(pose);
    }
    @Override public float scale() { return 1; }
}
