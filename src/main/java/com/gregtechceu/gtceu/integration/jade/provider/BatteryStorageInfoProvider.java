package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

import static com.gregtechceu.gtceu.utils.GTUtil.formatLongNumber;
import static com.gregtechceu.gtceu.utils.GTUtil.getStringRemainTime;

public class BatteryStorageInfoProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getBlockEntity() instanceof BatteryBufferMachine) {
            CompoundTag serverData = blockAccessor.getServerData();
            if (serverData.contains("batteries")) {
                CompoundTag tag = serverData.getCompound("batteries");
                CompoundTag container = tag.getCompound("energy");
                long changed = container.getLong("changed"), stored = container.getLong("stored"),
                        capacity = container.getLong("capacity");
                iTooltip.add(Component.translatable("gtceu.jade.changes_eu_sec", formatLongNumber(changed)));
                if (changed > 0L) {
                    iTooltip.add(Component
                            .translatable("gtceu.jade.remaining_charge_time",
                                    getStringRemainTime((capacity - stored) / changed)));
                } else if (changed < 0L) {
                    iTooltip.add(Component.translatable("gtceu.jade.remaining_discharge_time",
                            getStringRemainTime((stored) / -changed)));
                }
                if (Minecraft.getInstance().player.isShiftKeyDown()) {
                    CustomItemStackHandler handler = new CustomItemStackHandler();
                    handler.deserializeNBT(tag.getCompound("storage"));
                    IElementHelper helper = iTooltip.getElementHelper();
                    for (int i = 0; i < handler.getSlots(); i++) {
                        if (handler.getStackInSlot(i).getCount() != 0) {
                            ItemStack stack = handler.getStackInSlot(i);
                            iTooltip.add(helper.smallItem(stack));
                            IElectricItem item = GTCapabilityHelper.getElectricItem(stack);
                            if (item == null) continue;
                            iTooltip.append(Component.literal(
                                    GTValues.VNF[item.getTier()] + "§r " + formatLongNumber(item.getCharge()) +
                                            " / " + formatLongNumber(item.getMaxCharge()) + " EU"));
                        }
                    }
                }
            }
        }
    }

    private CompoundTag getEnergyData(IEnergyContainer container) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("changed", container.getInputPerSec() - container.getOutputPerSec());
        tag.putLong("capacity", container.getEnergyCapacity());
        tag.putLong("stored", container.getEnergyStored());
        return tag;
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (blockAccessor.getBlockEntity() instanceof BatteryBufferMachine machine) {
            CompoundTag tag = new CompoundTag();
            tag.put("energy", getEnergyData(machine.energyContainer));
            tag.put("storage", machine.getBatteryInventory().serializeNBT());
            compoundTag.put("batteries", tag);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return GTCEu.id("battery_info");
    }
}
