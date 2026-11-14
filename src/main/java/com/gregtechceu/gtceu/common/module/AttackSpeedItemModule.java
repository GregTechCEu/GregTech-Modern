package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.api.item.module.TieredAttributeItemModule;
import com.gregtechceu.gtceu.utils.lang.FormattingUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class AttackSpeedItemModule extends TieredAttributeItemModule {

    private static final UUID MUL_ATTACK_SPEED_UUID = UUID.fromString("b5bd81ea-b3af-4cca-8866-f3e62f5f68f1");

    public AttackSpeedItemModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public double getEnergyCost() {
        return ArmorUtils.getModuleEnergyCost(getTier());
    }

    @Override
    public Component getInfo() {
        return Component.translatable(getDescriptionLanguageKey(), getMaxAttributeAmount() * 100,
                FormattingUtil.formatNumbers(getEnergyCost()));
    }

    @Override
    public Attribute getAttribute(ModuleContext moduleContext) {
        return Attributes.ATTACK_SPEED;
    }

    @Override
    public double getMaxAttributeAmount() {
        return getTier() / 32d;
    }

    @Override
    public AttributeModifier getAttributeModifier(ModuleContext moduleContext) {
        return new AttributeModifier(MUL_ATTACK_SPEED_UUID, "Attack Speed Modifier", getMaxAttributeAmount(),
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
