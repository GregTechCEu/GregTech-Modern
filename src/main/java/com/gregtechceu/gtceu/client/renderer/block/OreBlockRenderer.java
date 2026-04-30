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

import net.minecraft.client.Minecraft;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.model.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.ApiStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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

            Identifier blockId = BuiltInRegistries.BLOCK.getKey(model.block);
            Identifier modelId = GTCEu.id(ORE_MODEL_NAME_FORMAT
                    .formatted(iconSet.name, tagPrefix.name, iconType.name()));

            GTDynamicResourcePack.addBlockState(
                    BlockModelGenerators.createSimpleBlock(model.block, BlockModelGenerators.plainVariant(modelId)));
            // Ore block models use tintindex 0 (primary) and tintindex 1 (secondary) on faces.
            // The item definition must carry matching gtceu:item_color tints so the runtime
            // GTItemColors handler is invoked when the block's item form is rendered.
            GTDynamicResourcePack.addTintedItemModel(BuiltInRegistries.ITEM.getKey(model.block.asItem()),
                    new DelegatedModel(modelId).get(), 2);
        }

        TEMPLATE_MODEL_CACHE.getCache().clear();
    }

    /// This is called for every combination of tag prefix + icon type + icon set
    protected static void copyOreModelWithBaseStone(TagPrefix tagPrefix, TagPrefix.OreType oreType,
                                                    MaterialIconType iconType, MaterialIconSet iconSet) {
        JsonObject newJson;
        try {
            JsonObject original = TEMPLATE_MODEL_CACHE.apply(iconType, iconSet);
            if (original == NULL_ELEMENT_MARKER) {
                // if the icon set doesn't have an ore model (somehow...), skip it
                return;
            }
            newJson = createOreModelWithBaseStone(oreType.baseModelLocation(), original);
        } catch (RuntimeException e) {
            GTCEu.LOGGER.error("Could not build ore block model for ore type {}, icon type '{}', icon set '{}'",
                    tagPrefix.name, iconType.name(), iconSet.name, e);
            return;
        }

        GTDynamicResourcePack.addBlockModel(
                GTCEu.id(ORE_MODEL_NAME_FORMAT.formatted(iconSet.name, tagPrefix.name, iconType.name())), newJson);
    }

    private static JsonObject createOreModelWithBaseStone(Identifier baseModelLocation, JsonObject template) {
        JsonObject oreModel = unwrapOreModel(template);
        JsonObject baseModel = flattenModel(baseModelLocation, new HashSet<>());

        JsonObject model = new JsonObject();
        JsonObject textures = new JsonObject();
        JsonArray elements = new JsonArray();

        model.addProperty("parent", "block/block");
        copyTextures(textures, baseModel);
        copyTextures(textures, oreModel);
        copyElements(elements, baseModel);
        // Ore template elements (#layer0, #layer1) are full coplanar cubes that would
        // z-fight with the base stone cube — and the ore textures are mostly transparent,
        // so the base stone bleeds through. Push each overlay slightly outward by a
        // progressive offset so they stack outside the stone and outside each other.
        // We also pin explicit UVs to [0, 0, 16, 16]: auto-UV would otherwise compute
        // from the offset coordinates and exceed [0, 16], failing the bake atlas check.
        copyOreOverlayElements(elements, oreModel);

        model.add("textures", textures);
        model.add("elements", elements);
        return model;
    }

    private static JsonObject unwrapOreModel(JsonObject template) {
        if (template.has("children")) {
            JsonObject children = template.getAsJsonObject("children");
            if (children.has("ore_texture")) {
                return children.getAsJsonObject("ore_texture");
            }
        }
        return template;
    }

    private static JsonObject flattenModel(Identifier modelLocation, Set<Identifier> seen) {
        if (!seen.add(modelLocation)) {
            throw new RuntimeException("Circular parent chain while loading model " + modelLocation);
        }

        JsonObject model = loadRequiredModel(modelLocation);
        JsonObject flattened = new JsonObject();
        JsonObject textures = new JsonObject();
        JsonArray elements = null;

        if (model.has("parent")) {
            JsonObject parent = flattenModel(Identifier.parse(model.get("parent").getAsString()), seen);
            copyTextures(textures, parent);
            if (parent.has("elements")) {
                elements = parent.getAsJsonArray("elements").deepCopy();
            }
        }

        copyTextures(textures, model);
        if (model.has("elements")) {
            elements = model.getAsJsonArray("elements").deepCopy();
        }

        flattened.add("textures", textures);
        if (elements != null) {
            flattened.add("elements", elements);
        }
        seen.remove(modelLocation);
        return flattened;
    }

    private static JsonObject loadRequiredModel(Identifier modelLocation) {
        Identifier modelPath = GTDynamicResourcePack.MODEL_ID_CONVERTER.idToFile(modelLocation);
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        Optional<Resource> modelResource = resourceManager.getResource(modelPath);

        if (modelResource.isEmpty()) {
            throw new RuntimeException("Missing block model " + modelLocation + " at " + modelPath);
        }
        try (BufferedReader reader = modelResource.get().openAsReader()) {
            return GsonHelper.parse(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void copyTextures(JsonObject target, JsonObject model) {
        if (!model.has("textures")) {
            return;
        }
        for (var entry : model.getAsJsonObject("textures").entrySet()) {
            target.add(entry.getKey(), entry.getValue().deepCopy());
        }
    }

    private static void copyElements(JsonArray target, JsonObject model) {
        if (!model.has("elements")) {
            return;
        }
        for (JsonElement element : model.getAsJsonArray("elements")) {
            target.add(element.deepCopy());
        }
    }

    private static void copyOreOverlayElements(JsonArray target, JsonObject model) {
        if (!model.has("elements")) {
            return;
        }
        JsonArray src = model.getAsJsonArray("elements");
        for (int i = 0; i < src.size(); i++) {
            JsonObject element = src.get(i).getAsJsonObject().deepCopy();
            // Progressive outward offset: first overlay sits 0.005 units outside the
            // base stone, second sits another 0.005 outside that. Far enough to
            // resolve depth precision against the base cube.
            float offset = 0.005f * (i + 1);
            offsetElementBounds(element, offset);
            // Pin explicit UVs so auto-UV doesn't compute from the offset coords and
            // exceed [0, 16], which would fail the bake atlas check.
            pinFaceUVs(element);
            // Strip cullface — the bounds now lie outside [0, 16], so the neighbor-
            // facing cull check against the inner face position would still cull the
            // overlay even though it actually pokes out past the block boundary.
            stripCullface(element);
            target.add(element);
        }
    }

    private static void offsetElementBounds(JsonObject element, float offset) {
        JsonArray from = element.getAsJsonArray("from");
        JsonArray to = element.getAsJsonArray("to");
        for (int i = 0; i < 3; i++) {
            from.set(i, new com.google.gson.JsonPrimitive(from.get(i).getAsFloat() - offset));
            to.set(i, new com.google.gson.JsonPrimitive(to.get(i).getAsFloat() + offset));
        }
    }

    private static void pinFaceUVs(JsonObject element) {
        if (!element.has("faces")) return;
        JsonObject faces = element.getAsJsonObject("faces");
        for (var entry : faces.entrySet()) {
            JsonObject face = entry.getValue().getAsJsonObject();
            if (!face.has("uv")) {
                JsonArray uv = new JsonArray(4);
                uv.add(0);
                uv.add(0);
                uv.add(16);
                uv.add(16);
                face.add("uv", uv);
            }
        }
    }

    private static void stripCullface(JsonObject element) {
        if (!element.has("faces")) return;
        JsonObject faces = element.getAsJsonObject("faces");
        for (var entry : faces.entrySet()) {
            JsonObject face = entry.getValue().getAsJsonObject();
            face.remove("cullface");
        }
    }

    private static JsonObject loadTemplateOreModel(MaterialIconType iconType, MaterialIconSet iconSet) {
        Identifier baseModelPath = iconType.getBlockModelPath(iconSet, true);
        baseModelPath = GTDynamicResourcePack.MODEL_ID_CONVERTER.idToFile(baseModelPath);

        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        Optional<Resource> modelResource = resourceManager.getResource(baseModelPath);

        if (modelResource.isEmpty()) {
            // if the icon set doesn't have an ore model (somehow...), skip it gracefully
            return NULL_ELEMENT_MARKER;
        }
        // read & cache the base ore model JSON
        try (BufferedReader reader = modelResource.get().openAsReader()) {
            return GsonHelper.parse(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
