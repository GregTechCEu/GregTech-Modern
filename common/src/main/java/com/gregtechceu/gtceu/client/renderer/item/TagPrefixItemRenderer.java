package com.gregtechceu.gtceu.client.renderer.item;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import com.gregtechceu.gtceu.utils.GradientUtil;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.Item;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static com.gregtechceu.gtceu.client.renderer.block.MaterialBlockRenderer.LAYER_2_SUFFIX;

/**
 * @author KilaBash
 * @date 2023/2/16
 * @implNote TagPrefixItemRenderer
 */
public class TagPrefixItemRenderer {
    private static final Set<TagPrefixItemRenderer> MODELS = new HashSet<>();

    public static void create(Item item, MaterialIconType type, MaterialIconSet iconSet) {
        MODELS.add(new TagPrefixItemRenderer(item, type, iconSet));
    }

    public static void reinitModels() {
        for (TagPrefixItemRenderer model : MODELS) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(model.item);
            GTDynamicResourcePack.addItemModel(itemId, new DelegatedModel(model.type.getItemModelPath(model.iconSet, true)));
            //ModelTemplates.FLAT_ITEM.create(GTDynamicResourcePack.getItemModelLocation(itemId), TextureMapping.layer0(itemId.withPrefix("item/")), GTDynamicResourcePack::addItemModel);
        }
    }

    public static void initTextures() {
        for (TagPrefixItemRenderer model : MODELS) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(model.item);

            Resource file1 = Minecraft.getInstance().getResourceManager().getResource(GTDynamicResourcePack.getTextureLocation(null, model.type.getItemTexturePath(model.iconSet, true)/*.withSuffix("_layer1")*/)).orElse(null);
            if (file1 == null) continue;
            try(InputStream stream1 = file1.open()) {
                if (!(model.item instanceof TagPrefixItem prefixItem)) continue;
                int materialRGBA = GradientUtil.argbToRgba(prefixItem.material.getMaterialARGB());
                int materialSecondaryRGBA = GradientUtil.argbToRgba(prefixItem.material.getMaterialSecondaryARGB());

                NativeImage image1 = NativeImage.read(stream1);
                try (NativeImage result = new NativeImage(image1.getWidth(), image1.getHeight(), true)) {
                    for (int x = 0; x < image1.getWidth(); ++x) {
                        for (int y = 0; y < image1.getHeight(); ++y) {
                            int color = image1.getPixelRGBA(x, y);
                            result.setPixelRGBA(x, y, GradientUtil.multiplyBlendRGBA(color, materialRGBA));
                        }
                    }
                    if (prefixItem.material.getMaterialSecondaryRGB() != -1) {
                        Resource file2 = Minecraft.getInstance().getResourceManager().getResource(GTDynamicResourcePack.getTextureLocation(null, model.type.getItemTexturePath(model.iconSet, true).withSuffix(LAYER_2_SUFFIX))).orElse(null);
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

                    GTDynamicResourcePack.addItemTexture(itemId, result.asByteArray());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final Item item;
    private final MaterialIconType type;
    private final MaterialIconSet iconSet;

    private TagPrefixItemRenderer(Item item, MaterialIconType type, MaterialIconSet iconSet) {
        this.item = item;
        this.type = type;
        this.iconSet = iconSet;
    }
}
