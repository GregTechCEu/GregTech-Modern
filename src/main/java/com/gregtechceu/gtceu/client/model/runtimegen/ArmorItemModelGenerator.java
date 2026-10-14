package com.gregtechceu.gtceu.client.model.runtimegen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ArmorItemModelGenerator {

    private static final Set<ArmorItemModelGenerator> MODELS = new HashSet<>();

    public static void reinitModels() {
        for (ArmorItemModelGenerator model : MODELS) {
            // read the base armor model JSON
            ResourceLocation modelLocation = GTCEu.id("models/item/armor/%s.json".formatted(model.armorType.getName()));
            JsonObject original;
            try (BufferedReader reader = Minecraft.getInstance().getResourceManager().openAsReader(modelLocation)) {
                original = GsonHelper.parse(reader, true);
            } catch (IOException e) {
                GTCEu.LOGGER.warn("Unable to load model: '{}': {}", modelLocation, e);
                continue;
            }

            GTDynamicResourcePack.addItemModel(BuiltInRegistries.ITEM.getKey(model.item), original);
        }
    }

    private final Item item;
    private final ArmorItem.Type armorType;

    protected ArmorItemModelGenerator(Item item, ArmorItem.Type armorType) {
        this.item = item;
        this.armorType = armorType;
    }

    public static void add(Item item, ArmorItem.Type armorType) {
        MODELS.add(new ArmorItemModelGenerator(item, armorType));
    }
}
