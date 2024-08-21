package com.gregtechceu.gtceu.client.renderer.pipe.cache;

import com.gregtechceu.gtceu.client.renderer.pipe.quad.RecolorableBakedQuad;
import com.gregtechceu.gtceu.client.renderer.pipe.util.ColorData;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class ColorQuadCache {

    // TODO dynamic cache growth & collapse
    private static final int CACHE_LIMIT = 20;

    private final List<RecolorableBakedQuad> prototypes;

    private final Object2ObjectLinkedOpenHashMap<ColorData, List<BakedQuad>> cache;

    public ColorQuadCache(List<RecolorableBakedQuad> prototypes) {
        this.prototypes = prototypes;
        this.cache = new Object2ObjectLinkedOpenHashMap<>();
    }

    public List<BakedQuad> getQuads(ColorData data) {
        List<BakedQuad> existing = cache.getAndMoveToFirst(data);
        if (existing == null) {
            existing = new ObjectArrayList<>();
            for (RecolorableBakedQuad quad : prototypes) {
                existing.add(quad.withColor(data));
            }
            cache.put(data, existing);
            if (cache.size() > CACHE_LIMIT) cache.removeLast();
        }
        return existing;
    }
}
