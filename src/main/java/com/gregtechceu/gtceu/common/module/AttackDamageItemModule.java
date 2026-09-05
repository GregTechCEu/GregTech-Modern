package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.module.AppliedItemModule;
import com.gregtechceu.gtceu.api.item.module.TieredAttributeItemModule;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

public class AttackDamageItemModule extends TieredAttributeItemModule {

    private static final UUID MUL_DAMAGE_UUID = UUID.fromString("a5bd81ea-b3af-4cca-8866-f3e62f5f68f1");

    public AttackDamageItemModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.attack_damage", getTier() * 100 / 16d);
    }

    @Override
    public Attribute getAttribute(AppliedItemModule module) {
        return Attributes.ATTACK_DAMAGE;
    }

    @Override
    public AttributeModifier getAttributeModifier(AppliedItemModule module) {
        double mul = 1 + getTier() / 16d;
        return new AttributeModifier(MUL_DAMAGE_UUID, "Attack Damage Modifier", mul,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips,
                                AppliedItemModule module) {
        super.appendHoverText(level, isAdvanced, tooltips, module);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.attack_damage",
                GTValues.VNF[getTier()]));
    }
}
