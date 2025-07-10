package com.gregtechceu.gtceu.client.renderer.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@MethodsReturnNonnullByDefault
public class OreBlockRenderer {

    private static final Set<OreBlockRenderer> MODELS = new HashSet<>();

    private final MaterialBlock block;

    public static void create(MaterialBlock block) {
        MODELS.add(new OreBlockRenderer(block));
    }

    public OreBlockRenderer(MaterialBlock block) {
        this.block = block;
    }

    public static void reinitModels() {
        for (OreBlockRenderer model : MODELS) {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(model.block);
            ResourceLocation modelId = blockId.withPrefix("block/");
            OreBlockRenderer.cloneBlockModel(blockId, model.block.tagPrefix, model.block.material);
            GTDynamicResourcePack.addBlockState(blockId, BlockModelGenerators.createSimpleBlock(model.block, modelId));
            GTDynamicResourcePack.addItemModel(BuiltInRegistries.ITEM.getKey(model.block.asItem()),
                    new DelegatedModel(ModelLocationUtils.getModelLocation(model.block)));
        }
    }

    /**
     * Clones & modifies the base JSON for a single ore block.
     * 
     * @param modelId  the model id (usually {@code gtceu:block/<block id path>})
     * @param prefix   the TagPrefix of the block being added.
     * @param material the material of the block being added. must have an ore property.
     */
    public static void cloneBlockModel(ResourceLocation modelId, TagPrefix prefix, Material material) {
        OreProperty prop = material.getProperty(PropertyKey.ORE);
        Preconditions.checkNotNull(prop,
                "material %s has no ore property, but needs one for an ore model!".formatted(material.getName()));

        // read the base ore model JSON
        JsonObject original;
        try (BufferedReader reader = Minecraft.getInstance().getResourceManager()
                .openAsReader(GTCEu.id("models/block/ore%s.json".formatted(prop.isEmissive() ? "_emissive" : "")))) {
            original = GsonHelper.parse(reader, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // clone it
        JsonObject newJson = original.deepCopy();
        JsonObject children = newJson.getAsJsonObject("children");
        // add the base stone texture.
        children.getAsJsonObject("base_stone").addProperty("parent",
                TagPrefix.ORES.get(prefix).baseModelLocation().toString());

        ResourceLocation layer0 = prefix.materialIconType().getBlockTexturePath(material.getMaterialIconSet(), true);
        ResourceLocation layer1 = prefix.materialIconType().getBlockTexturePath(material.getMaterialIconSet(), "layer2",
                true);
        JsonObject oresTextures = children.getAsJsonObject("ore_texture").getAsJsonObject("textures");
        oresTextures.addProperty("layer0", layer0.toString());
        oresTextures.addProperty("layer1", layer1.toString());

        newJson.getAsJsonObject("textures").addProperty("particle", layer0.toString());

        GTDynamicResourcePack.addBlockModel(modelId, newJson);
    }
}
