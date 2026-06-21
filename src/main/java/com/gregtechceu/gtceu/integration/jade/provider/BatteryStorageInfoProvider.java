package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

import static com.gregtechceu.gtceu.utils.GTUtil.formatLongNumber;
import static com.gregtechceu.gtceu.utils.GTUtil.getStringRemainTime;

public class BatteryStorageInfoProvider extends MachineInfoProvider<BatteryBufferMachine, CompoundTag> {

    public BatteryStorageInfoProvider() {
        super(GTCEu.id("battery_info"), BatteryBufferMachine.class);
    }

    @Override
    protected CompoundTag write(BatteryBufferMachine machine) {
        CompoundTag tag = new CompoundTag();
        tag.put("energy", getEnergyData(machine.energyContainer));
        tag.put("storage", machine.getBatteryInventory().serializeNBT());
        return tag;
    }

    private CompoundTag getEnergyData(IEnergyContainer container) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("changed", container.getInputPerSec() - container.getOutputPerSec());
        tag.putLong("capacity", container.getEnergyCapacity());
        tag.putLong("stored", container.getEnergyStored());
        return tag;
    }

    @Override
    protected void addTooltip(CompoundTag data, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        CompoundTag container = data.getCompound("energy");
        long changed = container.getLong("changed"), stored = container.getLong("stored"),
                capacity = container.getLong("capacity");
        tooltip.add(Component.translatable("gtceu.jade.changes_eu_sec", formatLongNumber(changed)));
        if (changed > 0L) {
            tooltip.add(Component
                    .translatable("gtceu.jade.remaining_charge_time",
                            getStringRemainTime((capacity - stored) / changed)));
        } else if (changed < 0L) {
            tooltip.add(Component.translatable("gtceu.jade.remaining_discharge_time",
                    getStringRemainTime((stored) / -changed)));
        }
        if (GTUtil.isShiftDown()) {
            CustomItemStackHandler handler = new CustomItemStackHandler();
            handler.deserializeNBT(data.getCompound("storage"));
            IElementHelper helper = tooltip.getElementHelper();
            for (int i = 0; i < handler.getSlots(); i++) {
                if (handler.getStackInSlot(i).getCount() != 0) {
                    ItemStack stack = handler.getStackInSlot(i);
                    tooltip.add(helper.smallItem(stack));
                    IElectricItem item = GTCapabilityHelper.getElectricItem(stack);
                    if (item == null) continue;
                    tooltip.append(Component.literal(
                            GTValues.VNF[item.getTier()] + "§r " + formatLongNumber(item.getCharge()) +
                                    " / " + formatLongNumber(item.getMaxCharge()) + " EU"));
                }
            }
        }
    }
}
