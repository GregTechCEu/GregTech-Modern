package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

public class GTArmorMaterials {

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister
            .create(BuiltInRegistries.ARMOR_MATERIAL, GTCEu.MOD_ID);

    public static final List<ArmorMaterial.Layer> BLANK_LAYERS = List.of(new ArmorMaterial.Layer(GTCEu.id("armor")));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> GOGGLES = ARMOR_MATERIALS.register("goggles",
            () -> new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 0);
                map.put(ArmorItem.Type.LEGGINGS, 0);
                map.put(ArmorItem.Type.CHESTPLATE, 0);
                map.put(ArmorItem.Type.HELMET, 0);
            }), 50, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY, BLANK_LAYERS, 0.0F, 0.0F));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> JETPACK = ARMOR_MATERIALS.register("jetpack",
            () -> new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 0);
                map.put(ArmorItem.Type.LEGGINGS, 0);
                map.put(ArmorItem.Type.CHESTPLATE, 0);
                map.put(ArmorItem.Type.HELMET, 0);
            }), 50, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY, BLANK_LAYERS, 0.0F, 0.0F));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ARMOR = ARMOR_MATERIALS.register("armor",
            () -> new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 0);
                map.put(ArmorItem.Type.LEGGINGS, 0);
                map.put(ArmorItem.Type.CHESTPLATE, 0);
                map.put(ArmorItem.Type.HELMET, 0);
            }), 50, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY, BLANK_LAYERS, 5.0F, 0.0F));
}
