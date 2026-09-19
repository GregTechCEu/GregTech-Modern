package brachy.modularui.drawable;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/** Entity preview with a captured GUI transform, including rotation and nonuniform scaling. */
public record GuiEntityPreviewState(EntityRenderState entity, Vector3fc translation, Quaternionfc rotation,
                                     @Nullable Quaternionfc cameraAngle, Matrix3x2fc pose,
                                     int x0, int y0, int x1, int y1, float scale,
                                     @Nullable ScreenRectangle scissorArea,
                                     @Nullable ScreenRectangle bounds) implements PictureInPictureRenderState {
    public GuiEntityPreviewState {
        translation = new Vector3f(translation);
        rotation = new Quaternionf(rotation);
        cameraAngle = cameraAngle == null ? null : new Quaternionf(cameraAngle);
        pose = new Matrix3x2f(pose);
    }
}
