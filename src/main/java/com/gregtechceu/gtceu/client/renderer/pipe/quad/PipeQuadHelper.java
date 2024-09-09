package com.gregtechceu.gtceu.client.renderer.pipe.quad;

import com.gregtechceu.gtceu.client.renderer.pipe.util.SpriteInformation;

import com.lowdragmc.lowdraglib.client.bakedpipeline.Quad;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class PipeQuadHelper {

    private SpriteInformation targetSprite;

    private final List<Pair<Vector3f, Vector3f>> coreBoxList = new ObjectArrayList<>();
    private final List<EnumMap<Direction, Pair<Vector3f, Vector3f>>> sideBoxesList = new ObjectArrayList<>();

    private float[] definition;

    public PipeQuadHelper(float x, float y, float z, float small, float large) {
        float xS = (x + small) * 16;
        float xL = (x + large) * 16;
        float yS = (y + small) * 16;
        float yL = (y + large) * 16;
        float zS = (z + small) * 16;
        float zL = (z + large) * 16;
        definition = new float[] { xS, xL, yS, yL, zS, zL };
    }

    @Contract("_ -> this")
    public PipeQuadHelper initialize(OverlayLayerDefinition... overlayLayers) {
        if (definition != null) {
            float xS = definition[0];
            float xL = definition[1];
            float yS = definition[2];
            float yL = definition[3];
            float zS = definition[4];
            float zL = definition[5];
            definition = null;
            generateBox(xS, xL, yS, yL, zS, zL,
                    (facing, x1, y1, z1, x2, y2, z2) -> QuadHelper.toPair(x1, y1, z1, x2, y2, z2));
            for (OverlayLayerDefinition definition : overlayLayers) {
                generateBox(xS, xL, yS, yL, zS, zL, definition);
            }
        }
        return this;
    }

    public int getLayerCount() {
        return coreBoxList.size();
    }

    private void generateBox(float xS, float xL, float yS, float yL, float zS, float zL,
                             @NotNull OverlayLayerDefinition definition) {
        coreBoxList.add(definition.computeBox(null, xS, yS, zS, xL, yL, zL));
        EnumMap<Direction, Pair<Vector3f, Vector3f>> sideBoxes = new EnumMap<>(Direction.class);
        sideBoxes.put(Direction.DOWN, definition.computeBox(Direction.DOWN, xS, 0, zS, xL, yS, zL));
        sideBoxes.put(Direction.UP, definition.computeBox(Direction.UP, xS, yL, zS, xL, 16, zL));
        sideBoxes.put(Direction.NORTH, definition.computeBox(Direction.NORTH, xS, yS, 0, xL, yL, zS));
        sideBoxes.put(Direction.SOUTH, definition.computeBox(Direction.SOUTH, xS, yS, zL, xL, yL, 16));
        sideBoxes.put(Direction.WEST, definition.computeBox(Direction.WEST, 0, yS, zS, xS, yL, zL));
        sideBoxes.put(Direction.EAST, definition.computeBox(Direction.EAST, xL, yS, zS, 16, yL, zL));
        sideBoxesList.add(sideBoxes);
    }

    @Contract("_, _, _, _ -> new")
    public static @NotNull PipeQuadHelper create(float thickness, double x, double y, double z) {
        float small = 0.5f - thickness / 2;
        float large = 0.5f + thickness / 2;
        return new PipeQuadHelper((float) x, (float) y, (float) z, small, large);
    }

    @Contract("_ -> new")
    public static @NotNull PipeQuadHelper create(float thickness) {
        return create(thickness, 0, 0, 0);
    }

    public void setTargetSprite(SpriteInformation sprite) {
        this.targetSprite = sprite;
    }

    public @NotNull BakedQuad visitCore(Direction facing) {
        return visitCore(facing, 0);
    }

    public @NotNull BakedQuad visitCore(Direction facing, int overlayLayer) {
        return visitQuad(facing, coreBoxList.get(overlayLayer), UVMapper.standard(0));
    }

    public @NotNull List<BakedQuad> visitTube(Direction facing) {
        return visitTube(facing, 0);
    }

    public @NotNull List<BakedQuad> visitTube(Direction facing, int overlayLayer) {
        List<BakedQuad> list = new ObjectArrayList<>();
        Pair<Vector3f, Vector3f> box = sideBoxesList.get(overlayLayer).get(facing);
        switch (facing.getAxis()) {
            case X -> {
                list.add(visitQuad(Direction.UP, box, UVMapper.standard(0)));
                list.add(visitQuad(Direction.DOWN, box, UVMapper.standard(0)));
                list.add(visitQuad(Direction.SOUTH, box, UVMapper.standard(0)));
                list.add(visitQuad(Direction.NORTH, box, UVMapper.flipped(0)));
            }
            case Y -> {
                list.add(visitQuad(Direction.EAST, box, UVMapper.standard(0)));
                list.add(visitQuad(Direction.WEST, box, UVMapper.standard(0)));
                list.add(visitQuad(Direction.SOUTH, box, UVMapper.standard(0)));
                list.add(visitQuad(Direction.NORTH, box, UVMapper.standard(0)));
            }
            case Z -> {
                list.add(visitQuad(Direction.UP, box, UVMapper.flipped(0)));
                list.add(visitQuad(Direction.DOWN, box, UVMapper.standard(0)));
                list.add(visitQuad(Direction.EAST, box, UVMapper.flipped(0)));
                list.add(visitQuad(Direction.WEST, box, UVMapper.standard(0)));
            }
        }
        return list;
    }

    public @NotNull BakedQuad visitCapper(Direction facing) {
        return visitCapper(facing, 0);
    }

    public @NotNull BakedQuad visitCapper(Direction facing, int overlayLayer) {
        return visitQuad(facing, sideBoxesList.get(overlayLayer).get(facing), UVMapper.standard(0));
    }

    public @NotNull BakedQuad visitQuad(Direction normal, Pair<Vector3f, Vector3f> box, UVMapper uv) {
        return QuadHelper.buildQuad(normal, box, uv, targetSprite);
    }

    public static @NotNull List<BakedQuad> createFrame(BakedModel frameModel, RandomSource randomSource,
                                                       ModelData modelData, RenderType renderType) {
        List<BakedQuad> list = new ObjectArrayList<>();
        // should always work
        list.addAll(frameModel.getQuads(null, null, randomSource, modelData, renderType)
                .stream()
                .map(quad -> {
                    BakedQuad q = Quad.from(quad, -0.002f).rebake();
                    return new BakedQuad(q.getVertices(), q.getTintIndex() + 3,
                            q.getDirection(), q.getSprite(), q.isShade(), q.hasAmbientOcclusion());
                })
                .toList());
        return list;
    }
}
