package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.utils.ResourceHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
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
        if (LDLib.isClient()) {
            registerEvent();
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            register.accept(texture);
            emissiveTexture = new ResourceLocation(texture.getNamespace(), texture.getPath() + "_emissive");
            if (ResourceHelper.isTextureExist(emissiveTexture)) register.accept(emissiveTexture); else emissiveTexture = null;
        }
    }

    @Environment(EnvType.CLIENT)
    public void renderCover(List<BakedQuad> quads, Direction side, RandomSource rand, @NotNull CoverBehavior coverBehavior, Direction modelFacing, ModelState modelState) {
        if (side == coverBehavior.attachedSide && modelFacing != null) {
            quads.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(texture), modelState));
            if (emissiveTexture != null) {
                quads.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(emissiveTexture), modelState));
            }
        }
    }

}
