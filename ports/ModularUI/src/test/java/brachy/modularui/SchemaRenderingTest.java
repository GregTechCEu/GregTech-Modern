package brachy.modularui;

import brachy.modularui.drawable.schema.SchemaCameraTransform;
import brachy.modularui.drawable.schema.SchemaGeometry;
import brachy.modularui.drawable.schema.BlockHighlight;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SchemaRenderingTest {
    @Test void highlightFacesAndBordersAreCompleteQuads() {
        var hit = new BlockHitResult(new Vec3(2, 3, 4), Direction.UP, new BlockPos(2, 3, 4), false);
        var camera = new Vector3f(5, 5, 5);
        var solid = new BlockHighlight(0x80abcdef, false, -1).extract(hit, camera);
        assertEquals(4, solid.size());
        for (var vertex : solid) {
            assertEquals(4.005f, vertex.y(), .0001f);
            assertEquals(0x80abcdef, vertex.color());
        }
        assertEquals(24, new BlockHighlight(-1, true, -1).extract(hit, camera).size());
        assertEquals(16, new BlockHighlight(-1, false, .05f).extract(hit, camera).size());
        assertEquals(96, new BlockHighlight(-1, true, .05f).extract(hit, camera).size());
        assertTrue(new BlockHighlight(-1, true, 0).extract(hit, camera).isEmpty());
        assertTrue(new BlockHighlight(-1).extract(null, camera).isEmpty());
    }
    @Test void pickingMatchesPerspectiveAndOrthographicProjection() {
        var camera = new Vector3f(8, 6, 10);
        var target = new Vector3f(2, 1, -3);
        for (boolean ortho : new boolean[]{false, true}) {
            var combined = SchemaCameraTransform.projection(640, 360, ortho,
                    SchemaCameraTransform.orthoHeight(camera, target), false, false)
                    .mul(SchemaCameraTransform.view(camera, target));
            for (var point : new Vector3f[]{target, new Vector3f(3, 2, -1), new Vector3f(-2, 0, -5)}) {
                var screen = combined.project(point, new int[]{0, 0, 640, 360}, new Vector3f());
                var restored = SchemaCameraTransform.unproject(combined, screen.x, 360 - screen.y, 640, 360, screen.z);
                assertTrue(point.distance(restored) < .003f, () -> point + " != " + restored);
            }
            var near = SchemaCameraTransform.unproject(combined, 320, 180, 640, 360, 0);
            var far = SchemaCameraTransform.unproject(combined, 320, 180, 640, 360, 1);
            var ray = far.sub(near).normalize();
            assertTrue(ray.dot(new Vector3f(target).sub(camera).normalize()) > .9999f);
        }
    }
    @Test void reversedDepthMatchesBothGraphicsConventions() {
        for (boolean ortho : new boolean[]{false, true}) for (boolean zeroToOne : new boolean[]{false, true}) {
            var projection = SchemaCameraTransform.projection(400, 200, ortho, 12, true, zeroToOne);
            var near = projection.transformProject(new Vector3f(0, 0, -SchemaCameraTransform.NEAR));
            var far = projection.transformProject(new Vector3f(0, 0, -SchemaCameraTransform.FAR));
            assertEquals(1f, near.z, .0001f);
            assertEquals(zeroToOne ? 0f : -1f, far.z, .0001f);
        }
    }
    @Test void verticalAndCoincidentCamerasStayFinite() {
        for (var target : new Vector3f[]{new Vector3f(0, 10, 0), new Vector3f(0, -10, 0), new Vector3f()}) {
            Matrix4f view = SchemaCameraTransform.view(new Vector3f(), target);
            assertTrue(view.isFinite());
            assertEquals(1f, view.determinant(), .0001f);
        }
    }
    @Test void sectionOffsetsAndVertexAttributesAreCaptured() {
        var builder = new SchemaGeometry.Builder().offset(-16, 32, 16);
        for (int i = 0; i < 4; i++) builder.addVertex(15, 2, 3).setColor(0x80776655)
                .setUv(.25f, .75f).setUv1(4, 10).setUv2(160, 240).setNormal(0, 0, -1);
        var snapshot = builder.build();
        builder.offset(32, -16, 0);
        for (int i = 0; i < 4; i++) builder.addVertex(1, 1, 1);
        assertEquals(4, snapshot.size());
        assertEquals(8, builder.build().size());
        assertEquals(new SchemaGeometry.Vertex(-1, 34, 19, 0x80776655, .25f, .75f,
                4 | (10 << 16), 160 | (240 << 16), 0, 0, -1, 1), snapshot.getFirst());
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        // The collector is primitive-agnostic: pending vertices must survive a snapshot.
        var single = new SchemaGeometry.Builder().addVertex(0, 0, 0);
        assertEquals(1, single.build().size());
        assertEquals(1, single.build().size(), "Repeated snapshots must not duplicate the last vertex");
        assertThrows(IllegalStateException.class, () -> new SchemaGeometry.Builder().setColor(-1));
    }
    public static void main(String[] args) {
        var test = new SchemaRenderingTest();
        test.pickingMatchesPerspectiveAndOrthographicProjection();
        test.reversedDepthMatchesBothGraphicsConventions();
        test.verticalAndCoincidentCamerasStayFinite();
        test.sectionOffsetsAndVertexAttributesAreCaptured();
        test.highlightFacesAndBordersAreCompleteQuads();
        System.out.println("5 structure rendering checks passed");
    }
}
