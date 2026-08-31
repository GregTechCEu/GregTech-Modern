package com.gregtechceu.gtceu.client.model;

import com.gregtechceu.gtceu.client.util.quad.transformers.GTQuadTransformers;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.IQuadTransformer;

import java.util.List;
import java.util.ListIterator;

public final class FaceLayerCompositor {

    private static final float POSITION_EPSILON = 1.0e-5F;
    // Existing machine models use small outward offsets for layered face textures. Larger offsets are most likely
    // intentional.
    // TODO: This is for handling our model offsets, and is a good catchall on other blocks made with our overlay
    // systems, in the future I'd like to rip this out and just have proper offsets, but OOS for now.
    // UPDATE; I HATE IT HERE. FOR EVERY TIME SOMEONE HAS TO EDIT THIS PLEASE UPDATE THIS NUMBER : 1
    @Deprecated(since = "8.0")
    private static final float MAX_LEGACY_OUTWARD_OFFSET = 0.025F;

    public static void retainBaseLayers(List<BakedQuad> layers) {
        ListIterator<BakedQuad> iterator = layers.listIterator();
        while (iterator.hasNext()) {
            if (resolveLayer(iterator.next()).rendersAboveBase()) {
                iterator.remove();
            }
        }
    }

    public static void retainFaceLayers(List<BakedQuad> layers) {
        ListIterator<BakedQuad> iterator = layers.listIterator();
        while (iterator.hasNext()) {
            BakedQuad layer = iterator.next();
            if (!resolveLayer(layer).rendersAboveBase()) {
                iterator.remove();
                continue;
            }
            iterator.set(canonicalize(layer));
        }
    }

    private static FaceLayer resolveLayer(BakedQuad quad) {
        FaceLayer layer = quad.gtceu$getFaceLayer();
        if (layer != FaceLayer.UNCLASSIFIED) {
            return layer;
        }
        return isLegacyOverlay(quad) ? FaceLayer.MACHINE_FACE : FaceLayer.BASE;
    }

    private static boolean isLegacyOverlay(BakedQuad quad) {
        Direction face = quad.getDirection();
        int[] vertices = quad.getVertices();
        float boundary = face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0F : 0.0F;
        float plane = coordinate(vertices, 0, face.getAxis());

        for (int vertex = 1; vertex < 4; vertex++) {
            if (Math.abs(coordinate(vertices, vertex, face.getAxis()) - plane) > POSITION_EPSILON) {
                return false;
            }
        }

        float outwardOffset = (plane - boundary) * face.getAxisDirection().getStep();
        return outwardOffset > POSITION_EPSILON && outwardOffset <= MAX_LEGACY_OUTWARD_OFFSET;
    }

    private static BakedQuad canonicalize(BakedQuad quad) {
        Direction face = quad.getDirection();
        float boundary = face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0F : 0.0F;

        BakedQuad canonical = GTQuadTransformers.copy(quad);
        int[] vertices = canonical.getVertices();
        for (int vertex = 0; vertex < 4; vertex++) {
            setCoordinate(vertices, vertex, Direction.Axis.X,
                    canonicalCoordinate(coordinate(vertices, vertex, Direction.Axis.X)));
            setCoordinate(vertices, vertex, Direction.Axis.Y,
                    canonicalCoordinate(coordinate(vertices, vertex, Direction.Axis.Y)));
            setCoordinate(vertices, vertex, Direction.Axis.Z,
                    canonicalCoordinate(coordinate(vertices, vertex, Direction.Axis.Z)));
            setCoordinate(vertices, vertex, face.getAxis(), boundary);
        }
        return canonical;
    }

    private static float coordinate(int[] vertices, int vertex, Direction.Axis axis) {
        int offset = vertex * IQuadTransformer.STRIDE + IQuadTransformer.POSITION + coordinateIndex(axis);
        return Float.intBitsToFloat(vertices[offset]);
    }

    private static void setCoordinate(int[] vertices, int vertex, Direction.Axis axis, float coordinate) {
        int offset = vertex * IQuadTransformer.STRIDE + IQuadTransformer.POSITION + coordinateIndex(axis);
        vertices[offset] = Float.floatToRawIntBits(coordinate);
    }

    private static int coordinateIndex(Direction.Axis axis) {
        return switch (axis) {
            case X -> 0;
            case Y -> 1;
            case Z -> 2;
        };
    }

    private static float canonicalCoordinate(float coordinate) {
        if (coordinate < 0.0F && coordinate >= -MAX_LEGACY_OUTWARD_OFFSET) {
            return 0.0F;
        }
        if (coordinate > 1.0F && coordinate <= 1.0F + MAX_LEGACY_OUTWARD_OFFSET) {
            return 1.0F;
        }
        return coordinate;
    }

    private FaceLayerCompositor() {}
}
