package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.api.item.module.TieredAttributeItemModule;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

public class AttackSpeedItemModule extends TieredAttributeItemModule {

    public AttackSpeedItemModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.attack_speed", getTier() * 100 / 16d);
    }

    @Override
    public Holder<Attribute> getAttribute(ModuleContext moduleContext) {
        return Attributes.ATTACK_SPEED;
    }

    @Override
    public double getMaxAttributeAmount() {
        return 1 + getTier() / 16d;
    }

    @Override
    public AttributeModifier.Operation getModifierOperation() {
        return AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
    }

    @Override
    public void appendHoverText(ModuleContext moduleContext, Item.TooltipContext context, TooltipFlag isAdvanced,
                                List<Component> tooltips) {
        super.appendHoverText(moduleContext, context, isAdvanced, tooltips);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.attack_speed",
                GTValues.VNF[getTier()]));
    }
}
