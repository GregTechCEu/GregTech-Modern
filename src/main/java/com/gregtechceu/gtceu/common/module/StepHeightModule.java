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

public class StepHeightModule extends TieredAttributeItemModule {

    private static final UUID ADD_STEP_HEIGHT_UUID = UUID.fromString("f5bd81ea-b3af-4cca-8866-f3e62f5f68f1");

    public StepHeightModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.step_height", getTier() / 8d);
    }

    @Override
    public Attribute getAttribute(AppliedItemModule module) {
        return ForgeMod.STEP_HEIGHT_ADDITION.get();
    }

    @Override
    public AttributeModifier getAttributeModifier(AppliedItemModule module) {
        double add = getTier() / 8d;
        return new AttributeModifier(ADD_STEP_HEIGHT_UUID, "Step Height Modifier", add,
                AttributeModifier.Operation.ADDITION);
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips,
                                AppliedItemModule module) {
        super.appendHoverText(level, isAdvanced, tooltips, module);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.step_height",
                GTValues.VN[getTier()]));
    }
}
