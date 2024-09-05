package com.gregtechceu.gtceu.client.renderer.pipe.quad;

import com.gregtechceu.gtceu.client.renderer.pipe.util.ColorData;
import com.gregtechceu.gtceu.client.renderer.pipe.util.SpriteInformation;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import static net.minecraftforge.client.model.IQuadTransformer.*;

@MethodsReturnNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class RecolorableBakedQuad extends BakedQuad {

    private final Int2ObjectOpenHashMap<RecolorableBakedQuad> cache;
    private final SpriteInformation spriteInformation;

    /**
     * Create a new recolorable quad based off of a baked quad prototype.
     * 
     * @param prototype         the prototype.
     * @param spriteInformation the sprite information of this baked quad.
     */
    public RecolorableBakedQuad(BakedQuad prototype, SpriteInformation spriteInformation) {
        this(prototype, prototype.getTintIndex(), spriteInformation, new Int2ObjectOpenHashMap<>());
    }

    protected RecolorableBakedQuad(BakedQuad prototype, int tintIndex,
                                   SpriteInformation spriteInformation,
                                   Int2ObjectOpenHashMap<RecolorableBakedQuad> cache) {
        super(prototype.getVertices(), tintIndex, prototype.getDirection(), spriteInformation.sprite(),
                prototype.isShade(), prototype.hasAmbientOcclusion());
        this.spriteInformation = spriteInformation;
        this.cache = cache;
    }

    /**
     * Get a recolorable quad based off of this quad but aligned with the given color data.
     * 
     * @param data the color data.
     * @return a quad colored based on the color data.
     */
    public RecolorableBakedQuad withColor(ColorData data) {
        if (!spriteInformation.colorable()) return this;
        int argb = data.colorsARGB()[spriteInformation.colorID()];
        return cache.computeIfAbsent(argb,
                (c) -> new RecolorableBakedQuad(this, c, this.spriteInformation, this.cache));
    }
}
