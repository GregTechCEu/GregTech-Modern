package com.gregtechceu.gtceu.api.item.armor;

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
import java.util.List;

public interface IArmorLogic {

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
