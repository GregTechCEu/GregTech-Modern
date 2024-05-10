package com.gregtechceu.gtceu.common.item.armor;

import com.mojang.serialization.Codec;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.util.Lazy;

import java.util.EnumMap;

@AllArgsConstructor
public enum GTArmorMaterials implements ArmorMaterial, StringRepresentable {
    GOGGLES("goggles", 7, Util.make(new EnumMap<>(EquipmentSlot.class), map -> {
        map.put(EquipmentSlot.FEET, 0);
        map.put(EquipmentSlot.LEGS, 0);
        map.put(EquipmentSlot.CHEST, 0);
        map.put(EquipmentSlot.HEAD, 0);
    }), 50, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, () -> Ingredient.EMPTY),
    JETPACK("jetpack", 15, Util.make(new EnumMap<>(EquipmentSlot.class), map -> {
        map.put(EquipmentSlot.FEET, 0);
        map.put(EquipmentSlot.LEGS, 0);
        map.put(EquipmentSlot.CHEST, 0);
        map.put(EquipmentSlot.HEAD, 0);
    }), 50, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, () -> Ingredient.EMPTY),
    ARMOR("armor", 40, Util.make(new EnumMap<>(EquipmentSlot.class), map -> {
        map.put(EquipmentSlot.FEET, 0);
        map.put(EquipmentSlot.LEGS, 0);
        map.put(EquipmentSlot.CHEST, 0);
        map.put(EquipmentSlot.HEAD, 0);
    }), 50, SoundEvents.ARMOR_EQUIP_GENERIC, 5.0F, 0.0F, () -> Ingredient.EMPTY),
    ;

    public static final Codec<GTArmorMaterials> CODEC = StringRepresentable.fromEnum(GTArmorMaterials::values);
    private static final EnumMap<EquipmentSlot, Integer> HEALTH_FUNCTION_FOR_TYPE = Util.make(new EnumMap<>(EquipmentSlot.class), map -> {
        map.put(EquipmentSlot.FEET, 13);
        map.put(EquipmentSlot.LEGS, 15);
        map.put(EquipmentSlot.CHEST, 16);
        map.put(EquipmentSlot.HEAD, 11);
    });

    private final String name;
    private final int durabilityMultiplier;
    private final EnumMap<EquipmentSlot, Integer> protectionFunctionForType;
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
    public int getDurabilityForSlot(EquipmentSlot slot) {
        return HEALTH_FUNCTION_FOR_TYPE.get(slot) * this.durabilityMultiplier;
    }

    @Override
    public int getDefenseForSlot(EquipmentSlot slot) {
        return this.protectionFunctionForType.get(slot);
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
