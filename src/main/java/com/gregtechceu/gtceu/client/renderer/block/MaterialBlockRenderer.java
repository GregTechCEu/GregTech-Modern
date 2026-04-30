package com.gregtechceu.gtceu.client.renderer.block;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.model.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Set;

public class MaterialBlockRenderer {

    private static final Set<MaterialBlockRenderer> MODELS = new HashSet<>();

    public static void create(Block block, MaterialIconType type, MaterialIconSet iconSet) {
        MODELS.add(new MaterialBlockRenderer(block, type, iconSet));
    }

    public static void reinitModels() {
        for (MaterialBlockRenderer model : MODELS) {
            Identifier blockId = BuiltInRegistries.BLOCK.getKey(model.block);
            Identifier modelId = model.type.getBlockModelPath(model.iconSet, true);

            GTDynamicResourcePack.addBlockState(
                    BlockModelGenerators.createSimpleBlock(model.block, BlockModelGenerators.plainVariant(modelId)));
            // Material block models (block, frame_gt, raw_ore_block, ...) use tintindex 0 (primary)
            // and tintindex 1 (secondary) on faces. Emit matching gtceu:item_color tints so the
            // runtime GTItemColors handler is invoked when the block's item form is rendered.
            GTDynamicResourcePack.addTintedItemModel(BuiltInRegistries.ITEM.getKey(model.block.asItem()),
                    new DelegatedModel(modelId).get(), 2);
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
