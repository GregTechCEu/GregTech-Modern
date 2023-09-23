package com.gregtechceu.gtceu.api.item.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.model.HumanoidModel;
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

import javax.annotation.Nullable;
import java.util.UUID;

public interface IArmorLogic {

    UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("648D7064-6A60-4F59-8ABE-C2C23A6DD7A9");
    UUID ATTACK_SPEED_MODIFIER = UUID.fromString("FA233E1C-4180-4288-B05C-BCCE9785ACA3");

    default void addToolComponents(ArmorComponentItem metaValueItem) {
    }

    ArmorItem.Type getEquipmentSlot();

    default boolean canBreakWithDamage(ItemStack stack) {
        return false;
    }

    default void damageArmor(LivingEntity entity, ItemStack itemStack, DamageSource source, int damage, EquipmentSlot equipmentSlot) {

    }

    default Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return ImmutableMultimap.of();
    }

    default boolean isValidArmor(ItemStack itemStack, Entity entity, EquipmentSlot equipmentSlot) {
        return getEquipmentSlot().getSlot() == equipmentSlot;
    }

    default void onArmorTick(Level world, Entity player, ItemStack itemStack) {
    }

    @OnlyIn(Dist.CLIENT)
    default void renderHelmetOverlay(ItemStack itemStack, Player player, float partialTicks) {
    }

    default int getArmorLayersAmount(ItemStack itemStack) {
        return 1;
    }

    default int getArmorLayerColor(ItemStack itemStack, int layerIndex) {
        return 0xFFFFFF;
    }

    String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type);

    @Nullable
    default HumanoidModel<?> getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel<?> defaultModel) {
        return null;
    }

    /**
     *
     * @return the value to multiply heat damage by
     */
    default float getHeatResistance() {
        return 1.0f;
    }
}
