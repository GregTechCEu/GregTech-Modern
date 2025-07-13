package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.client.util.ModelUtils;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SimpleCoverRenderer implements ICoverRenderer {

    @OnlyIn(Dist.CLIENT)
    protected TextureAtlasSprite sprite = null;
    @OnlyIn(Dist.CLIENT)
    protected TextureAtlasSprite emissiveSprite = null;

    public SimpleCoverRenderer(ResourceLocation texture) {
        this(texture, null);
    }

    public SimpleCoverRenderer(ResourceLocation texture, ResourceLocation emissiveTexture) {
        ModelUtils.registerAtlasStitchedEventListener(false, InventoryMenu.BLOCK_ATLAS, event -> {
            var atlas = event.getAtlas();

            sprite = atlas.getSprite(texture);
            if (emissiveTexture != null) {
                emissiveSprite = atlas.getSprite(emissiveTexture);
            } else {
                ResourceLocation emissiveTex = texture.withSuffix("_emissive");
                if (atlas.getTextureLocations().contains(emissiveTex)) {
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
