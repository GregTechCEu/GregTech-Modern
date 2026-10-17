package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.api.item.module.TieredAttributeItemModule;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class AttackDamageItemModule extends TieredAttributeItemModule {

    private static final UUID MUL_DAMAGE_UUID = UUID.fromString("a5bd81ea-b3af-4cca-8866-f3e62f5f68f1");

    public AttackDamageItemModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("module.gtceu.attack_damage.description", getTier() * 100 / 16d);
    }

    @Override
    public Attribute getAttribute(ModuleContext moduleContext) {
        return Attributes.ATTACK_DAMAGE;
    }

    @Override
    public double getMaxAttributeAmount() {
        return 1 + getTier() / 16d;
    }

    @Override
    public AttributeModifier getAttributeModifier(ModuleContext moduleContext) {
        return new AttributeModifier(MUL_DAMAGE_UUID, "Attack Damage Modifier", getMaxAttributeAmount(),
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
