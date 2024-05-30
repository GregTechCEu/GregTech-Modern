package com.gregtechceu.gtceu.client.renderer.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author KilaBash
 * @date 2023/2/16
 * @implNote TagPrefixItemRenderer
 */
public class ToolItemRenderer {

    private static final Set<ToolItemRenderer> MODELS = new HashSet<>();

    public static void reinitModels() {
        for (ToolItemRenderer model : MODELS) {
            GTDynamicResourcePack.addItemModel(BuiltInRegistries.ITEM.getKey(model.item),
                    () -> {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("parent", model.toolType.modelLocation.toString());
                        JsonArray clonedOverrides = cloneToolModelOverrides(model.toolType.modelLocation);
                        if (clonedOverrides != null) {
                            jsonObject.add("overrides", clonedOverrides);
                        }
                        return jsonObject;
                    });
        }
    }

    private final Item item;
    private final GTToolType toolType;

    protected ToolItemRenderer(Item item, GTToolType toolType) {
        this.item = item;
        this.toolType = toolType;
    }

    public static void create(Item item, GTToolType toolType) {
        MODELS.add(new ToolItemRenderer(item, toolType));
    }

    /**
     * Clones the overrides for a single tool model.
     *
     * @param modelId the model id (usually {@code gtceu:item/tools/<tool id path>})
     */
    @Nullable
    public static JsonArray cloneToolModelOverrides(ResourceLocation modelId) {
        JsonObject original;
        try (BufferedReader reader = Minecraft.getInstance().getResourceManager().openAsReader(modelId.withPrefix("models/").withSuffix(".json"))) {
            original = GsonHelper.parse(reader, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // clone it
        JsonObject newJson = original.deepCopy();
        if (newJson.has("overrides")) {
            return newJson.getAsJsonArray("overrides");
        }
        return null;
    }
}
