package brachy.modularui.utils;

import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

/** Bridges ModularUI's logical viewport matrices and Minecraft's 2D GUI extraction poses. */
public final class GuiPoseTransforms {
    private GuiPoseTransforms() {}

    public static Matrix4f snapshot(Matrix3x2fc pose) {
        return new Matrix4f().m00(pose.m00()).m01(pose.m01()).m10(pose.m10()).m11(pose.m11())
                .m30(pose.m20()).m31(pose.m21());
    }

    public static void apply(Matrix3x2f pose, Matrix4fc viewport) {
        pose.mul(new Matrix3x2f(viewport.m00(), viewport.m01(), viewport.m10(), viewport.m11(),
                viewport.m30(), viewport.m31()));
    }
}
