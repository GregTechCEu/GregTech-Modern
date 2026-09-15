package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.module.ItemModuleType;
import com.gregtechceu.gtceu.api.item.module.TieredAttributeItemModule;
import com.gregtechceu.gtceu.common.data.GTItemModules;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;

import java.util.List;
import java.util.UUID;

public class MovementSpeedItemModule extends TieredAttributeItemModule {

    private static final UUID MUL_MOVEMENT_SPEED_UUID = UUID.fromString("d5bd81ea-b3af-4cca-8866-f3e62f5f68f1");

    // spotless:off
    public static final Codec<MovementSpeedItemModule> CODEC = tieredAttributeCodec(MovementSpeedItemModule::new);
    //spotless:on

    public MovementSpeedItemModule(boolean isEnabled, ItemStack moduleItem, int tier, double modifierAmount) {
        super(isEnabled, moduleItem, tier, modifierAmount);
    }

    public MovementSpeedItemModule(ItemStack moduleItem, int tier) {
        super(moduleItem, tier);
    }

    @Override
    public ItemModuleType<MovementSpeedItemModule> type() {
        return GTItemModules.MOVEMENT_SPEED_ATTR[getTier()];
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.movement_speed", getTier() * 100 / 8d);
    }

    @Override
    public Attribute getAttribute() {
        return Attributes.MOVEMENT_SPEED;
    }

    @Override
    public AttributeModifier getAttributeModifier() {
        double mul = 1 + getTier() / 8d;
        return new AttributeModifier(MUL_MOVEMENT_SPEED_UUID, "Movement Speed Modifier", mul,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips) {
        super.appendHoverText(level, isAdvanced, tooltips);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.movement_speed",
                GTValues.VNF[getTier()]));
    }
}
