package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;
import com.gregtechceu.gtceu.common.machine.electric.ChargerMachine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

public class BatteryStorageInfoProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getBlockEntity() instanceof IMachineBlockEntity blockEntity) {
            if (blockEntity.getMetaMachine() instanceof ChargerMachine ||
                    blockEntity.getMetaMachine() instanceof BatteryBufferMachine) {
                CompoundTag serverData = blockAccessor.getServerData();
                if (serverData.contains("batteries")) {
                    CustomItemStackHandler handler = new CustomItemStackHandler();
                    handler.deserializeNBT(serverData.getCompound("batteries"));
                    IElementHelper helper = iTooltip.getElementHelper();
                    for (int i = 0; i < handler.getSlots(); i++) {
                        if (handler.getStackInSlot(i).getCount() != 0) {
                            iTooltip.add(helper.smallItem(handler.getStackInSlot(i)));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (blockAccessor.getBlockEntity() instanceof IMachineBlockEntity blockEntity) {
            if (blockEntity.getMetaMachine() instanceof ChargerMachine machine) {
                compoundTag.put("batteries", machine.getChargerInventory().serializeNBT());
            } else if (blockEntity.getMetaMachine() instanceof BatteryBufferMachine machine) {
                compoundTag.put("batteries", machine.getBatteryInventory().serializeNBT());
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return GTCEu.id("battery_info");
    }
}
