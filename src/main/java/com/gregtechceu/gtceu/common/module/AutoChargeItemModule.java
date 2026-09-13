package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.module.*;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.data.GTItemModules;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AutoChargeItemModule extends TieredItemModule {

    // spotless:off
    public static final Codec<AutoChargeItemModule> CODEC = RecordCodecBuilder.create(instance -> tieredBaseCodec(instance).and(
            GlobalPos.CODEC.optionalFieldOf("linked_machine").forGetter(v -> Optional.ofNullable(v.getLinkedMachine()))
    ).apply(instance, AutoChargeItemModule::new));
    //spotless:on

    @Getter
    private @Nullable GlobalPos linkedMachine;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public AutoChargeItemModule(boolean isEnabled, ItemStack attachItem, int tier, Optional<GlobalPos> linkedMachine) {
        super(isEnabled, attachItem, tier);
        this.linkedMachine = linkedMachine.orElse(null);
    }

    public AutoChargeItemModule(boolean isEnabled, ItemStack attachItem, int tier, GlobalPos linkedMachine) {
        super(isEnabled, attachItem, tier);
        this.linkedMachine = linkedMachine;
    }

    public AutoChargeItemModule(ItemStack attachItem, int tier) {
        super(attachItem, tier);
    }

    @Override
    public ItemModuleType<AutoChargeItemModule> type() {
        return GTItemModules.WIRELESS_CHARGER[getTier()];
    }

    @Override
    public Component getInfo() {
        if (getTier() < GTValues.IV)
            return Component.translatable("gtceu.module.wireless_charging", getRange(), GTValues.VNF[getTier()]);
        else return Component.translatable("gtceu.module.wireless_charging.interdimensional", getRange(),
                GTValues.VNF[getTier()], GTValues.VNF[GTValues.IV]);
    }

    @Override
    public void onInventoryTick(Player player) {
        super.onInventoryTick(player);
        long energy = getEnergyToTransfer(player);
        MetaMachine machine = getLinkedMachine(Objects.requireNonNull(player.getServer()));
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(getAppliedTo());
        if (electricItem == null) return;
        if (energy > 0) {
            if (machine instanceof PowerSubstationMachine substation) {
                electricItem.charge(substation.getEnergyBank().drain(energy), electricItem.getTier(), false, false);
            } else if (machine instanceof BatteryBufferMachine charger) {
                electricItem.charge(charger.energyContainer.removeEnergy(energy), charger.getTier(), false, false);
            }
        }
    }

    private long getEnergyToTransfer(Player player) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(getAppliedTo());
        if (electricItem == null) return 0;
        long energy = Math.min(electricItem.getMaxCharge() - electricItem.getCharge(), electricItem.getTransferLimit());
        if (energy <= 0) return 0;
        MetaMachine machine = getLinkedMachine(Objects.requireNonNull(player.getServer()));
        if (machine == null) return 0;
        int interdimensionalTier = -1;
        // ItemModule[] damageBlock = GTItemModules.DAMAGE_BLOCK;
        // for (int i = 0; i < damageBlock.length; i++) {
        // ItemModule shieldModule = damageBlock[i];
        // if (getModularItemStack().getModule(shieldModule) != null)
        // interdimensionalTier = i + 1;
        // }
        interdimensionalTier = Math.min(interdimensionalTier, getTier());
        if (machine.getLevel() != player.level() && interdimensionalTier < GTValues.IV) return 0;
        if (interdimensionalTier < GTValues.IV && machine.getBlockPos().distSqr(player.blockPosition()) > getRange())
            return 0;
        return Math.min(energy, GTValues.V[machine.getLevel() == player.level() ? getTier() : interdimensionalTier]);
    }

    private @Nullable MetaMachine getLinkedMachine(MinecraftServer server) {
        if (linkedMachine == null) return null;
        Level level = server.getLevel(linkedMachine.dimension());
        if (level == null) return null;
        return MetaMachine.getMachine(level, linkedMachine.pos());
    }

    private double getRange() {
        return (8 << getTier());
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips) {
        super.appendHoverText(level, isAdvanced, tooltips);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.wireless_charging", GTValues.VNF[getTier()]));
    }
}
