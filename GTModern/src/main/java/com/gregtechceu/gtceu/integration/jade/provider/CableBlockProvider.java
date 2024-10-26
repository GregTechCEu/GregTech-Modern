package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.block.CableBlock;
import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
            CompoundTag data = blockAccessor.getServerData().getCompound(getUid().toString());
            if (data.contains("cableData", Tag.TAG_COMPOUND)) {
                var tag = data.getCompound("cableData");
                long voltage = tag.getLong("currentVoltage");
                double amperage = tag.getDouble("currentAmperage");
                iTooltip.add(Component.translatable("gtceu.top.cable_voltage"));
                if (voltage != 0) {
                    iTooltip.append(Component.literal(GTValues.VNF[GTUtil.getTierByVoltage(voltage)]));
                    iTooltip.append(Component.literal(" / "));
                }
                iTooltip.append(Component.literal(GTValues.VNF[GTUtil.getTierByVoltage(tag.getLong("maxVoltage"))]));

                iTooltip.add(Component.translatable("gtceu.top.cable_amperage"));
                if (amperage != 0) {
                    iTooltip.append(Component.literal(DECIMAL_FORMAT_1F.format(amperage) + "A / "));
                }
                iTooltip.append(Component.literal(DECIMAL_FORMAT_1F.format(tag.getDouble("maxAmperage")) + "A"));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        CompoundTag data = compoundTag.getCompound(getUid().toString());
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
        return GTCEu.id("cable_info");
    }
}
