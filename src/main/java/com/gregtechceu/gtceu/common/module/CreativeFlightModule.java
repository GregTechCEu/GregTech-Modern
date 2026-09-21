package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.module.ITieredItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.common.NeoForgeMod;

import java.util.List;

public class CreativeFlightModule extends ItemModule implements ITieredItemModule {

    public CreativeFlightModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("module.gtceu.creative_flight", 2048);
    }

    private boolean isFlying(LivingEntity entity) {
        if (entity instanceof Player player) {
            return player.getAbilities().flying;
        } else return false;
    }

    private void attachAttribute(ModuleContext moduleContext) {
        AttributeModifier attributeModifier = new AttributeModifier(getId(), 1d, AttributeModifier.Operation.ADD_VALUE);
        var modifiers = moduleContext.getAppliedTo().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.EMPTY);

        if (modifiers.modifiers().stream().anyMatch(v -> v.modifier().id().equals(getId()))) return;

        modifiers = modifiers.withModifierAdded(NeoForgeMod.CREATIVE_FLIGHT, attributeModifier,
                EquipmentSlotGroup.bySlot(getSlot(moduleContext.getAppliedTo())));

        moduleContext.getAppliedTo().set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
    }

    private EquipmentSlot getSlot(ItemStack stack) {
        Equipable equipable = Equipable.get(stack);
        if (equipable != null) return equipable.getEquipmentSlot();
        return EquipmentSlot.MAINHAND;
    }

    private void detachAttribute(ModuleContext moduleContext) {
        var modifiers = moduleContext.getAppliedTo().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.EMPTY);

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        modifiers.modifiers().forEach(v -> {
            if (v.modifier().id().equals(getId())) return;
            builder.add(v.attribute(), v.modifier(), v.slot());
        });

        moduleContext.getAppliedTo().set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    @Override
    public void onEquip(ModuleContext moduleContext, LivingEntity entity) {
        super.onEquip(moduleContext, entity);
        attachAttribute(moduleContext);
    }

    @Override
    public void onUnequip(ModuleContext moduleContext, LivingEntity entity) {
        super.onUnequip(moduleContext, entity);
        detachAttribute(moduleContext);
    }

    @Override
    public void onArmorTick(ModuleContext moduleContext, LivingEntity entity) {
        super.onArmorTick(moduleContext, entity);
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        if (electricItem == null || !isFlying(entity)) return;
        if (!electricItem.canUse(2048)) detachAttribute(moduleContext);
        else {
            electricItem.discharge(2048, electricItem.getTier(), true, false, false);
            attachAttribute(moduleContext);
        }
    }

    @Override
    public void appendHoverText(ModuleContext moduleContext, Item.TooltipContext context, List<Component> tooltips,
                                TooltipFlag isAdvanced) {
        super.appendHoverText(moduleContext, context, tooltips, isAdvanced);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.creative_flight")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    @Override
    public int getTier() {
        return GTValues.IV;
    }
}
