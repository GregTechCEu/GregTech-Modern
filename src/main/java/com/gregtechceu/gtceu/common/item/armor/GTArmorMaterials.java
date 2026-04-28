package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;
import java.util.Map;

public class GTArmorMaterials {

    public static final ResourceKey<EquipmentAsset> BLANK_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID,
            GTCEu.id("armor"));
    private static final TagKey<Item> EMPTY_REPAIR_INGREDIENT = TagKey.create(Registries.ITEM,
            GTCEu.id("empty_armor_repair"));

    public static final ArmorMaterial GOGGLES = material(0, Util.make(new EnumMap<>(ArmorType.class), map -> {
        map.put(ArmorType.BOOTS, 0);
        map.put(ArmorType.LEGGINGS, 0);
        map.put(ArmorType.CHESTPLATE, 0);
        map.put(ArmorType.HELMET, 0);
    }), 1, 0.0F, 0.0F);
    public static final ArmorMaterial JETPACK = material(0, Util.make(new EnumMap<>(ArmorType.class), map -> {
        map.put(ArmorType.BOOTS, 0);
        map.put(ArmorType.LEGGINGS, 0);
        map.put(ArmorType.CHESTPLATE, 0);
        map.put(ArmorType.HELMET, 0);
    }), 1, 0.0F, 0.0F);
    public static final ArmorMaterial ARMOR = material(0, Util.make(new EnumMap<>(ArmorType.class), map -> {
        map.put(ArmorType.BOOTS, 0);
        map.put(ArmorType.LEGGINGS, 0);
        map.put(ArmorType.CHESTPLATE, 0);
        map.put(ArmorType.HELMET, 0);
    }), 1, 5.0F, 0.0F);
    public static final ArmorMaterial BAD_PPE_EQUIPMENT = material(10, Util.make(new EnumMap<>(ArmorType.class),
            map -> {
                map.put(ArmorType.BOOTS, 1);
                map.put(ArmorType.LEGGINGS, 2);
                map.put(ArmorType.CHESTPLATE, 3);
                map.put(ArmorType.HELMET, 1);
            }), 10, 0.0F, 0.0F);
    public static final ArmorMaterial GOOD_PPE_EQUIPMENT = material(10, Util.make(new EnumMap<>(ArmorType.class),
            map -> {
                map.put(ArmorType.BOOTS, 2);
                map.put(ArmorType.LEGGINGS, 5);
                map.put(ArmorType.CHESTPLATE, 6);
                map.put(ArmorType.HELMET, 2);
            }), 10, 0.0F, 0.0F);

    private static ArmorMaterial material(int durability, Map<ArmorType, Integer> defense, int enchantmentValue,
                                          float toughness, float knockbackResistance) {
        return new ArmorMaterial(durability, defense, enchantmentValue, SoundEvents.ARMOR_EQUIP_GENERIC, toughness,
                knockbackResistance, EMPTY_REPAIR_INGREDIENT, BLANK_ASSET);
    }
}
