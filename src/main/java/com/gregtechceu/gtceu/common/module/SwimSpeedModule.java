package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.api.item.module.TieredAttributeItemModule;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.common.NeoForgeMod;

import java.util.List;

public class SwimSpeedModule extends TieredAttributeItemModule {

    public SwimSpeedModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.swim_speed", getTier() * 100 / 8d);
    }

    @Override
    public Holder<Attribute> getAttribute(ModuleContext moduleContext) {
        return NeoForgeMod.SWIM_SPEED;
    }

    @Override
    public double getMaxAttributeAmount() {
        return 1 + getTier() / 8d;
    }

    @Override
    public AttributeModifier.Operation getModifierOperation() {
        return AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
    }

    @Override
    public void appendHoverText(ModuleContext moduleContext, Item.TooltipContext context, List<Component> tooltips,
                                TooltipFlag isAdvanced) {
        super.appendHoverText(moduleContext, context, tooltips, isAdvanced);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.swim_speed",
                GTValues.VNF[getTier()]));
    }
}
