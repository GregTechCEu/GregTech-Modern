package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.api.item.module.TieredAttributeItemModule;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;

public class SwimSpeedModule extends TieredAttributeItemModule {

    private static final UUID MUL_SWIM_SPEED_UUID = UUID.fromString("e5bd81ea-b3af-4cca-8866-f3e62f5f68f1");

    public SwimSpeedModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public double getEnergyCost() {
        return ArmorUtils.getModuleEnergyCost(getTier());
    }

    @Override
    protected boolean consumesEnergyThisTick(ModuleContext context, LivingEntity entity) {
        return entity.isInWater() && !entity.isPassenger() &&
                (ArmorUtils.isMoving(entity) || entity.getDeltaMovement().lengthSqr() > 1.0E-6);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("module.gtceu.swim_speed.description", getMaxAttributeAmount() * 100,
                getEnergyCost());
    }

    @Override
    public Attribute getAttribute(ModuleContext moduleContext) {
        return ForgeMod.SWIM_SPEED.get();
    }

    @Override
    public double getMaxAttributeAmount() {
        return getTier() / 8d;
    }

    @Override
    public AttributeModifier getAttributeModifier(ModuleContext moduleContext) {
        return new AttributeModifier(MUL_SWIM_SPEED_UUID, "Swim Speed Modifier", getMaxAttributeAmount(),
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
