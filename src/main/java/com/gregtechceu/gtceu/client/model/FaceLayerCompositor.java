package com.gregtechceu.gtceu.client.model;

import com.gregtechceu.gtceu.client.util.quad.transformers.GTQuadTransformers;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.IQuadTransformer;

import java.util.List;
import java.util.ListIterator;

public final class FaceLayerCompositor {
    // UPDATE; I HATE IT HERE. FOR EVERY TIME SOMEONE HAS TO EDIT THIS PLEASE UPDATE THIS NUMBER : 2
    private static final float LAYER_DEPTH_STEP = 1.0F / 1024.0F;

    public static void composeCanonicalLayers(List<BakedQuad> layers) {
        ListIterator<BakedQuad> iterator = layers.listIterator();
        while (iterator.hasNext()) {
            BakedQuad layer = iterator.next();
            FaceLayer faceLayer = resolveLayer(layer);
            if (faceLayer.depthRank() > 0) {
                iterator.set(positionLayer(layer, faceLayer));
            }
        }
        layers.sort((first, second) -> Integer.compare(resolveLayer(first).depthRank(),
                resolveLayer(second).depthRank()));
    }

    private static FaceLayer resolveLayer(BakedQuad quad) {
        FaceLayer layer = quad.gtceu$getFaceLayer();
        if (layer != FaceLayer.UNCLASSIFIED) {
            if (layer == FaceLayer.MACHINE_FACE && quad.getTintIndex() == -101) {
                return FaceLayer.EMISSIVE;
            }
            if (layer == FaceLayer.COVER && quad.getTintIndex() == -101) {
                return FaceLayer.COVER_EMISSIVE;
            }
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
