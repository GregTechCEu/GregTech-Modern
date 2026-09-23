package com.gregtechceu.gtceu.client.model.runtimegen;

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

public class MaterialBlockModelGenerator {

    private static final Set<MaterialBlockModelGenerator> MODELS = new HashSet<>();

    public static void reinitModels() {
        for (MaterialBlockModelGenerator model : MODELS) {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(model.block);
            ResourceLocation modelId = model.type.getBlockModelPath(model.iconSet, true);

            GTDynamicResourcePack.addBlockState(blockId, BlockModelGenerators.createSimpleBlock(model.block, modelId));
            GTDynamicResourcePack.addItemModel(BuiltInRegistries.ITEM.getKey(model.block.asItem()),
                    new DelegatedModel(modelId));
        }
    }

    private final Block block;
    private final MaterialIconType type;
    private final MaterialIconSet iconSet;

    protected MaterialBlockModelGenerator(Block block, MaterialIconType type, MaterialIconSet iconSet) {
        this.block = block;
        this.type = type;
        this.iconSet = iconSet;
    }

    public static void add(Block block, MaterialIconType type, MaterialIconSet iconSet) {
        MODELS.add(new MaterialBlockModelGenerator(block, type, iconSet));
    }
}
