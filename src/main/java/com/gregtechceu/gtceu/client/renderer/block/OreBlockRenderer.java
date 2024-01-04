package com.gregtechceu.gtceu.client.renderer.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.OreBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author KilaBash
 * @date 2023/2/27
 * @implNote OreBlockRenderer
 */
@MethodsReturnNonnullByDefault
public class OreBlockRenderer {
    private static final TextureSlot BASE_STONE = TextureSlot.create("stone");
    private static final ModelTemplate ORE_TEMPLATE = new ModelTemplate(Optional.of(GTCEu.id("block/ore")), Optional.empty(), BASE_STONE, TextureSlot.LAYER0, TextureSlot.LAYER1);
    private static final ModelTemplate ORE_EMISSIVE_TEMPLATE = new ModelTemplate(Optional.of(GTCEu.id("block/ore_emissive")), Optional.empty(), BASE_STONE, TextureSlot.LAYER0, TextureSlot.LAYER1);
    private static final TexturedModel.Provider ORE_PROVIDER = TexturedModel.createDefault((block) -> {
        if (block instanceof OreBlock oreBlock) {
            ResourceLocation stoneTexture = TagPrefix.ORES.get(oreBlock.tagPrefix).stoneTexture();
            ResourceLocation layer0 = oreBlock.tagPrefix.materialIconType().getBlockTexturePath(oreBlock.material.getMaterialIconSet(), true);
            ResourceLocation layer1 = oreBlock.tagPrefix.materialIconType().getBlockTexturePath(oreBlock.material.getMaterialIconSet(), "layer2", true);
            return new TextureMapping().put(BASE_STONE, stoneTexture).put(TextureSlot.LAYER0, layer0).put(TextureSlot.LAYER1, layer1);
        }
        return null;
    }, ORE_TEMPLATE);
    private static final TexturedModel.Provider ORE_EMISSIVE_PROVIDER = TexturedModel.createDefault((block) -> {
        if (block instanceof OreBlock oreBlock) {
            ResourceLocation stoneTexture = TagPrefix.ORES.get(oreBlock.tagPrefix).stoneTexture();
            ResourceLocation layer0 = oreBlock.tagPrefix.materialIconType().getBlockTexturePath(oreBlock.material.getMaterialIconSet(), true);
            ResourceLocation layer1 = oreBlock.tagPrefix.materialIconType().getBlockTexturePath(oreBlock.material.getMaterialIconSet(), "layer2", true);
            return new TextureMapping().put(BASE_STONE, stoneTexture).put(TextureSlot.LAYER0, layer0).put(TextureSlot.LAYER1, layer1);
        }
        return null;
    }, ORE_EMISSIVE_TEMPLATE);

    private static final Set<OreBlockRenderer> MODELS = new HashSet<>();

    private final Block block;
    private final boolean emissive;

    public static void create(Block block, ResourceLocation stoneTexture, MaterialIconType type, MaterialIconSet set, boolean emissive) {
        MODELS.add(new OreBlockRenderer(block, stoneTexture, type, set, emissive));
    }

    public OreBlockRenderer(Block block, ResourceLocation stoneTexture, MaterialIconType type, MaterialIconSet set, boolean emissive) {
        this.block = block;
        this.emissive = emissive;
    }

    public static void reinitModels() {
        for (OreBlockRenderer model : MODELS) {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(model.block);
            ResourceLocation modelId = blockId.withPrefix("block/");
            (model.emissive ? OreBlockRenderer.ORE_EMISSIVE_PROVIDER : OreBlockRenderer.ORE_PROVIDER).get(model.block).create(model.block, GTDynamicResourcePack::addBlockModel);
            //GTDynamicResourcePack.addBlockModel(modelId, new DelegatedModel(model.type.getBlockModelPath(model.iconSet, true)));
            GTDynamicResourcePack.addBlockState(blockId, BlockModelGenerators.createSimpleBlock(model.block, modelId));
            //        ModelTemplates.CUBE_ALL.create(model.block,
            //                cubeTwoLayer(model.type.getBlockTexturePath(model.iconSet, true), model.type.getBlockTexturePath(model.iconSet, true).withSuffix(LAYER_2_SUFFIX)),
            //                GTDynamicResourcePack::addBlockModel)));
            GTDynamicResourcePack.addItemModel(BuiltInRegistries.ITEM.getKey(model.block.asItem()), new DelegatedModel(ModelLocationUtils.getModelLocation(model.block)));
        }
    }

}
