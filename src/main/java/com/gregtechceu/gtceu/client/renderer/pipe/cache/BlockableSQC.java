package com.gregtechceu.gtceu.client.renderer.pipe.cache;

import com.gregtechceu.gtceu.client.renderer.pipe.quad.PipeQuadHelper;
import com.gregtechceu.gtceu.client.renderer.pipe.quad.QuadHelper;
import com.gregtechceu.gtceu.client.renderer.pipe.quad.RecolorableBakedQuad;
import com.gregtechceu.gtceu.client.renderer.pipe.util.ColorData;
import com.gregtechceu.gtceu.client.renderer.pipe.util.SpriteInformation;
import com.gregtechceu.gtceu.utils.GTUtil;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

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
        helper.initialize((facing, x1, y1, z1, x2, y2, z2) -> QuadHelper.tubeOverlay(facing, x1, y1, z1, x2, y2, z2,
                OVERLAY_DIST_1));
        BlockableSQC cache = new BlockableSQC(helper, endTex, sideTex, blockedTex);
        cache.buildPrototype();
        return cache;
    }

    @Override
    protected List<RecolorableBakedQuad> buildPrototypeInternal() {
        List<RecolorableBakedQuad> quads = super.buildPrototypeInternal();
        buildBlocked(quads);
        return quads;
    }

    protected void buildBlocked(List<RecolorableBakedQuad> list) {
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
