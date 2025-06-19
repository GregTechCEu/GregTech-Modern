package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.client.util.ModelUtils;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.data.ModelData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SimpleCoverRenderer implements ICoverRenderer {

    ResourceLocation texture;
    ResourceLocation emissiveTexture;

    public SimpleCoverRenderer(ResourceLocation texture) {
        this.texture = texture;
        if (GTCEu.isClientSide()) {
            ModelUtils.registerAddModelsEventListener(this::loadModels);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void renderCover(List<BakedQuad> quads, Direction side, RandomSource rand,
                            @NotNull CoverBehavior coverBehavior, Direction elementSide, BlockPos pos,
                            BlockAndTintGetter level, ModelState modelState,
                            @NotNull ModelData modelData, @Nullable RenderType renderType) {
        if (elementSide == null) return;
        if (side == coverBehavior.attachedSide) {
            quads.add(StaticFaceBakery.bakeFace(elementSide, ModelFactory.getBlockSprite(texture), modelState));
            if (emissiveTexture != null) {
                quads.add(StaticFaceBakery.bakeFace(elementSide, ModelFactory.getBlockSprite(emissiveTexture),
                        modelState));
            }
        }
    }

    protected void loadModels(ModelEvent.RegisterAdditional event) {
        // is this required? I hope it isn't, because figuring all the simple renderers' models would be a pain.
    }
}
