package com.gregtechceu.gtceu.client.renderer.pipe.cache;

import com.gregtechceu.gtceu.client.renderer.pipe.quad.PipeQuadHelper;
import com.gregtechceu.gtceu.client.renderer.pipe.quad.QuadHelper;
import com.gregtechceu.gtceu.client.renderer.pipe.util.ColorData;
import com.gregtechceu.gtceu.client.renderer.pipe.util.SpriteInformation;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.List;

public class BlockableSQC extends StructureQuadCache {

    protected final EnumMap<Direction, SubListAddress> blockedCoords = new EnumMap<>(Direction.class);

    protected final SpriteInformation blockedTex;

    protected BlockableSQC(PipeQuadHelper helper, SpriteInformation endTex, SpriteInformation sideTex,
                           SpriteInformation blockedTex) {
        super(helper, endTex, sideTex);
        this.blockedTex = blockedTex;
        if (helper.getLayerCount() < 2) throw new IllegalStateException(
                "Cannot create a BlockableSQC without 2 or more layers present on the helper!");
    }

    public static @NotNull BlockableSQC create(PipeQuadHelper helper, SpriteInformation endTex,
                                               SpriteInformation sideTex, SpriteInformation blockedTex) {
        helper.initialize((facing, x1, y1, z1, x2, y2, z2) -> minLengthTube(facing, x1, y1, z1, x2, y2, z2,
                OVERLAY_DIST_1, 4));
        BlockableSQC cache = new BlockableSQC(helper, endTex, sideTex, blockedTex);
        cache.buildPrototype();
        return cache;
    }


    public static ImmutablePair<Vector3f, Vector3f> minLengthTube(@Nullable Direction facing, float x1, float y1,
                                                                  float z1, float x2,
                                                                  float y2, float z2, float g, float minLength) {
        if (facing == null) return QuadHelper.tubeOverlay(facing, x1, y1, z1, x2, y2, z2, g);
        return switch (facing) {
            case UP -> QuadHelper.tubeOverlay(facing, x1, Math.min(y1, y2 - minLength), z1, x2, y2, z2, g);
            case DOWN -> QuadHelper.tubeOverlay(facing, x1, y1, z1, x2, Math.max(y2, y1 + minLength), z2, g);
            case EAST -> QuadHelper.tubeOverlay(facing, Math.min(x1, x2 - minLength), y1, z1, x2, y2, z2, g);
            case WEST -> QuadHelper.tubeOverlay(facing, x1, y1, z1, Math.max(x2, x1 + minLength), y2, z2, g);
            case SOUTH -> QuadHelper.tubeOverlay(facing, x1, y1, Math.min(z1, z2 - minLength), x2, y2, z2, g);
            case NORTH -> QuadHelper.tubeOverlay(facing, x1, y1, z1, x2, y2, Math.max(z2, z1 + minLength), g);
        };
    }

    @Override
    protected List<BakedQuad> buildPrototypeInternal() {
        List<BakedQuad> quads = super.buildPrototypeInternal();
        buildBlocked(quads);
        return quads;
    }

    protected void buildBlocked(List<BakedQuad> list) {
        helper.setTargetSprite(blockedTex);
        for (Direction facing : GTUtil.DIRECTIONS) {
            int start = list.size();
            list.addAll(helper.visitTube(facing, 1));
            blockedCoords.put(facing, new SubListAddress(start, list.size()));
        }
    }

    @Override
    public void addToList(List<BakedQuad> list, byte connectionMask, byte closedMask, byte blockedMask, ColorData data,
                          byte coverMask) {
        List<BakedQuad> quads = cache.getQuads(data);
        for (Direction facing : GTUtil.DIRECTIONS) {
            if (GTUtil.evalMask(facing, connectionMask)) {
                list.addAll(tubeCoords.get(facing).getSublist(quads));
                if (!GTUtil.evalMask(facing, coverMask)) {
                    if (GTUtil.evalMask(facing, closedMask)) {
                        list.addAll(capperClosedCoords.get(facing).getSublist(quads));
                    } else {
                        list.addAll(capperCoords.get(facing).getSublist(quads));
                    }
                }
                if (GTUtil.evalMask(facing, blockedMask)) {
                    list.addAll(blockedCoords.get(facing).getSublist(quads));
                }
            } else {
                list.addAll(coreCoords.get(facing).getSublist(quads));
            }
        }
    }
}
