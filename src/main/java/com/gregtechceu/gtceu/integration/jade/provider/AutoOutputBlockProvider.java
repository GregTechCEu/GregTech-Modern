package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputFluid;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.apache.commons.lang3.StringUtils;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class AutoOutputBlockProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        BlockEntity be = blockAccessor.getBlockEntity();
        if (be != null) {
            CompoundTag data = blockAccessor.getServerData().getCompound(getUid().toString());
            if (data.contains("autoOutputItem", Tag.TAG_COMPOUND)) {
                var tag = data.getCompound("autoOutputItem");
                addAutoOutputInfo(iTooltip, blockAccessor, tag, "gtceu.top.item_auto_output");
            }

            if (data.contains("autoOutputFluid", Tag.TAG_COMPOUND)) {
                var tag = data.getCompound("autoOutputFluid");
                addAutoOutputInfo(iTooltip, blockAccessor, tag, "gtceu.top.fluid_auto_output");
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        CompoundTag data = compoundTag.getCompound(getUid().toString());
        var level = blockAccessor.getLevel();
        var pos = blockAccessor.getPosition();
        if (MetaMachine.getMachine(level, pos) instanceof IAutoOutputItem outputItem) {
            var direction = outputItem.getOutputFacingItems();
            if (direction != null) {
                data.put("autoOutputItem", writeData(new CompoundTag(), direction, blockAccessor,
                        outputItem.isAllowInputFromOutputSideItems(), outputItem.isAutoOutputItems()));
            }
        }
        if (MetaMachine.getMachine(level, pos) instanceof IAutoOutputFluid outputFluid) {
            var direction = outputFluid.getOutputFacingFluids();
            if (direction != null) {
                data.put("autoOutputFluid", writeData(new CompoundTag(), direction, blockAccessor,
                        outputFluid.isAllowInputFromOutputSideFluids(), outputFluid.isAutoOutputFluids()));
            }
        }
        compoundTag.put(getUid().toString(), data);
    }

    @Override
    public ResourceLocation getUid() {
        return GTCEu.id("auto_output_info");
    }

    private CompoundTag writeData(CompoundTag compoundTag, Direction direction, BlockAccessor blockAccessor,
                                  boolean allowInput, boolean auto) {
        compoundTag.putString("direction", direction.getName());
        var level = blockAccessor.getLevel();
        var pos = blockAccessor.getPosition().relative(direction);
        if (level != null) {
            var key = BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock());
            compoundTag.putString("block", key.toString());
        }
        compoundTag.putBoolean("allowInput", allowInput);
        compoundTag.putBoolean("auto", auto);
        return compoundTag;
    }

    private void addAutoOutputInfo(ITooltip iTooltip, BlockAccessor blockAccessor, CompoundTag compoundTag,
                                   String text) {
        var direction = Direction.byName(compoundTag.getString("direction"));
        boolean allowInput = compoundTag.getBoolean("allowInput");
        boolean auto = compoundTag.getBoolean("auto");
        if (direction != null) {
            iTooltip.add(Component.translatable(text, StringUtils.capitalize(direction.getName())));
            if (blockAccessor.showDetails()) {
                var block = BuiltInRegistries.BLOCK.get(new ResourceLocation(compoundTag.getString("block"))).asItem()
                        .getDefaultInstance();
                if (!block.isEmpty()) {
                    iTooltip.append(iTooltip.getElementHelper().smallItem(block));
                }
            }

            if (allowInput || auto) {
                var component = Component.literal(" (");
                if (auto) {
                    component.append(Component.translatable("gtceu.top.auto_output"));
                }

                if (allowInput && auto) {
                    component.append("/");
                }

                if (allowInput) {
                    component.append(Component.translatable("gtceu.top.allow_output_input"));
                }
                component.append(")");
                iTooltip.append(component);
            }
        }
    }
}
