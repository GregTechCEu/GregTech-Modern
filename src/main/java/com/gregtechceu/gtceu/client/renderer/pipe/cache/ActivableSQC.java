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
public class ActivableSQC extends StructureQuadCache {

    protected final EnumMap<Direction, SubListAddress> overlayCoords = new EnumMap<>(Direction.class);
    protected final EnumMap<Direction, SubListAddress> overlayActiveCoords = new EnumMap<>(Direction.class);

    protected final SpriteInformation overlayTex;
    protected final SpriteInformation overlayActiveTex;

    protected ActivableSQC(PipeQuadHelper helper, SpriteInformation endTex, SpriteInformation sideTex,
                           SpriteInformation overlayTex, SpriteInformation overlayActiveTex) {
        super(helper, endTex, sideTex);
        this.overlayTex = overlayTex;
        this.overlayActiveTex = overlayActiveTex;
        if (helper.getLayerCount() < 2) throw new IllegalStateException(
                "Cannot create an ActivableSQC without 2 or more layers present on the helper!");
    }

    public static @NotNull ActivableSQC create(PipeQuadHelper helper, SpriteInformation endTex,
                                               SpriteInformation sideTex, SpriteInformation overlayTex,
                                               SpriteInformation overlayActiveTex) {
        helper.initialize((facing, x1, y1, z1, x2, y2, z2) -> QuadHelper.tubeOverlay(facing, x1, y1, z1, x2, y2, z2,
                OVERLAY_DIST_1));
        ActivableSQC cache = new ActivableSQC(helper, endTex, sideTex, overlayTex, overlayActiveTex);
        cache.buildPrototype();
        return cache;
    }

    @Override
    protected List<BakedQuad> buildPrototypeInternal() {
        List<BakedQuad> quads = super.buildPrototypeInternal();
        buildOverlay(quads);
        buildOverlayActive(quads);
        return quads;
    }

    protected void buildOverlay(List<BakedQuad> list) {
        helper.setTargetSprite(overlayTex);
        for (Direction facing : GTUtil.DIRECTIONS) {
            int start = list.size();
            list.addAll(helper.visitTube(facing, 1));
            overlayCoords.put(facing, new SubListAddress(start, list.size()));
        }
    }

    protected void buildOverlayActive(List<BakedQuad> list) {
        helper.setTargetSprite(overlayActiveTex);
        for (Direction facing : GTUtil.DIRECTIONS) {
            int start = list.size();
            list.addAll(helper.visitTube(facing, 1));
            overlayActiveCoords.put(facing, new SubListAddress(start, list.size()));
        }
    }

    public void addOverlay(List<BakedQuad> list, byte overlayMask, ColorData data, boolean active) {
        List<BakedQuad> quads = cache.getQuads(data);
        for (Direction facing : GTUtil.DIRECTIONS) {
            if (GTUtil.evalMask(facing, overlayMask)) {
                if (active) {
                    list.addAll(overlayActiveCoords.get(facing).getSublist(quads));
                } else {
                    list.addAll(overlayCoords.get(facing).getSublist(quads));
                }
            }
        }
    }
}
