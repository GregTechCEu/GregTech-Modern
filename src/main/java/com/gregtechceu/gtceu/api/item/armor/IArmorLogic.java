package com.gregtechceu.gtceu.api.item.armor;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.Util;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

public interface IArmorLogic {

    UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("648D7064-6A60-4F59-8ABE-C2C23A6DD7A9");
    UUID ATTACK_SPEED_MODIFIER = UUID.fromString("FA233E1C-4180-4288-B05C-BCCE9785ACA3");
    EnumMap<ArmorItem.Type, ResourceLocation> ARMOR_MODIFIER_UUID_PER_TYPE = Util
            .make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, GTCEu.id("boot_modifier"));
                map.put(ArmorItem.Type.LEGGINGS, GTCEu.id("legs_modifier"));
                map.put(ArmorItem.Type.CHESTPLATE, GTCEu.id("chest_modifier"));
                map.put(ArmorItem.Type.HELMET, GTCEu.id("helmet_modifier"));
            });

    default void addToolComponents(ArmorComponentItem item) {}

    ArmorItem.Type getArmorType();

    /**
     * Get the displayed effective armor.
     *
     * @return The number of armor points for display, 2 per shield.
     */
    int getArmorDisplay(Player player, @NotNull ItemStack armor, EquipmentSlot slot);

    default boolean canBreakWithDamage(ItemStack stack) {
        return false;
    }

    default boolean isPPE() {
        return false;
    }

    default void damageArmor(LivingEntity entity, ItemStack itemStack, DamageSource source, int damage) {}

    default List<ItemAttributeModifiers.Entry> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return Collections.emptyList();
    }

    default boolean isValidArmor(ItemStack itemStack, Entity entity, EquipmentSlot equipmentSlot) {
        return getArmorType().getSlot() == equipmentSlot;
    }

    default void onArmorTick(Level world, Player player, ItemStack itemStack) {}

    @OnlyIn(Dist.CLIENT)
    default void renderHelmetOverlay(ItemStack itemStack, Player player, float partialTicks) {}

    default int getArmorLayersAmount(ItemStack itemStack) {
        return 1;
    }

    default int getArmorLayerColor(ItemStack itemStack, int layerIndex) {
        return 0xFFFFFF;
    }

    @Nullable
    ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer);

    @NotNull
    default HumanoidModel<?> getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot,
                                           HumanoidModel<?> defaultModel) {
        return defaultModel;
    }

    /**
     *
     * @return the value to multiply heat damage by
     */
    default float getHeatResistance() {
        return 1.0f;
    }
}
