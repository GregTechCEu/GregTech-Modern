package com.gregtechceu.gtceu.client.renderer.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ArmorItemRenderer {

    private static final String[] TRIM_MATERIALS = {
            "quartz",
            "iron",
            "netherite",
            "redstone",
            "copper",
            "gold",
            "emerald",
            "diamond",
            "lapis",
            "amethyst",
            "resin"
    };

    private static final Set<ArmorItemRenderer> MODELS = new HashSet<>();

    public static void reinitModels() {
        for (ArmorItemRenderer model : MODELS) {
            // read the base armor model JSON
            JsonObject original;
            try (BufferedReader reader = Minecraft.getInstance().getResourceManager()
                    .openAsReader(GTCEu.id("models/item/armor/%s.json".formatted(model.armorType.getName())))) {
                original = GsonHelper.parse(reader);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Identifier itemId = BuiltInRegistries.ITEM.getKey(model.item);
            Identifier itemModel = itemId.withPrefix("item/");
            GTDynamicResourcePack.addModel(itemModel, original);
            GTDynamicResourcePack.addItemDefinition(itemId, createTrimmedItemDefinition(model.armorType, itemModel));
        }
    }

    private static JsonObject createTrimmedItemDefinition(ArmorType armorType, Identifier fallbackModel) {
        JsonObject model = new JsonObject();
        model.addProperty("type", "minecraft:select");
        model.addProperty("property", "minecraft:trim_material");

        JsonObject fallback = createModel(fallbackModel);
        model.add("fallback", fallback);

        JsonArray cases = new JsonArray();
        for (String trimMaterial : TRIM_MATERIALS) {
            JsonObject trimCase = new JsonObject();
            trimCase.addProperty("when", "minecraft:" + trimMaterial);
            trimCase.add("model", createModel(GTCEu.id("item/armor/trimmed/%s_%s_trim"
                    .formatted(armorType.getName(), trimMaterial))));
            cases.add(trimCase);
        }
        model.add("cases", cases);

        JsonObject definition = new JsonObject();
        definition.add("model", model);
        return definition;
    }

    private static JsonObject createModel(Identifier modelId) {
        JsonObject model = new JsonObject();
        model.addProperty("type", "minecraft:model");
        model.addProperty("model", modelId.toString());
        return model;
    }

    private final Item item;
    private final ArmorType armorType;

    protected ArmorItemRenderer(Item item, ArmorType armorType) {
        this.item = item;
        this.armorType = armorType;
    }

    public static void create(Item item, ArmorType armorType) {
        MODELS.add(new ArmorItemRenderer(item, armorType));
    }
}
