package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.module.AppliedItemModule;
import com.gregtechceu.gtceu.api.item.module.TieredAttributeItemModule;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;

import java.util.List;
import java.util.UUID;

public class SwimSpeedModule extends TieredAttributeItemModule {

    private static final UUID MUL_SWIM_SPEED_UUID = UUID.fromString("e5bd81ea-b3af-4cca-8866-f3e62f5f68f1");

    public SwimSpeedModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.swim_speed", getTier() * 100 / 8d);
    }

    @Override
    public Attribute getAttribute(AppliedItemModule module) {
        return ForgeMod.SWIM_SPEED.get();
    }

    @Override
    public AttributeModifier getAttributeModifier(AppliedItemModule module) {
        double mul = 1 + getTier() / 8d;
        return new AttributeModifier(MUL_SWIM_SPEED_UUID, "Swim Speed Modifier", mul,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips,
                                AppliedItemModule module) {
        super.appendHoverText(level, isAdvanced, tooltips, module);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.swim_speed",
                GTValues.VNF[getTier()]));
    }
}
