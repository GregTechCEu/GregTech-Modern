package brachy.modularui;

import brachy.modularui.utils.GuiPoseTransforms;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GuiPoseTransformsTest {
    @Test void snapshotRetainsRotationScaleAndTranslation() {
        var pose = new Matrix3x2f().translate(12, -3).rotate(.7f).scale(2, 4);
        var expected = pose.transformPosition(5, 9, new Vector2f());
        var snapshot = GuiPoseTransforms.snapshot(pose);
        pose.identity();
        var actual = snapshot.transformPosition(5, 9, 0, new Vector3f());
        assertEquals(expected.x, actual.x, .0001);
        assertEquals(expected.y, actual.y, .0001);
        assertEquals(0, actual.z);
    }

    @Test void viewportComposesWithExistingPose() {
        var base = new Matrix3x2f().translate(-6, 13).scale(2, 3);
        var viewport = new Matrix4f().translate(4, 8, 99).rotateZ(.3f).scale(2, 4, 1);
        var local = viewport.transformPosition(7, 2, 0, new Vector3f());
        var expected = base.transformPosition(local.x, local.y, new Vector2f());
        GuiPoseTransforms.apply(base, viewport);
        var actual = base.transformPosition(7, 2, new Vector2f());
        assertEquals(expected.x, actual.x, .0001);
        assertEquals(expected.y, actual.y, .0001);
    }

    public static void main(String[] args) {
        var test = new GuiPoseTransformsTest();
        test.snapshotRetainsRotationScaleAndTranslation();
        test.viewportComposesWithExistingPose();
        System.out.println("GUI pose transforms: 2 checks passed");
    }
}
