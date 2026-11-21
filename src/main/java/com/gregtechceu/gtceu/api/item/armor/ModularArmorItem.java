package com.gregtechceu.gtceu.api.item.armor;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.*;
import com.gregtechceu.gtceu.api.item.module.IModularItem;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Accessors(chain = true)
public class ModularArmorItem extends ArmorComponentItem implements IComponentItem {

    public static final String NETHERITE_PLATED_TAG = "netherite_plated";

    public ModularArmorItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide || getEquipmentSlot() != EquipmentSlot.CHEST ||
                !(entity instanceof Player player) || player.getItemBySlot(EquipmentSlot.CHEST) != stack ||
                !(stack.getTag() != null && stack.getTag().getBoolean("DischargeMode")))
            return;
        IElectricItem battery = GTCapabilityHelper.getElectricItem(stack);
        if (battery != null) ArmorUtils.chargeCarriedItems(player, stack, battery, battery.getTransferLimit());
    }

    public float getHeatResistance() {
        return 1.0f;
    }

    public void appendBuiltInTraits(ItemStack stack, List<Component> tooltip) {
        List<Component> traits = new ArrayList<>();
        if (stack.is(CustomTags.FALL_IMMUNE_WEARABLES)) {
            traits.add(Component.translatable("tooltip.gtceu.armor.trait.fall_protection"));
        }
        if (stack.is(CustomTags.FIRE_IMMUNE_WEARABLES)) {
            traits.add(Component.translatable("tooltip.gtceu.armor.trait.fire_protection"));
        }
        if (stack.is(ItemTags.FREEZE_IMMUNE_WEARABLES)) {
            traits.add(Component.translatable("tooltip.gtceu.armor.trait.freeze_protection"));
        }
        if (getHeatResistance() < 1.0f) {
            traits.add(Component.translatable("tooltip.gtceu.armor.trait.heat_protection",
                    Math.round((1.0f - getHeatResistance()) * 100)));
        }
        if (stack.is(CustomTags.PPE_ARMOR)) {
            traits.add(Component.translatable("tooltip.gtceu.armor.trait.hazard_protection"));
        }
        if (traits.isEmpty()) return;

        tooltip.add(Component.translatable("tooltip.gtceu.armor.built_in_traits"));
        for (Component trait : traits) {
            tooltip.add(Component.literal(" - ").append(trait).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        Component name = super.getName(stack);
        if (stack.hasTag() && stack.getOrCreateTag().contains(NETHERITE_PLATED_TAG)) {
            return Component.translatable("item.gtceu.netherite_plated", name);
        }
        return name;
    }

    @Override
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        IModularItem modular = GTCapabilityHelper.getModularItem(stack);
        if (modular != null) {
            for (var ctx : modular.getAllModuleInstances()) {
                ResourceLocation texture = ctx.getModule().getArmorTexture(ctx, entity, slot, type);
                if (texture != null) return texture.toString();
            }
        }
        return super.getArmorTexture(stack, entity, slot, type);
    }

    @Override
    public boolean isPPE(ItemStack stack) {
        IModularItem modular = GTCapabilityHelper.getModularItem(stack);
        if (modular != null) {
            for (var ctx : modular.getAllModuleInstances()) {
                if (ctx.getModule().isPPE(ctx)) return true;
            }
        }
        return super.isPPE(stack);
    }
}
