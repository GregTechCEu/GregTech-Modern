package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.api.item.module.TieredAttributeItemModule;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.common.NeoForgeMod;

public class SwimSpeedModule extends TieredAttributeItemModule {

    public SwimSpeedModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("module.gtceu.swim_speed.description", getMaxAttributeAmount() * 100);
    }

    @Override
    public Holder<Attribute> getAttribute(ModuleContext moduleContext) {
        return NeoForgeMod.SWIM_SPEED;
    }

    @Override
    public double getMaxAttributeAmount() {
        return getTier() / 8d;
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
