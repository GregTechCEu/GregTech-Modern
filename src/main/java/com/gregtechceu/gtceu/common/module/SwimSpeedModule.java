package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.module.ItemModuleType;
import com.gregtechceu.gtceu.api.item.module.TieredAttributeItemModule;
import com.gregtechceu.gtceu.common.data.GTItemModules;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;

import com.mojang.serialization.Codec;

import java.util.List;
import java.util.UUID;

public class SwimSpeedModule extends TieredAttributeItemModule {

    private static final UUID MUL_SWIM_SPEED_UUID = UUID.fromString("e5bd81ea-b3af-4cca-8866-f3e62f5f68f1");

    // spotless:off
    public static final Codec<SwimSpeedModule> CODEC = tieredAttributeCodec(SwimSpeedModule::new);
    //spotless:on

    public SwimSpeedModule(boolean isEnabled, ItemStack moduleItem, int tier, double modifierAmount) {
        super(isEnabled, moduleItem, tier, modifierAmount);
    }

    public SwimSpeedModule(ItemStack moduleItem, int tier) {
        super(moduleItem, tier);
    }

    @Override
    public ItemModuleType<SwimSpeedModule> type() {
        return GTItemModules.SWIM_SPEED[getTier()];
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.swim_speed", getTier() * 100 / 8d);
    }

    @Override
    public Attribute getAttribute() {
        return ForgeMod.SWIM_SPEED.get();
    }

    @Override
    public AttributeModifier getAttributeModifier() {
        double mul = 1 + getTier() / 8d;
        return new AttributeModifier(MUL_SWIM_SPEED_UUID, "Swim Speed Modifier", mul,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips) {
        super.appendHoverText(level, isAdvanced, tooltips);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.swim_speed",
                GTValues.VNF[getTier()]));
    }
}
