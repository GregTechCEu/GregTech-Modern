package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;
import com.gregtechceu.gtceu.common.machine.electric.ChargerMachine;

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

import java.math.BigInteger;

import static com.gregtechceu.gtceu.utils.FormattingUtil.DECIMAL_FORMAT_SIC_2F;

public class BatteryStorageInfoProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getBlockEntity() instanceof IMachineBlockEntity blockEntity) {
            if (blockEntity.getMetaMachine() instanceof ChargerMachine ||
                    blockEntity.getMetaMachine() instanceof BatteryBufferMachine) {
                CompoundTag serverData = blockAccessor.getServerData();
                if (serverData.contains("batteries")) {
                    CompoundTag tag = serverData.getCompound("batteries");
                    long changed = tag.getLong("changed"), stored = tag.getLong("stored"),
                            capacity = tag.getLong("capacity");
                    iTooltip.add(Component.translatable("gtceu.jade.changes_eu_sec", changed));
                    if (changed > 0) {
                        iTooltip.add(Component
                                .translatable("gtceu.jade.remaining_charge_time",
                                        getStringRemainTime((capacity - stored) / changed)));
                    } else if (changed < 0) {
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
                                        GTValues.VNF[item.getTier()] + "§r " + formatEnergy(item.getCharge(), 100000) +
                                                " / " + formatEnergy(item.getMaxCharge(), 100000) + " EU"));
                            }
                        }
                    }
                }
            }
        }
    }

    private String getStringRemainTime(long time) {
        String s = Component.translatable("gtceu.jade.seconds", time % 60).getString();
        time /= 60;
        if (time > 0) {
            s = Component.translatable("gtceu.jade.minutes", time % 60).getString() + " " + s;
            time /= 60;
            if (time > 0) {
                s = Component.translatable("gtceu.jade.hours", time % 60).getString() + " " + s;
                time /= 60;
                if (time > 0) {
                    s = Component.translatable("gtceu.jade.days", time % 24).getString() + " " + s;
                    time /= 24;
                    if (time > 0) {
                        s = Component.translatable("gtceu.jade.years", formatEnergy(time, 10000)).getString() + " " + s;
                    }
                }
            }
        }
        return s;
    }

    String formatEnergy(long energy, long trueshold) {
        if (energy > trueshold) return DECIMAL_FORMAT_SIC_2F.format(BigInteger.valueOf(energy));
        else return "" + energy;
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (blockAccessor.getBlockEntity() instanceof IMachineBlockEntity blockEntity) {
            if (blockEntity.getMetaMachine() instanceof ChargerMachine machine) {
                CompoundTag tag = new CompoundTag();
                IEnergyContainer container = machine.energyContainer;
                tag.putLong("changed", container.getInputPerSec() - container.getOutputPerSec());
                tag.putLong("capacity", container.getEnergyCapacity());
                tag.putLong("stored", container.getEnergyStored());
                tag.put("storage", machine.getChargerInventory().serializeNBT());
                compoundTag.put("batteries", tag);
            } else if (blockEntity.getMetaMachine() instanceof BatteryBufferMachine machine) {
                CompoundTag tag = new CompoundTag();
                IEnergyContainer container = machine.energyContainer;
                tag.putLong("changed", container.getInputPerSec() - container.getOutputPerSec());
                tag.putLong("capacity", container.getEnergyCapacity());
                tag.putLong("stored", container.getEnergyStored());
                tag.put("storage", machine.getBatteryInventory().serializeNBT());
                compoundTag.put("batteries", tag);
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return GTCEu.id("battery_info");
    }
}
