package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.module.AttributeItemModule;
import com.gregtechceu.gtceu.api.item.module.ITieredItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForgeMod;

public class CreativeFlightModule extends AttributeItemModule implements ITieredItemModule {

    public CreativeFlightModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public Component getInfo() {
        return Component.translatable(getDescriptionLanguageKey(), 2048);
    }

    private boolean isFlying(LivingEntity entity) {
        if (entity instanceof Player player) {
            return player.getAbilities().flying;
        } else return false;
    }

    @Override
    public Holder<Attribute> getAttribute(ModuleContext moduleContext) {
        return NeoForgeMod.CREATIVE_FLIGHT;
    }

    @Override
    public double getModifier(ModuleContext moduleContext) {
        return 1;
    }

    @Override
    protected double getNeutralModifier(ModuleContext moduleContext) {
        return 1;
    }

    @Override
    public AttributeModifier.Operation getModifierOperation() {
        return AttributeModifier.Operation.ADD_VALUE;
    }

    @Override
    public double getMaxAttributeAmount() {
        return 1;
    }

    @Override
    public Component getDisplayName(ModuleContext moduleContext) {
        return Component.translatable(getLanguageKey()).withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    @Override
    public boolean keepAttributeActive(ModuleContext moduleContext, LivingEntity entity) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        if (electricItem == null) return false;
        if (!isFlying(entity)) return true;

        if (!electricItem.canUse(2048)) return false;
        else {
            electricItem.discharge(2048, electricItem.getTier(), true, false, false);
            return true;
        }
    }

    @Override
    public int getTier() {
        return GTValues.IV;
    }
}
