package com.gregtechceu.gtceu.client.renderer.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;
import com.gregtechceu.gtceu.utils.memoization.function.MemoizedBiFunction;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.ApiStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@MethodsReturnNonnullByDefault
public class OreBlockRenderer {

    protected static final Set<OreBlockRenderer> MODELS = new HashSet<>();

    protected static final JsonObject NULL_ELEMENT_MARKER = new JsonObject();
    protected static final MemoizedBiFunction<MaterialIconType, MaterialIconSet, JsonObject> TEMPLATE_MODEL_CACHE = GTMemoizer
            .memoizeFunctionWeakIdent(OreBlockRenderer::loadTemplateOreModel);

    // First format key is material set name, 2nd is stone type prefix's name, 3rd is icon type's name
    public static final String ORE_MODEL_NAME_FORMAT = "block/material_sets/%s/ores/%s/%s";

    protected final MaterialBlock block;

    public static void create(MaterialBlock block) {
        MODELS.add(new OreBlockRenderer(block));
    }

    public OreBlockRenderer(MaterialBlock block) {
        this.block = block;
    }

    @ApiStatus.Internal
    public static void reinitModels() {
        // first set up all the stone types for all tag prefixes
        for (MaterialIconSet iconSet : MaterialIconSet.ICON_SETS.values()) {
            for (var entry : TagPrefix.ORES.entrySet()) {
                copyOreModelWithBaseStone(entry.getKey(), entry.getValue(), MaterialIconType.ore, iconSet);
                copyOreModelWithBaseStone(entry.getKey(), entry.getValue(), MaterialIconType.oreEmissive, iconSet);
                // TODO uncomment if/when small ores are added
                // copyOreModelWithBaseStone(entry.getKey(), entry.getValue(), MaterialIconType.oreSmall, iconSet);
            }
        }

        // then create block state JSONs for all ore blocks with those models
        for (OreBlockRenderer model : MODELS) {
            Material material = model.block.material;
            TagPrefix tagPrefix = model.block.tagPrefix;
            MaterialIconSet iconSet = material.getMaterialIconSet();
            MaterialIconType iconType = tagPrefix.getMaterialIconType(material);

            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(model.block);
            ResourceLocation modelId = GTCEu.id(ORE_MODEL_NAME_FORMAT
                    .formatted(iconSet.name, tagPrefix.name, iconType.name()));

            GTDynamicResourcePack.addBlockState(blockId, BlockModelGenerators.createSimpleBlock(model.block, modelId));
            GTDynamicResourcePack.addItemModel(BuiltInRegistries.ITEM.getKey(model.block.asItem()),
                    new DelegatedModel(modelId));
        }

        TEMPLATE_MODEL_CACHE.getCache().clear();
    }

    /// This is called for every combination of tag prefix + icon type + icon set
    protected static void copyOreModelWithBaseStone(TagPrefix tagPrefix, TagPrefix.OreType oreType,
                                                    MaterialIconType iconType, MaterialIconSet iconSet) {
        // read the base ore model JSON
        JsonObject original;
        try {
            original = TEMPLATE_MODEL_CACHE.apply(iconType, iconSet);
        } catch (RuntimeException e) {
            GTCEu.LOGGER.error("Could not load template block model for ore type {}, icon type '{}', icon set '{}'",
                    tagPrefix.name, iconType.name(), iconSet.name, e);
            return;
        }
        if (original == NULL_ELEMENT_MARKER) {
            // if the icon set doesn't have an ore model (somehow...), skip it
            return;
        }

        // copy it
        JsonObject newJson = original.deepCopy();
        // add the base stone model.
        newJson.getAsJsonObject("children")
                .getAsJsonObject("base_stone")
                .addProperty("parent", oreType.baseModelLocation().toString());

        GTDynamicResourcePack.addBlockModel(
                GTCEu.id(ORE_MODEL_NAME_FORMAT.formatted(iconSet.name, tagPrefix.name, iconType.name())), newJson);
    }

    private static JsonObject loadTemplateOreModel(MaterialIconType iconType, MaterialIconSet iconSet) {
        ResourceLocation baseModelPath = iconType.getBlockModelPath(iconSet, true);
        baseModelPath = GTDynamicResourcePack.MODEL_ID_CONVERTER.idToFile(baseModelPath);

        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        Optional<Resource> modelResource = resourceManager.getResource(baseModelPath);

        if (modelResource.isEmpty()) {
            // if the icon set doesn't have an ore model (somehow...), skip it gracefully
            return NULL_ELEMENT_MARKER;
        }
        // read & cache the base ore model JSON
        try (BufferedReader reader = modelResource.get().openAsReader()) {
            return GsonHelper.parse(reader, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
