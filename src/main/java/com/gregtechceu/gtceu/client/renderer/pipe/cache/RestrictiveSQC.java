package com.gregtechceu.gtceu.client.renderer.pipe.cache;

import com.gregtechceu.gtceu.client.renderer.pipe.quad.PipeQuadHelper;
import com.gregtechceu.gtceu.client.renderer.pipe.quad.QuadHelper;
import com.gregtechceu.gtceu.client.renderer.pipe.util.ColorData;
import com.gregtechceu.gtceu.client.renderer.pipe.util.SpriteInformation;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;

import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;

public class RestrictiveSQC extends BlockableSQC {

    protected final EnumMap<Direction, SubListAddress> restrictiveCoords = new EnumMap<>(Direction.class);

    private final SpriteInformation restrictiveTex;

    protected RestrictiveSQC(PipeQuadHelper helper, SpriteInformation endTex, SpriteInformation sideTex,
                             SpriteInformation blockedTex, SpriteInformation restrictiveTex) {
        super(helper, endTex, sideTex, blockedTex);
        this.restrictiveTex = restrictiveTex;
        if (helper.getLayerCount() < 3) throw new IllegalStateException(
                "Cannot create a RestrictiveSQC without 3 or more layers present on the helper!");
    }

    public static @NotNull RestrictiveSQC create(PipeQuadHelper helper, SpriteInformation endTex,
                                                 SpriteInformation sideTex,
                                                 SpriteInformation blockedTex, SpriteInformation restrictiveTex) {
        helper.initialize(
                (facing, x1, y1, z1, x2, y2, z2) -> minLengthTube(facing, x1, y1, z1, x2, y2, z2,
                        OVERLAY_DIST_2, 4),
                (facing, x1, y1, z1, x2, y2, z2) -> minLengthTube(facing, x1, y1, z1, x2, y2, z2,
                        OVERLAY_DIST_1, 2));
        RestrictiveSQC sqc = new RestrictiveSQC(helper, endTex, sideTex, blockedTex, restrictiveTex);
        sqc.buildPrototype();
        return sqc;
    }

    @Override
    protected List<BakedQuad> buildPrototypeInternal() {
        List<BakedQuad> quads = super.buildPrototypeInternal();
        buildRestrictive(quads);
        return quads;
    }

    protected void buildRestrictive(List<BakedQuad> list) {
        helper.setTargetSprite(restrictiveTex);
        for (Direction facing : GTUtil.DIRECTIONS) {
            int start = list.size();
            list.addAll(helper.visitTube(facing, 2));
            restrictiveCoords.put(facing, new SubListAddress(start, list.size()));
        }
    }

    @Override
    public void addToList(List<BakedQuad> list, byte connectionMask, byte closedMask, byte blockedMask, ColorData data,
                          byte coverMask) {
        List<BakedQuad> quads = cache.getQuads(data);
        for (Direction facing : GTUtil.DIRECTIONS) {
            if (GTUtil.evalMask(facing, connectionMask)) {
                list.addAll(tubeCoords.get(facing).getSublist(quads));
                list.addAll(restrictiveCoords.get(facing).getSublist(quads));
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
