package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.api.item.module.TieredAttributeItemModule;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class AttackSpeedItemModule extends TieredAttributeItemModule {

    public AttackSpeedItemModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public double getEnergyCost() {
        return ArmorUtils.getModuleEnergyCost(getTier());
    }

    @Override
    public Component getInfo() {
        return Component.translatable(getDescriptionLanguageKey(), getMaxAttributeAmount() * 100, getEnergyCost());
    }

    @Override
    public Holder<Attribute> getAttribute(ModuleContext moduleContext) {
        return Attributes.ATTACK_SPEED;
    }

    @Override
    public double getMaxAttributeAmount() {
        return getTier() / 32d;
    }

    @Override
    protected double getLegacyMaxAttributeAmount() {
        return 1 + getMaxAttributeAmount();
    }

    @Override
    public AttributeModifier.Operation getModifierOperation() {
        return AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
    }
}
