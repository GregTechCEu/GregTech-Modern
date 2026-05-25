package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.module.AppliedItemModule;
import com.gregtechceu.gtceu.api.item.module.IModularItem;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.TieredItemModule;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.data.GTItemModules;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class AutoChargeItemModule extends TieredItemModule {

    public AutoChargeItemModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public Component getInfo() {
        if (getTier() < GTValues.IV)
            return Component.translatable("gtceu.module.wireless_charging", getRange(), GTValues.VNF[getTier()]);
        else return Component.translatable("gtceu.module.wireless_charging.interdimensional", getRange(),
                GTValues.VNF[getTier()], GTValues.VNF[GTValues.IV]);
    }

    @Override
    public void onInventoryTick(Player player, AppliedItemModule module) {
        super.onInventoryTick(player, module);
        long energy = getEnergyToTransfer(player, module);
        MetaMachine machine = getLinkedMachine(player.getServer(), module);
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(module.getAppliedTo());
        if (electricItem == null) return;
        if (energy > 0) {
            if (machine instanceof PowerSubstationMachine substation) {
                electricItem.charge(substation.getEnergyBank().drain(energy), electricItem.getTier(), false, false);
            } else if (machine instanceof BatteryBufferMachine charger) {
                electricItem.charge(charger.energyContainer.changeEnergy(-energy), charger.getTier(), false, false);
            }
        }
    }

    private long getEnergyToTransfer(Player player, AppliedItemModule module) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(module.getAppliedTo());
        if (electricItem == null) return 0;
        long energy = Math.min(electricItem.getMaxCharge() - electricItem.getCharge(), electricItem.getTransferLimit());
        if (energy <= 0) return 0;
        MetaMachine machine = getLinkedMachine(player.getServer(), module);
        if (machine == null) return 0;
        int interdimensionalTier = -1;
        ItemModule[] damageBlock = GTItemModules.DAMAGE_BLOCK;
        for (int i = 0; i < damageBlock.length; i++) {
            ItemModule shieldModule = damageBlock[i];
            IModularItem modularItem = GTCapabilityHelper.getModularItem(module.getAppliedTo());
            if (modularItem != null && modularItem.getModule(shieldModule) != null)
                interdimensionalTier = i + 1;
        }
        interdimensionalTier = Math.min(interdimensionalTier, getTier());
        if (machine.getLevel() != player.level() && interdimensionalTier < GTValues.IV) return 0;
        if (interdimensionalTier < GTValues.IV && machine.getBlockPos().distSqr(player.blockPosition()) > getRange())
            return 0;
        return Math.min(energy, GTValues.V[machine.getLevel() == player.level() ? getTier() : interdimensionalTier]);
    }

    private MetaMachine getLinkedMachine(MinecraftServer server, AppliedItemModule module) {
        if (!module.getModuleItem().getOrCreateTag().contains("LinkedCharger")) return null;
        CompoundTag tag = module.getModuleItem().getOrCreateTagElement("LinkedCharger");
        int x = tag.getInt("x");
        int y = tag.getInt("y");
        int z = tag.getInt("z");
        if (server == null) return null;
        Level level = server
                .getLevel(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(tag.getString("dim"))));
        if (level == null) return null;
        return MetaMachine.getMachine(level, new BlockPos(x, y, z));
    }

    private double getRange() {
        return (8 << getTier());
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips,
                                AppliedItemModule module) {
        super.appendHoverText(level, isAdvanced, tooltips, module);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.wireless_charging", GTValues.VNF[getTier()]));
    }
}
