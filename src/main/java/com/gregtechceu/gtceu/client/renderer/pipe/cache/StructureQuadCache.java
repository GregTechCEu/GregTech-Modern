package com.gregtechceu.gtceu.client.renderer.pipe.cache;

import com.gregtechceu.gtceu.client.renderer.pipe.quad.PipeQuadHelper;
import com.gregtechceu.gtceu.client.renderer.pipe.util.ColorData;
import com.gregtechceu.gtceu.client.renderer.pipe.util.SpriteInformation;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class StructureQuadCache {

    public static final float OVERLAY_DIST_1 = 0.003f;
    public static final float OVERLAY_DIST_2 = 0.006f;

    protected final PipeQuadHelper helper;

    protected ColorQuadCache cache;

    protected final EnumMap<Direction, SubListAddress> tubeCoords = new EnumMap<>(Direction.class);

    protected final EnumMap<Direction, SubListAddress> coreCoords = new EnumMap<>(Direction.class);
    protected final EnumMap<Direction, SubListAddress> capperCoords = new EnumMap<>(Direction.class);
    protected final EnumMap<Direction, SubListAddress> capperClosedCoords = new EnumMap<>(Direction.class);

    protected final SpriteInformation endTex;
    protected final SpriteInformation sideTex;

    protected StructureQuadCache(PipeQuadHelper helper, SpriteInformation endTex, SpriteInformation sideTex) {
        this.helper = helper;
        this.endTex = endTex;
        this.sideTex = sideTex;
        if (helper.getLayerCount() < 1)
            throw new IllegalStateException("Cannot create an SQC without at least one layer present on the helper!");
    }

    public static @NotNull StructureQuadCache create(PipeQuadHelper helper, SpriteInformation endTex,
                                                     SpriteInformation sideTex) {
        StructureQuadCache cache = new StructureQuadCache(helper.initialize(), endTex, sideTex);
        cache.buildPrototype();
        return cache;
    }

    protected void buildPrototype() {
        this.cache = new ColorQuadCache(this.buildPrototypeInternal());
    }

    protected List<BakedQuad> buildPrototypeInternal() {
        List<BakedQuad> quads = new ObjectArrayList<>();
        buildTube(quads);
        buildCore(quads);
        buildCapper(quads);
        buildCapperClosed(quads);
        return quads;
    }

    protected void buildTube(List<BakedQuad> list) {
        helper.setTargetSprite(sideTex);
        for (Direction facing : GTUtil.DIRECTIONS) {
            int start = list.size();
            list.addAll(helper.visitTube(facing));
            tubeCoords.put(facing, new SubListAddress(start, list.size()));
        }
    }

    protected void buildCore(List<BakedQuad> list) {
        helper.setTargetSprite(sideTex);
        for (Direction facing : GTUtil.DIRECTIONS) {
            int start = list.size();
            list.add(helper.visitCore(facing));
            coreCoords.put(facing, new SubListAddress(start, start + 1));
        }
    }

    protected void buildCapper(List<BakedQuad> list) {
        helper.setTargetSprite(endTex);
        for (Direction facing : GTUtil.DIRECTIONS) {
            int start = list.size();
            list.add(helper.visitCapper(facing));
            capperCoords.put(facing, new SubListAddress(start, start + 1));
        }
    }

    protected void buildCapperClosed(List<BakedQuad> list) {
        helper.setTargetSprite(sideTex);
        for (Direction facing : GTUtil.DIRECTIONS) {
            int start = list.size();
            list.add(helper.visitCapper(facing));
            capperClosedCoords.put(facing, new SubListAddress(start, start + 1));
        }
    }

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
            } else {
                list.addAll(coreCoords.get(facing).getSublist(quads));
            }
        }
    }
}
