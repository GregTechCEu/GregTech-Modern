package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.utils.ResourceHelper;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/2/24
 * @implNote SimpleCoverRenderer
 */
public class SimpleCoverRenderer implements ICoverRenderer {

    ResourceLocation texture;
    ResourceLocation emissiveTexture;

    public SimpleCoverRenderer(ResourceLocation texture) {
        this.texture = texture;
        if (GTCEu.isClientSide()) {
            registerEvent();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            register.accept(texture);
            emissiveTexture = new ResourceLocation(texture.getNamespace(), texture.getPath() + "_emissive");
            if (ResourceHelper.isTextureExist(emissiveTexture)) register.accept(emissiveTexture);
            else emissiveTexture = null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void renderCover(List<BakedQuad> quads, Direction side, RandomSource rand,
                            @NotNull CoverBehavior coverBehavior, Direction modelFacing, BlockPos pos,
                            BlockAndTintGetter level, ModelState modelState) {
        if (side == coverBehavior.attachedSide && modelFacing != null) {
            quads.add(StaticFaceBakery.bakeFace(modelFacing, ModelFactory.getBlockSprite(texture), modelState));
            if (emissiveTexture != null) {
                quads.add(StaticFaceBakery.bakeFace(modelFacing, ModelFactory.getBlockSprite(emissiveTexture),
                        modelState));
            }
        }
    }
}
