package com.gregtechceu.gtceu.client.renderer.pipe.cache;

import com.gregtechceu.gtceu.client.renderer.pipe.util.ColorData;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class ColorQuadCache {

    private final List<BakedQuad> prototypes;

    private final Object2ObjectLinkedOpenHashMap<ColorData, List<BakedQuad>> cache;

    public ColorQuadCache(List<BakedQuad> prototypes) {
        this.prototypes = prototypes;
        this.cache = new Object2ObjectLinkedOpenHashMap<>();
    }

    public List<BakedQuad> getQuads(ColorData data) {
        // List<BakedQuad> existing = cache.get(data);
        // if (existing == null) {
        // existing = new ObjectArrayList<>();
        // for (BakedQuad quad : prototypes) {
        // existing.add(quad);
        // }
        // cache.put(data, existing);
        // // if (cache.size() > 20) cache.removeLast();
        // }
        return prototypes;
    }
}
