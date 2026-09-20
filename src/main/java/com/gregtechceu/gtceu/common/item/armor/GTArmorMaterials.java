package com.gregtechceu.gtceu.common.item.armor;

import net.minecraft.util.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.neoforge.common.util.Lazy;

import com.mojang.serialization.Codec;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.EnumMap;

@AllArgsConstructor
public enum GTArmorMaterials implements ArmorMaterial, StringRepresentable {

    GOGGLES("goggles", 0, Util.make(new EnumMap<>(ArmorType.class), map -> {
        map.put(ArmorType.BOOTS, 0);
        map.put(ArmorType.LEGGINGS, 0);
        map.put(ArmorType.CHESTPLATE, 0);
        map.put(ArmorType.HELMET, 0);
    }), 50, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, () -> Ingredient.EMPTY),
    JETPACK("jetpack", 0, Util.make(new EnumMap<>(ArmorType.class), map -> {
        map.put(ArmorType.BOOTS, 0);
        map.put(ArmorType.LEGGINGS, 0);
        map.put(ArmorType.CHESTPLATE, 0);
        map.put(ArmorType.HELMET, 0);
    }), 50, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, () -> Ingredient.EMPTY),
    ARMOR("armor", 0, Util.make(new EnumMap<>(ArmorType.class), map -> {
        map.put(ArmorType.BOOTS, 0);
        map.put(ArmorType.LEGGINGS, 0);
        map.put(ArmorType.CHESTPLATE, 0);
        map.put(ArmorType.HELMET, 0);
    }), 50, SoundEvents.ARMOR_EQUIP_GENERIC, 5.0F, 0.0F, () -> Ingredient.EMPTY),
    BAD_PPE_EQUIPMENT("bad_ppe_equipment", 10, Util.make(new EnumMap<>(ArmorType.class), map -> {
        map.put(ArmorType.BOOTS, 1);
        map.put(ArmorType.LEGGINGS, 2);
        map.put(ArmorType.CHESTPLATE, 3);
        map.put(ArmorType.HELMET, 1);
    }), 10, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, () -> Ingredient.EMPTY),
    GOOD_PPE_EQUIPMENT("good_ppe_equipment", 20, Util.make(new EnumMap<>(ArmorType.class), map -> {
        map.put(ArmorType.BOOTS, 2);
        map.put(ArmorType.LEGGINGS, 5);
        map.put(ArmorType.CHESTPLATE, 6);
        map.put(ArmorType.HELMET, 2);
    }), 10, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, () -> Ingredient.EMPTY),
    ;

    public static final Codec<GTArmorMaterials> CODEC = StringRepresentable.fromEnum(GTArmorMaterials::values);
    private static final EnumMap<ArmorType, Integer> HEALTH_FUNCTION_FOR_TYPE = Util
            .make(new EnumMap<>(ArmorType.class), map -> {
                map.put(ArmorType.BOOTS, 13);
                map.put(ArmorType.LEGGINGS, 15);
                map.put(ArmorType.CHESTPLATE, 16);
                map.put(ArmorType.HELMET, 11);
            });

    private final String name;
    private final int durabilityMultiplier;
    private final EnumMap<ArmorType, Integer> protectionFunctionForType;
    @Getter
    private final int enchantmentValue;
    @Getter
    private final SoundEvent equipSound;
    @Getter
    private final float toughness;
    @Getter
    private final float knockbackResistance;
    private final Lazy<Ingredient> repairIngredient;

    @Override
    public int getDurabilityForType(ArmorType type) {
        return HEALTH_FUNCTION_FOR_TYPE.get(type) * this.durabilityMultiplier;
    }

    @Override
    public int getDefenseForType(ArmorType type) {
        return this.protectionFunctionForType.get(type);
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
