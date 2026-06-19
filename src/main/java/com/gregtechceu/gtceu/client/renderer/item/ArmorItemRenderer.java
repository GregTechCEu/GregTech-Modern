package com.gregtechceu.gtceu.client.renderer.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ArmorItemRenderer {

    private static final Set<ArmorItemRenderer> MODELS = new HashSet<>();

    public static void reinitModels() {
        for (ArmorItemRenderer model : MODELS) {
            // read the base armor model JSON
            JsonObject original;
            try (BufferedReader reader = Minecraft.getInstance().getResourceManager()
                    .openAsReader(GTCEu.id("models/item/armor/%s.json".formatted(model.armorType.getName())))) {
                original = GsonHelper.parse(reader, true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            GTDynamicResourcePack.addItemModel(BuiltInRegistries.ITEM.getKey(model.item), original);
        }
    }

    private final Item item;
    private final ArmorItem.Type armorType;

    protected ArmorItemRenderer(Item item, ArmorItem.Type armorType) {
        this.item = item;
        this.armorType = armorType;
    }

    public static void create(Item item, ArmorItem.Type armorType) {
        MODELS.add(new ArmorItemRenderer(item, armorType));
    }
}
