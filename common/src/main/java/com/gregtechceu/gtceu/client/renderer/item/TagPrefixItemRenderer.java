package com.gregtechceu.gtceu.client.renderer.item;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
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
import java.util.HashMap;

/**
 * @author KilaBash
 * @date 2023/2/16
 * @implNote TagPrefixItemRenderer
 */
public class TagPrefixItemRenderer {
    private static final Table<MaterialIconType, MaterialIconSet, TagPrefixItemRenderer> MODELS = Tables.newCustomTable(new HashMap<>(), HashMap::new);

    public static void reinitModels() {
        for (TagPrefixItemRenderer model : MODELS.values()) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(model.item);
            GTDynamicResourcePack.addItemModel(itemId, new DelegatedModel(model.type.getItemModelPath(model.iconSet, true)));

            try {
                if (!(model.item instanceof TagPrefixItem materialBlock)) continue;
                ResourceLocation itemTexturePath = GTDynamicResourcePack.getTextureLocation(null, model.type.getItemTexturePath(model.iconSet, true));
                Resource file = Minecraft.getInstance().getResourceManager().getResource(itemTexturePath).orElse(null);
                if (file == null) continue;

                NativeImage image = NativeImage.read(file.open());
                for (int x = 0; x < image.getWidth(); ++x) {
                    for (int y = 0; y < image.getHeight(); ++y) {
                        image.blendPixel(x, y, GradientUtil.argbToABGR(materialBlock.material.getMaterialARGB()));
                    }
                }
                GTDynamicResourcePack.addItemTexture(itemId, image.asByteArray());
            } catch (
                    IOException e) {
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

    public static TagPrefixItemRenderer getOrCreate(Item item, MaterialIconType type, MaterialIconSet iconSet) {
        if (!MODELS.contains(type, iconSet)) {
            MODELS.put(type, iconSet, new TagPrefixItemRenderer(item, type, iconSet));
        }
        return MODELS.get(type, iconSet);
    }
}
