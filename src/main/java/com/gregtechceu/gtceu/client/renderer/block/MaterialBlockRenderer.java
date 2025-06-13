package com.gregtechceu.gtceu.client.renderer.block;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Set;

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
            GTDynamicResourcePack.addBlockModel(blockId,
                    new DelegatedModel(model.type.getBlockModelPath(model.iconSet, true)));
            GTDynamicResourcePack.addBlockState(blockId, BlockModelGenerators.createSimpleBlock(model.block, modelId));
            GTDynamicResourcePack.addItemModel(BuiltInRegistries.ITEM.getKey(model.block.asItem()),
                    new DelegatedModel(ModelLocationUtils.getModelLocation(model.block)));
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
