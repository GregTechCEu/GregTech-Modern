package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.block.CableBlock;
import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import static com.gregtechceu.gtceu.utils.FormattingUtil.DECIMAL_FORMAT_1F;

public class CableBlockProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        BlockEntity be = blockAccessor.getBlockEntity();
        if (be != null) {
            CompoundTag data = blockAccessor.getServerData().getCompoundOrEmpty(getUid().toString());
            var cableData = data.getCompound("cableData");
            if (cableData.isPresent()) {
                var tag = cableData.get();
                long voltage = tag.getLongOr("currentVoltage", 0);
                double amperage = tag.getDoubleOr("currentAmperage", 0);
                iTooltip.add(Component.translatable("gtceu.top.cable_voltage"));
                if (voltage != 0) {
                    iTooltip.append(Component.literal(GTValues.VNF[GTUtil.getTierByVoltage(voltage)]));
                    iTooltip.append(Component.literal(" / "));
                }
                iTooltip.append(
                        Component.literal(GTValues.VNF[GTUtil.getTierByVoltage(tag.getLongOr("maxVoltage", 0))]));

                iTooltip.add(Component.translatable("gtceu.top.cable_amperage"));
                if (amperage != 0) {
                    iTooltip.append(Component.literal(DECIMAL_FORMAT_1F.format(amperage) + "A / "));
                }
                iTooltip.append(Component.literal(DECIMAL_FORMAT_1F.format(tag.getDoubleOr("maxAmperage", 0)) + "A"));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        CompoundTag data = compoundTag.getCompoundOrEmpty(getUid().toString());
        if (blockAccessor.getBlock() instanceof CableBlock cableBlock) {
            CableBlockEntity cable = (CableBlockEntity) cableBlock.getPipeTile(blockAccessor.getLevel(),
                    blockAccessor.getPosition());
            if (cable != null) {
                var cableData = new CompoundTag();
                cableData.putLong("maxVoltage", cable.getMaxVoltage());
                cableData.putLong("currentVoltage", cable.getCurrentMaxVoltage());
                cableData.putDouble("maxAmperage", cable.getMaxAmperage());
                cableData.putDouble("currentAmperage", cable.getAverageAmperage());
                data.put("cableData", cableData);
            }
        }
        compoundTag.put(getUid().toString(), data);
    }

    @Override
    public ResourceLocation getUid() {
        return GTJadeIds.toResourceLocation("cable_info");
    }
}
