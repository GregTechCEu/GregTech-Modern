package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.IIOCover;
import com.gregtechceu.gtceu.client.util.ModelUtils;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
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

    public IOCoverRenderer(@Nullable ResourceLocation overlay,
                           @Nullable ResourceLocation invertedOverlay,
                           @Nullable ResourceLocation emissiveOverlay,
                           @Nullable ResourceLocation invertedEmissiveOverlay) {
        ModelUtils.registerAtlasStitchedEventListener(false, InventoryMenu.BLOCK_ATLAS, event -> {
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
                        invertedEmissiveOverlaySprite, BlockModelRotation.X0_Y0, -101, 15, true, false));
            } else if (emissiveOverlaySprite != null) {
                quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.COVER_OVERLAY, coverBehavior.attachedSide,
                        emissiveOverlaySprite, BlockModelRotation.X0_Y0, -101, 15, true, false));
            }
        }
    }
}
