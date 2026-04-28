package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.client.util.ModelUtils;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.model.data.ModelData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SimpleCoverRenderer implements ICoverRenderer {

    @OnlyIn(Dist.CLIENT)
    protected TextureAtlasSprite sprite = null;
    @OnlyIn(Dist.CLIENT)
    protected TextureAtlasSprite emissiveSprite = null;

    public SimpleCoverRenderer(Identifier texture) {
        this(texture, null);
    }

    public SimpleCoverRenderer(Identifier texture, Identifier emissiveTexture) {
        ModelUtils.registerAtlasStitchedEventListener(false, TextureAtlas.LOCATION_BLOCKS, event -> {
            var atlas = event.getAtlas();

            sprite = atlas.getSprite(texture);
            if (emissiveTexture != null) {
                emissiveSprite = atlas.getSprite(emissiveTexture);
            } else {
                Identifier emissiveTex = texture.withSuffix("_emissive");
                if (atlas.getTextures().containsKey(emissiveTex)) {
                    emissiveSprite = atlas.getSprite(emissiveTex);
                }
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void renderCover(List<BakedQuad> quads, Direction side, RandomSource rand,
                            @NotNull CoverBehavior coverBehavior, BlockPos pos, BlockAndTintGetter level,
                            @NotNull ModelData modelData, @Nullable RenderType renderType) {
        if (side == null || side == coverBehavior.attachedSide) {
            quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.COVER_OVERLAY, coverBehavior.attachedSide, sprite));
            if (emissiveSprite != null) {
                quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.COVER_OVERLAY, coverBehavior.attachedSide,
                        emissiveSprite));
            }
        }
    }
}
