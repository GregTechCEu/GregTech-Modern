package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.api.item.module.TieredAttributeItemModule;

import com.gregtechceu.gtceu.utils.input.SyncedKeyMappings;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class StepHeightModule extends TieredAttributeItemModule {

    public StepHeightModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public Component getInfo() {
        return Component.translatable(getDescriptionLanguageKey(), getTier() / 8d);
    }

    @Override
    public Holder<Attribute> getAttribute(ModuleContext moduleContext) {
        return Attributes.STEP_HEIGHT;
    }

    @Override
    public double getMaxAttributeAmount() {
        return getTier() / 8d;
    }

    @Override
    public AttributeModifier.Operation getModifierOperation() {
        return AttributeModifier.Operation.ADD_VALUE;
    }

    @Override
    public boolean keepAttributeActive(ModuleContext moduleContext, LivingEntity entity) {
        if (!(entity instanceof Player player)) return true;
        return SyncedKeyMappings.STEP_ASSIST_ENABLE.isKeyDown(player);
    }
}
