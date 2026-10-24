package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

public class GTArmorMaterials {

    private static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister
            .create(BuiltInRegistries.ARMOR_MATERIAL, GTCEu.MOD_ID);

    public static final List<ArmorMaterial.Layer> BLANK_LAYERS = List.of(new ArmorMaterial.Layer(GTCEu.id("armor")));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> GOGGLES = ARMOR_MATERIALS.register("goggles",
            () -> new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 0);
                map.put(ArmorItem.Type.LEGGINGS, 0);
                map.put(ArmorItem.Type.CHESTPLATE, 0);
                map.put(ArmorItem.Type.HELMET, 0);
            }), 0, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY, BLANK_LAYERS, 0.0F, 0.0F));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> JETPACK = ARMOR_MATERIALS.register("jetpack",
            () -> new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 0);
                map.put(ArmorItem.Type.LEGGINGS, 0);
                map.put(ArmorItem.Type.CHESTPLATE, 0);
                map.put(ArmorItem.Type.HELMET, 0);
            }), 0, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY, BLANK_LAYERS, 0.0F, 0.0F));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> BAD_PPE_EQUIPMENT = ARMOR_MATERIALS
            .register("bad_ppe_equipment",
                    () -> new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 1);
                        map.put(ArmorItem.Type.LEGGINGS, 2);
                        map.put(ArmorItem.Type.CHESTPLATE, 3);
                        map.put(ArmorItem.Type.HELMET, 1);
                    }), 10, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY, BLANK_LAYERS, 0.0F, 0.0F));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> GOOD_PPE_EQUIPMENT = ARMOR_MATERIALS
            .register("good_ppe_equipment",
                    () -> new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 2);
                        map.put(ArmorItem.Type.LEGGINGS, 5);
                        map.put(ArmorItem.Type.CHESTPLATE, 6);
                        map.put(ArmorItem.Type.HELMET, 2);
                    }), 10, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY, BLANK_LAYERS, 0.0F, 0.0F));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> HYDRAULIC = ARMOR_MATERIALS.register(
            "hydraulic",
            () -> new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 3);
                map.put(ArmorItem.Type.LEGGINGS, 6);
                map.put(ArmorItem.Type.CHESTPLATE, 8);
                map.put(ArmorItem.Type.HELMET, 3);
            }), 50, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY, BLANK_LAYERS, 2.0F, 0.0F));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> NANO_MUSCLE = ARMOR_MATERIALS.register(
            "nano_muscle",
            () -> new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 4);
                map.put(ArmorItem.Type.LEGGINGS, 8);
                map.put(ArmorItem.Type.CHESTPLATE, 12);
                map.put(ArmorItem.Type.HELMET, 4);
            }), 50, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY, BLANK_LAYERS, 4.0F, 0.0F));
    // Note: Quark-Matter Armor exceeds Vanilla's Armor and Toughness limits.
    // We may want to consider Apothic Attributes in the future if other modular tool/gear systems demand it and justify
    // it, this is slightly misleading but suffices for vanilla
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> QUARK_TECH = ARMOR_MATERIALS.register("quark_tech",
            () -> new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 6);
                map.put(ArmorItem.Type.LEGGINGS, 12);
                map.put(ArmorItem.Type.CHESTPLATE, 18);
                map.put(ArmorItem.Type.HELMET, 6);
            }), 50, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY, BLANK_LAYERS, 5.0F, 0.0F));

    public static void init(IEventBus modBus) {
        ARMOR_MATERIALS.register(modBus);
    }
}
