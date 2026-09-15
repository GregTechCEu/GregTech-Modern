package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.module.ITieredItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModuleType;
import com.gregtechceu.gtceu.common.data.GTItemModules;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;

import java.util.List;

public class CreativeFlightModule extends ItemModule implements ITieredItemModule {

    public static final Codec<CreativeFlightModule> CODEC = simpleCodec(CreativeFlightModule::new);

    public CreativeFlightModule(boolean isEnabled, ItemStack moduleItem) {
        super(isEnabled, moduleItem);
    }

    public CreativeFlightModule(ItemStack moduleItem) {
        super(moduleItem);
    }

    @Override
    public ItemModuleType<CreativeFlightModule> type() {
        return GTItemModules.CREATIVE_FLIGHT;
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.creative_flight", 2048);
    }

    private void setMayFly(LivingEntity entity, boolean mayFly) {
        if (entity instanceof Player player) {
            player.getAbilities().mayfly = mayFly;
            if (!mayFly) player.getAbilities().flying = false;
        }
    }

    private boolean isFlying(LivingEntity entity) {
        if (entity instanceof Player player) {
            return player.getAbilities().flying;
        } else return false;
    }

    @Override
    public void onEquip(LivingEntity entity) {
        super.onEquip(entity);
        setMayFly(entity, true);
    }

    @Override
    public void onUnequip(LivingEntity entity) {
        super.onUnequip(entity);
        setMayFly(entity, false);
    }

    @Override
    public void onArmorTick(LivingEntity entity) {
        super.onArmorTick(entity);
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(getAppliedTo());
        if (electricItem == null || !isFlying(entity)) return;
        if (!electricItem.canUse(2048)) setMayFly(entity, false);
        else {
            electricItem.discharge(2048, electricItem.getTier(), true, false, false);
            setMayFly(entity, true);
        }
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips) {
        super.appendHoverText(level, isAdvanced, tooltips);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.creative_flight")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    @Override
    public int getTier() {
        return GTValues.IV;
    }
}
