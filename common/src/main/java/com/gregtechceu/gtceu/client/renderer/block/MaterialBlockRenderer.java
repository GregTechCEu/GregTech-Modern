package com.gregtechceu.gtceu.client.renderer.block;

import com.google.common.collect.ImmutableList;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import com.gregtechceu.gtceu.utils.GradientUtil;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.block.Block;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * @author KilaBash
 * @date 2023/2/27
 * @implNote MaterialBlockRenderer
 */
public class MaterialBlockRenderer {
    public static final String LAYER_2_SUFFIX = "_layer2";
    private static final Set<MaterialBlockRenderer> MODELS = new HashSet<>();

    public static void create(Block block, MaterialIconType type, MaterialIconSet iconSet) {
        MODELS.add(new MaterialBlockRenderer(block, type, iconSet));
    }

    public static void reinitModels() {
        for (MaterialBlockRenderer model : MODELS) {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(model.block);
            ResourceLocation modelId = blockId.withPrefix("block/");
            GTDynamicResourcePack.addBlockModel(modelId, new DelegatedModel(model.type.getBlockModelPath(model.iconSet, true)));
            GTDynamicResourcePack.addBlockState(blockId, BlockModelGenerators.createSimpleBlock(model.block, modelId));
            //        ModelTemplates.CUBE_ALL.create(model.block,
            //                cubeTwoLayer(model.type.getBlockTexturePath(model.iconSet, true), model.type.getBlockTexturePath(model.iconSet, true).withSuffix(LAYER_2_SUFFIX)),
            //                GTDynamicResourcePack::addBlockModel)));
            GTDynamicResourcePack.addItemModel(BuiltInRegistries.ITEM.getKey(model.block.asItem()), new DelegatedModel(ModelLocationUtils.getModelLocation(model.block)));
        }
    }

    public static void initTextures() {
        for (MaterialBlockRenderer model : MODELS) {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(model.block);

            Resource file1 = Minecraft.getInstance().getResourceManager().getResource(GTDynamicResourcePack.getTextureLocation(null, model.type.getBlockTexturePath(model.iconSet, true)/*.withSuffix("_layer1")*/)).orElse(null);
            if (file1 == null) continue;
            try(InputStream stream1 = file1.open()) {
                if (!(model.block instanceof MaterialBlock materialBlock)) continue;
                int materialRGBA = GradientUtil.argbToRgba(materialBlock.material.getMaterialARGB());

                NativeImage image1 = NativeImage.read(stream1);
                try (NativeImage result = new NativeImage(image1.getWidth(), image1.getHeight(), true)) {
                    for (int x = 0; x < image1.getWidth(); ++x) {
                        for (int y = 0; y < image1.getHeight(); ++y) {
                            int color = image1.getPixelRGBA(x, y);
                            result.setPixelRGBA(x, y, GradientUtil.multiplyBlendRGBA(color, materialRGBA));
                        }
                    }
                    if (materialBlock.material.getMaterialSecondaryRGB() != -1) {
                        int materialSecondaryRGBA = GradientUtil.argbToRgba(materialBlock.material.getMaterialSecondaryARGB());
                        Resource file2 = Minecraft.getInstance().getResourceManager().getResource(GTDynamicResourcePack.getTextureLocation(null, model.type.getBlockTexturePath(model.iconSet, true).withSuffix(LAYER_2_SUFFIX))).orElse(null);
                        if (file2 != null) {
                            try(InputStream stream2 = file2.open()) {
                                NativeImage image2 = NativeImage.read(stream2);
                                for (int x = 0; x < image1.getWidth(); ++x) {
                                    for (int y = 0; y < image1.getHeight(); ++y) {
                                        int color = image2.getPixelRGBA(x, y);
                                        result.blendPixel(x, y, GradientUtil.multiplyBlendRGBA(color, materialSecondaryRGBA));
                                    }
                                }
                            }
                        }
                    }

                    GTDynamicResourcePack.addBlockTexture(blockId, result.asByteArray());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final Block block;
    private final MaterialIconType type;
    private final MaterialIconSet iconSet;

    protected MaterialBlockRenderer(Block block, MaterialIconType type, MaterialIconSet iconSet) {
        this.block = block;
        this.type = type;
        this.iconSet = iconSet;
    }
}
