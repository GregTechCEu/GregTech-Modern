package com.gregtechceu.gtceu.client.renderer.item;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtlib.client.model.ModelFactory;
import com.gregtechceu.gtlib.client.renderer.impl.IModelRenderer;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/2/16
 * @implNote TagPrefixItemRenderer
 */
public class TagPrefixItemRenderer extends IModelRenderer {
    public static final Table<MaterialIconType, MaterialIconSet, TagPrefixItemRenderer> MODELS = Tables.newCustomTable(new HashMap<>(), HashMap::new);

    private ResourceLocation modelLocation;
    private MaterialIconType type;
    private MaterialIconSet iconSet;

    private TagPrefixItemRenderer(MaterialIconType type, MaterialIconSet iconSet) {
        super(type.getItemModelPath(iconSet));
        this.modelLocation = GTCEu.id(String.format("item/material_sets/%s/%s", iconSet.name, type.name()));
        this.type = type;
        this.iconSet = iconSet;
    }

    public void setModelLocation(ResourceLocation newModelLocation) {
        this.modelLocation = this.type.getItemModelPath(iconSet);
        this.itemModel = null;
    }

    public static TagPrefixItemRenderer getOrCreate(MaterialIconType type, MaterialIconSet iconSet) {
        if (!MODELS.contains(type, iconSet)) {
            MODELS.put(type, iconSet, new TagPrefixItemRenderer(type, iconSet));
        }
        return MODELS.get(type, iconSet);
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected UnbakedModel getModel() {
        return ModelFactory.getUnBakedModel(modelLocation);
    }

    @Environment(EnvType.CLIENT)
    @Nullable
    @Override
    protected BakedModel getItemBakedModel() {
        if (itemModel == null) {
            var model = getModel();
            if (model instanceof BlockModel blockModel && blockModel.getRootModel() == ModelBakery.GENERATION_MARKER) {
                // fabric doesn't help us to fix vanilla bakery, so we have to do it ourselves
                model = ModelFactory.ITEM_MODEL_GENERATOR.generateBlockModel(Material::sprite, blockModel);
            }
            itemModel = model.bake(
                    ModelFactory.getModeBakery(),
                    Material::sprite,
                    BlockModelRotation.X0_Y0,
                    modelLocation);
        }
        return itemModel;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public BakedModel getRotatedModel(Direction frontFacing) {
        return blockModels.computeIfAbsent(frontFacing, facing -> getModel().bake(
                ModelFactory.getModeBakery(),
                Material::sprite,
                ModelFactory.getRotation(facing),
                modelLocation));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onAdditionalModel(Consumer<ResourceLocation> registry) {
        registry.accept(modelLocation);
    }
}
