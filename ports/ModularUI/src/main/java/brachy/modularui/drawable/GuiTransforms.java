package brachy.modularui.drawable;

import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

/** Bridges widget coordinates to Minecraft's two-dimensional GUI pose stack. */
public final class GuiTransforms {
    private GuiTransforms() {}

    public static Matrix3x2f toGui(Matrix4fc matrix) {
        return new Matrix3x2f(matrix.m00(), matrix.m01(), matrix.m10(), matrix.m11(), matrix.m30(), matrix.m31());
    }

    public static Matrix4f toWorld(Matrix3x2fc matrix) {
        return new Matrix4f().m00(matrix.m00()).m01(matrix.m01()).m10(matrix.m10())
                .m11(matrix.m11()).m30(matrix.m20()).m31(matrix.m21());
    }
}
