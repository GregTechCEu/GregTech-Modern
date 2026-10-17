package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.api.item.module.TieredAttributeItemModule;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class MovementSpeedItemModule extends TieredAttributeItemModule {

    public MovementSpeedItemModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("module.gtceu.movement_speed.description", getTier() * 100 / 8d);
    }

    @Override
    public Holder<Attribute> getAttribute(ModuleContext moduleContext) {
        return Attributes.MOVEMENT_SPEED;
    }

    @Override
    public double getMaxAttributeAmount() {
        return 1 + getTier() / 8d;
    }

    @Override
    public AttributeModifier.Operation getModifierOperation() {
        return AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
    }
}
