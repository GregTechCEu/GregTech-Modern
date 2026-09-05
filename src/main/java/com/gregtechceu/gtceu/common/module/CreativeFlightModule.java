package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.module.AppliedItemModule;
import com.gregtechceu.gtceu.api.item.module.ITieredItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModule;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class CreativeFlightModule extends ItemModule implements ITieredItemModule {

    public CreativeFlightModule(ResourceLocation id) {
        super(id);
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
    public void onEquip(LivingEntity entity, AppliedItemModule module) {
        super.onEquip(entity, module);
        setMayFly(entity, true);
    }

    @Override
    public void onUnequip(LivingEntity entity, AppliedItemModule module) {
        super.onUnequip(entity, module);
        setMayFly(entity, false);
    }

    @Override
    public void onArmorTick(LivingEntity entity, AppliedItemModule module) {
        super.onArmorTick(entity, module);
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(module.getAppliedTo());
        if (electricItem == null || !isFlying(entity)) return;
        if (!electricItem.canUse(2048)) setMayFly(entity, false);
        else {
            electricItem.discharge(2048, electricItem.getTier(), true, false, false);
            setMayFly(entity, true);
        }
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips,
                                AppliedItemModule module) {
        super.appendHoverText(level, isAdvanced, tooltips, module);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.creative_flight")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    @Override
    public int getTier() {
        return GTValues.IV;
    }
}
