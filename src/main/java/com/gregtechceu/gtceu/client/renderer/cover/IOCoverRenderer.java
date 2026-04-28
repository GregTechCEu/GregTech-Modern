package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.IIOCover;
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

public class IOCoverRenderer implements ICoverRenderer {

    public static final IOCoverRenderer PUMP_LIKE_COVER_RENDERER = new IOCoverRenderer(
            GTCEu.id("block/cover/pump"),
            GTCEu.id("block/cover/pump_inverted"),
            null, null);

    @OnlyIn(Dist.CLIENT)
    protected TextureAtlasSprite overlaySprite = null;
    @OnlyIn(Dist.CLIENT)
    protected TextureAtlasSprite invertedOverlaySprite = null;
    @OnlyIn(Dist.CLIENT)
    protected TextureAtlasSprite emissiveOverlaySprite = null;
    @OnlyIn(Dist.CLIENT)
    protected TextureAtlasSprite invertedEmissiveOverlaySprite = null;

    public IOCoverRenderer(@Nullable Identifier overlay,
                           @Nullable Identifier invertedOverlay,
                           @Nullable Identifier emissiveOverlay,
                           @Nullable Identifier invertedEmissiveOverlay) {
        ModelUtils.registerAtlasStitchedEventListener(false, TextureAtlas.LOCATION_BLOCKS, event -> {
            var atlas = event.getAtlas();

            if (overlay != null) {
                overlaySprite = atlas.getSprite(overlay);
            }
            if (invertedOverlay != null) {
                invertedOverlaySprite = atlas.getSprite(invertedOverlay);
            }
            if (emissiveOverlay != null) {
                emissiveOverlaySprite = atlas.getSprite(emissiveOverlay);
            }
            if (invertedEmissiveOverlay != null) {
                invertedEmissiveOverlaySprite = atlas.getSprite(invertedEmissiveOverlay);
            }
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderCover(List<BakedQuad> quads, @Nullable Direction side, RandomSource rand,
                            @NotNull CoverBehavior coverBehavior, BlockPos pos, BlockAndTintGetter level,
                            @NotNull ModelData modelData, @Nullable RenderType renderType) {
        if ((side == null || side == coverBehavior.attachedSide) && coverBehavior instanceof IIOCover ioCover) {
            boolean isInverted = ioCover.getIo() != IO.OUT;

            if (isInverted && invertedOverlaySprite != null) {
                quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.COVER_OVERLAY, coverBehavior.attachedSide,
                        invertedOverlaySprite));
            } else if (overlaySprite != null) {
                quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.COVER_OVERLAY, coverBehavior.attachedSide,
                        overlaySprite));
            }
            if (isInverted && invertedEmissiveOverlaySprite != null) {
                quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.COVER_OVERLAY, coverBehavior.attachedSide,
                        invertedEmissiveOverlaySprite, -101, 15, false));
            } else if (emissiveOverlaySprite != null) {
                quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.COVER_OVERLAY, coverBehavior.attachedSide,
                        emissiveOverlaySprite, -101, 15, false));
            }
        }
    }
}
