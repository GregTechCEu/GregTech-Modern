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

    UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("648D7064-6A60-4F59-8ABE-C2C23A6DD7A9");
    UUID ATTACK_SPEED_MODIFIER = UUID.fromString("FA233E1C-4180-4288-B05C-BCCE9785ACA3");
    EnumMap<ArmorItem.Type, UUID> ARMOR_MODIFIER_UUID_PER_TYPE = Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
        map.put(ArmorItem.Type.BOOTS, UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"));
        map.put(ArmorItem.Type.LEGGINGS, UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"));
        map.put(ArmorItem.Type.CHESTPLATE, UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"));
        map.put(ArmorItem.Type.HELMET, UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150"));
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
