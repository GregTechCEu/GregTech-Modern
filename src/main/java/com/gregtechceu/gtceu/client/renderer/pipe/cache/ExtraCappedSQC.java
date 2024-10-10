package com.gregtechceu.gtceu.client.renderer.pipe.cache;

import com.gregtechceu.gtceu.client.renderer.pipe.quad.PipeQuadHelper;
import com.gregtechceu.gtceu.client.renderer.pipe.quad.QuadHelper;
import com.gregtechceu.gtceu.client.renderer.pipe.util.ColorData;
import com.gregtechceu.gtceu.client.renderer.pipe.util.SpriteInformation;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ExtraCappedSQC extends StructureQuadCache {

    protected final EnumMap<Direction, SubListAddress> extraCapperCoords = new EnumMap<>(Direction.class);

    protected final SpriteInformation extraEndTex;

    protected ExtraCappedSQC(PipeQuadHelper helper, SpriteInformation endTex, SpriteInformation sideTex,
                             SpriteInformation extraEndTex) {
        super(helper, endTex, sideTex);
        this.extraEndTex = extraEndTex;
        if (helper.getLayerCount() < 2) throw new IllegalStateException(
                "Cannot create an ExtraCappedSQC without 2 or more layers present on the helper!");
    }

    public static @NotNull ExtraCappedSQC create(PipeQuadHelper helper, SpriteInformation endTex,
                                                 SpriteInformation sideTex, SpriteInformation extraEndTex) {
        helper.initialize((facing, x1, y1, z1, x2, y2, z2) -> QuadHelper.capOverlay(facing, x1, y1, z1, x2, y2, z2,
                OVERLAY_DIST_1));
        ExtraCappedSQC cache = new ExtraCappedSQC(helper, endTex, sideTex, extraEndTex);
        cache.buildPrototype();
        return cache;
    }

    @Override
    protected List<BakedQuad> buildPrototypeInternal() {
        List<BakedQuad> quads = super.buildPrototypeInternal();
        buildExtraCapper(quads);
        return quads;
    }

    protected void buildExtraCapper(List<BakedQuad> list) {
        helper.setTargetSprite(extraEndTex);
        for (Direction facing : GTUtil.DIRECTIONS) {
            int start = list.size();
            list.add(helper.visitCapper(facing, 1));
            extraCapperCoords.put(facing, new SubListAddress(start, list.size()));
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
                        list.addAll(extraCapperCoords.get(facing).getSublist(quads));
                    }
                }
            } else {
                list.addAll(coreCoords.get(facing).getSublist(quads));
            }
        }
    }
}
