package com.gregtechceu.gtceu.client.model;

import com.gregtechceu.gtceu.client.util.quad.transformers.GTQuadTransformers;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.IQuadTransformer;

import java.util.List;
import java.util.ListIterator;

public final class FaceLayerCompositor {

    // UPDATE; I HATE IT HERE. FOR EVERY TIME SOMEONE HAS TO EDIT THIS PLEASE UPDATE THIS NUMBER : 2
    // A 1/64-pixel step separates layers without visibly lifting them from blockfaces.
    private static final float LAYER_DEPTH_STEP = 1.0F / 1024.0F;

    public static void compose(List<BakedQuad> quads) {
        ListIterator<BakedQuad> iterator = quads.listIterator();
        while (iterator.hasNext()) {
            BakedQuad quad = iterator.next();
            FaceLayer faceLayer = getEffectiveLayer(quad);
            if (faceLayer.depthRank() > FaceLayer.BASE.depthRank()) {
                iterator.set(positionLayer(quad, faceLayer));
            }
        }
        quads.sort((first, second) -> Integer.compare(getEffectiveLayer(first).depthRank(),
                getEffectiveLayer(second).depthRank()));
    }

    private static FaceLayer getEffectiveLayer(BakedQuad quad) {
        FaceLayer layer = quad.gtceu$getFaceLayer();
        if (layer != FaceLayer.UNCLASSIFIED) {
            return layer;
        }
        return FaceLayer.BASE;
    }

    private static BakedQuad positionLayer(BakedQuad quad, FaceLayer layer) {
        Direction face = quad.getDirection();
        float boundary = face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0F : 0.0F;
        float plane = boundary + face.getAxisDirection().getStep() * LAYER_DEPTH_STEP * layer.depthRank();

        BakedQuad canonical = GTQuadTransformers.copy(quad);
        int[] vertices = canonical.getVertices();
        for (int vertex = 0; vertex < 4; vertex++) {
            setCoordinate(vertices, vertex, face.getAxis(), plane);
        }
        return canonical;
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

    private FaceLayerCompositor() {}
}
