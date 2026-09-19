package brachy.modularui;

import brachy.modularui.drawable.GuiShapeRenderState;
import brachy.modularui.drawable.GuiShapeRenderState.Vertex;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.joml.Matrix3x2f;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static brachy.modularui.drawable.GuiShapeRenderState.Topology.*;
import static org.junit.jupiter.api.Assertions.*;

public class GuiShapeRenderStateTest {
    private static final List<Vertex> QUAD = List.of(new Vertex(0, 0, 1), new Vertex(0, 2, 2),
            new Vertex(4, 2, 3), new Vertex(4, 0, 4));

    @Test
    void capturesTransformAndVertices() {
        var pose = new Matrix3x2f().translate(10.25f, -3.5f).scale(2, 3);
        var vertices = new ArrayList<>(QUAD);
        var state = GuiShapeRenderState.extract(pose, null, vertices, QUADS);
        pose.identity();
        vertices.clear();
        assertNotNull(state);
        assertEquals(new Vertex(18.25f, 2.5f, 3), state.vertices().get(2));
        assertEquals(new ScreenRectangle(10, -4, 9, 7), state.bounds());
        assertThrows(UnsupportedOperationException.class, () -> state.vertices().clear());
    }

    @Test
    void clipsBoundsAndRejectsInvisibleGeometry() {
        var clip = new ScreenRectangle(1, 1, 2, 2);
        var state = GuiShapeRenderState.extract(new Matrix3x2f(), clip, QUAD, QUADS);
        assertNotNull(state);
        assertEquals(new ScreenRectangle(1, 1, 2, 1), state.bounds());
        assertSame(clip, state.scissorArea());
        assertNull(GuiShapeRenderState.extract(new Matrix3x2f(), new ScreenRectangle(9, 9, 1, 1), QUAD, QUADS));
    }

    @Test
    void fanTrianglesRemainIndependentQuads() {
        var state = GuiShapeRenderState.extract(new Matrix3x2f(), null, QUAD, TRIANGLE_FAN);
        assertNotNull(state);
        assertEquals(List.of(QUAD.get(0), QUAD.get(1), QUAD.get(2), QUAD.get(2),
                QUAD.get(0), QUAD.get(2), QUAD.get(3), QUAD.get(3)), state.vertices());
    }

    @Test
    void stripPreservesAlternatingWinding() {
        var state = GuiShapeRenderState.extract(new Matrix3x2f(), null, QUAD, TRIANGLE_STRIP);
        assertNotNull(state);
        assertEquals(List.of(QUAD.get(0), QUAD.get(1), QUAD.get(2), QUAD.get(2),
                QUAD.get(2), QUAD.get(1), QUAD.get(3), QUAD.get(3)), state.vertices());
    }

    @Test
    void validatesPrimitiveCounts() {
        assertNull(GuiShapeRenderState.extract(new Matrix3x2f(), null, List.of(), QUADS));
        assertNull(GuiShapeRenderState.extract(new Matrix3x2f(), null, QUAD.subList(0, 2), TRIANGLE_FAN));
        assertThrows(IllegalArgumentException.class,
                () -> GuiShapeRenderState.extract(new Matrix3x2f(), null, QUAD.subList(0, 3), QUADS));
    }

    /** Allows isolated checks while unrelated legacy sources still prevent the full Gradle test task. */
    public static void main(String[] args) {
        var test = new GuiShapeRenderStateTest();
        test.capturesTransformAndVertices();
        test.clipsBoundsAndRejectsInvisibleGeometry();
        test.fanTrianglesRemainIndependentQuads();
        test.stripPreservesAlternatingWinding();
        test.validatesPrimitiveCounts();
        System.out.println("GUI geometry: 5 checks passed");
    }
}
