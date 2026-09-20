package brachy.modularui.drawable.schema;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/** Shared camera mathematics for drawing and picking, independent of a live framebuffer. */
public final class SchemaCameraTransform {
    public static final float NEAR = 0.05f;
    public static final float FAR = 10000f;
    public static final float FOV = (float) Math.toRadians(60);
    private SchemaCameraTransform() {}

    public static Matrix4f view(Vector3fc camera, Vector3fc target) {
        Vector3f direction = new Vector3f(target).sub(camera);
        if (direction.lengthSquared() < 1e-10f) direction.set(0, 0, -1);
        Vector3f up = Math.abs(direction.normalize().y) > 0.999f ? new Vector3f(0, 0, 1) : new Vector3f(0, 1, 0);
        return new Matrix4f().lookAt(camera, new Vector3f(camera).add(direction), up);
    }

    public static float orthoHeight(Vector3fc camera, Vector3fc target) {
        return Math.max(NEAR, camera.distance(target)) * 2 * (float) Math.tan(FOV / 2);
    }

    public static Matrix4f projection(int width, int height, boolean orthographic, float orthoHeight,
                                       boolean reversedDepth, boolean zeroToOne) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Preview dimensions must be positive");
        float near = reversedDepth ? FAR : NEAR, far = reversedDepth ? NEAR : FAR;
        float aspect = (float) width / height;
        return orthographic
                ? new Matrix4f().setOrtho(-orthoHeight * aspect / 2, orthoHeight * aspect / 2,
                        -orthoHeight / 2, orthoHeight / 2, near, far, zeroToOne)
                : new Matrix4f().setPerspective(FOV, aspect, near, far, zeroToOne);
    }

    public static Vector3f unproject(Matrix4f combined, float x, float y, int width, int height, float depth) {
        return combined.unproject(x, height - y, depth, new int[]{0, 0, width, height}, new Vector3f());
    }
}
