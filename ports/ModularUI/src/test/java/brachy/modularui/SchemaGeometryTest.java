package brachy.modularui;

import brachy.modularui.drawable.schema.SchemaGeometry;
import brachy.modularui.drawable.progress.RadialMask;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SchemaGeometryTest {
    @Test void fluidProxyOffsetsChainedAndBulkVertices() {
        var builder = new SchemaGeometry.Builder();
        var proxy = new brachy.modularui.drawable.schema.LiquidVertexConsumer(builder,
                net.minecraft.core.SectionPos.of(2, -1, 3));
        proxy.addVertex(1, 2, 3).setColor(0xff123456).addVertex(4, 5, 6).setColor(0xffabcdef);
        proxy.addVertex(7, 8, 9, -1, .5f, .25f, 10, 20, 0, 1, 0);
        var vertices = builder.build();
        assertEquals(33, vertices.get(0).x());
        assertEquals(-14, vertices.get(0).y());
        assertEquals(51, vertices.get(0).z());
        assertEquals(36, vertices.get(1).x());
        assertEquals(0xffabcdef, vertices.get(1).color());
        assertEquals(39, vertices.get(2).x());
        assertEquals(-8, vertices.get(2).y());
        assertEquals(57, vertices.get(2).z());
        assertEquals(20, vertices.get(2).light());
    }

    @Test void snapshotsAttributesAndOffsets() {
        var builder = new SchemaGeometry.Builder().offset(16, -32, 48);
        builder.addVertex(1, 2, 3).setColor(10, 20, 30, 40).setUv(.25f, .75f)
                .setUv1(3, 4).setUv2(5, 6).setNormal(0, 1, 0).setLineWidth(2);
        var first = builder.build();
        assertEquals(new SchemaGeometry.Vertex(17, -30, 51, 0x280a141e, .25f, .75f,
                0x40003, 0x60005, 0, 1, 0, 2), first.getFirst());
        builder.offset(0, 0, 0).addVertex(0, 0, 0);
        assertEquals(1, first.size());
        assertEquals(2, builder.build().size());
        assertEquals(2, builder.build().size());
        assertThrows(UnsupportedOperationException.class, () -> first.clear());
    }

    @Test void resetsAttributesBetweenVertices() {
        var builder = new SchemaGeometry.Builder();
        assertThrows(IllegalStateException.class, () -> builder.setColor(0));
        builder.addVertex(0, 0, 0).setColor(0).setUv(1, 1).setLight(42);
        builder.addVertex(1, 1, 1);
        var vertex = builder.build().get(1);
        assertEquals(-1, vertex.color());
        assertEquals(0, vertex.light());
        assertEquals(0, vertex.u());
    }

    @Test void radialMasksCoverRectangularBoundsInBothDirections() {
        for (boolean clockwise : new boolean[]{true, false}) {
            for (int eighth = 1; eighth <= 8; eighth++) {
                var vertices = RadialMask.vertices(eighth / 8f, 10, 20, 80, 30, clockwise);
                assertTrue(vertices.size() >= 3);
                for (var vertex : vertices) {
                    assertTrue(vertex.x() >= 10 && vertex.x() <= 90);
                    assertTrue(vertex.y() >= 20 && vertex.y() <= 50);
                }
                double area = 0;
                for (int i = 1; i + 1 < vertices.size(); i++) {
                    var a = vertices.getFirst();
                    var b = vertices.get(i);
                    var c = vertices.get(i + 1);
                    area += Math.abs((b.x() - a.x()) * (c.y() - a.y()) - (c.x() - a.x()) * (b.y() - a.y())) / 2;
                }
                assertEquals(80 * 30 * eighth / 8.0, area, .001);
            }
        }
        assertTrue(RadialMask.vertices(0, 0, 0, 10, 10, true).isEmpty());
        assertTrue(RadialMask.vertices(Float.NaN, 0, 0, 10, 10, true).isEmpty());
    }

    public static void main(String[] args) {
        var test = new SchemaGeometryTest();
        test.snapshotsAttributesAndOffsets();
        test.resetsAttributesBetweenVertices();
        test.radialMasksCoverRectangularBoundsInBothDirections();
        test.fluidProxyOffsetsChainedAndBulkVertices();
        System.out.println("Structure and radial geometry: 4 checks passed");
    }
}
