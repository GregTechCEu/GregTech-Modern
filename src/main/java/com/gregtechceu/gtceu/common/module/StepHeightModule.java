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

public class StepHeightModule extends TieredAttributeItemModule {

    private static final UUID ADD_STEP_HEIGHT_UUID = UUID.fromString("f5bd81ea-b3af-4cca-8866-f3e62f5f68f1");

    // spotless:off
    public static final Codec<StepHeightModule> CODEC = tieredAttributeCodec(StepHeightModule::new);
    //spotless:on

    public StepHeightModule(boolean isEnabled, ItemStack moduleItem, int tier, double modifierAmount) {
        super(isEnabled, moduleItem, tier, modifierAmount);
    }

    public StepHeightModule(ItemStack moduleItem, int tier) {
        super(moduleItem, tier);
    }

    @Override
    public ItemModuleType<StepHeightModule> type() {
        return GTItemModules.STEP_HEIGHT[getTier()];
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.step_height", getTier() / 8d);
    }

    @Override
    public Attribute getAttribute() {
        return ForgeMod.STEP_HEIGHT_ADDITION.get();
    }

    @Override
    public AttributeModifier getAttributeModifier() {
        double add = getTier() / 8d;
        return new AttributeModifier(ADD_STEP_HEIGHT_UUID, "Step Height Modifier", add,
                AttributeModifier.Operation.ADDITION);
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips) {
        super.appendHoverText(level, isAdvanced, tooltips);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.step_height",
                GTValues.VN[getTier()]));
    }
}
