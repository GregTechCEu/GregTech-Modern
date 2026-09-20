package brachy.modularui;

import brachy.modularui.utils.MatrixUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MatrixProjectionTest {
    @Test void perspectiveRoundTripUsesHomogeneousDivision() {
        var matrix = new Matrix4f().perspective((float) Math.toRadians(60), 2, .1f, 100)
                .lookAt(3, 4, 8, 0, 0, 0, 0, 1, 0);
        var point = new Vector3f(.5f, -.2f, 1);
        int[] viewport = {17, 31, 800, 400};
        var screen = MatrixUtils.projectWorldToScreen(point, matrix, viewport);
        var restored = MatrixUtils.projectScreenToWorld(screen.x, screen.y, screen.z, matrix, viewport);
        assertTrue(point.distance(restored) < .001f);
        assertTrue(screen.z > 0 && screen.z < 1);
    }

    @Test void orthographicProjectionIncludesViewportOffset() {
        var matrix = new Matrix4f().ortho(-2, 2, -1, 1, .1f, 10);
        int[] viewport = {17, 31, 800, 400};
        var screen = MatrixUtils.projectWorldToScreen(new Vector3f(0, 0, -1), matrix, viewport);
        assertEquals(417, screen.x, .0001f);
        assertEquals(231, screen.y, .0001f);
        var restored = MatrixUtils.projectScreenToWorld(screen.x, screen.y, screen.z, matrix, viewport);
        assertEquals(-1, restored.z, .0001f);
    }

    @Test void rejectsEmptyViewport() {
        assertThrows(IllegalArgumentException.class, () -> MatrixUtils.projectWorldToScreen(
                new Vector3f(), new Matrix4f(), new int[]{0, 0, 0, 400}));
    }

    public static void main(String[] args) {
        var test = new MatrixProjectionTest();
        test.perspectiveRoundTripUsesHomogeneousDivision();
        test.orthographicProjectionIncludesViewportOffset();
        test.rejectsEmptyViewport();
        System.out.println("Projection: 3 checks passed");
    }
}
