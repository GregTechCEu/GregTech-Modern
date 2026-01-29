package com.gregtechceu.gtceu.api.item.armor;

import net.minecraft.Util;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.UUID;

public interface IArmorLogic {

    UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("2adb6ae9-df4d-4a45-bb07-8553dd4b6832");
    UUID ATTACK_SPEED_MODIFIER = UUID.fromString("876a7cd1-aec0-4ae5-ae3f-e951d5f1965a");
    EnumMap<ArmorItem.Type, UUID> ARMOR_MODIFIER_UUID_PER_TYPE = Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
        map.put(ArmorItem.Type.BOOTS, UUID.fromString("be2b5c6e-bb5d-4675-a6be-c6c488a437f5"));
        map.put(ArmorItem.Type.LEGGINGS, UUID.fromString("435c34aa-0c5b-442d-abb0-d1c984c894f9"));
        map.put(ArmorItem.Type.CHESTPLATE, UUID.fromString("77d81693-63cc-4593-977d-8e0391d94a77"));
        map.put(ArmorItem.Type.HELMET, UUID.fromString("cc839692-3a33-4907-a114-21fa0a18184c"));
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

    default int damageArmor(LivingEntity entity, ItemStack itemStack, DamageSource source, int damage,
                            EquipmentSlot equipmentSlot) {
        return 0;
    }

    default Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return ImmutableMultimap.of();
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
    ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type);

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

    default void onEquip(Player player) {}

    default void onUnequip(Player player) {}
}
