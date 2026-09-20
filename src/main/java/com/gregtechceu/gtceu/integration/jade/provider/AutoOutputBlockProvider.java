package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.machine.trait.AutoOutputTrait;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.apache.commons.lang3.StringUtils;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class AutoOutputBlockProvider extends MachineTraitProvider<AutoOutputTrait, CompoundTag> {

    public AutoOutputBlockProvider() {
        super(GTCEu.id("auto_output_info"), AutoOutputTrait.class);
    }

    @Override
    protected void addTooltip(CompoundTag data, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        if ((data.get("autoOutputItem") instanceof CompoundTag)) {
            var tag = data.getCompoundOrEmpty("autoOutputItem");
            addAutoOutputInfo(tooltip, block, tag, "gtceu.top.item_auto_output");
        }

        if ((data.get("autoOutputFluid") instanceof CompoundTag)) {
            var tag = data.getCompoundOrEmpty("autoOutputFluid");
            addAutoOutputInfo(tooltip, block, tag, "gtceu.top.fluid_auto_output");
        }
    }

    @Override
    protected CompoundTag write(AutoOutputTrait trait) {
        var data = new CompoundTag();
        if (trait.supportsAutoOutputItems()) {
            var direction = trait.getItemOutputDirection();
            if (direction != null) {
                data.put("autoOutputItem",
                        writeData(new CompoundTag(), direction, trait.getLevel(), trait.getBlockPos(),
                                trait.allowsItemInputFromOutputSide(), trait.isAutoOutputItems()));
            }
        }
        if (trait.supportsAutoOutputFluids()) {
            var direction = trait.getFluidOutputDirection();
            if (direction != null) {
                data.put("autoOutputFluid",
                        writeData(new CompoundTag(), direction, trait.getLevel(), trait.getBlockPos(),
                                trait.allowsFluidInputFromOutputSide(), trait.isAutoOutputFluids()));
            }
        }
        return data;
    }

    private CompoundTag writeData(CompoundTag compoundTag, Direction direction, Level lvl, BlockPos pos,
                                  boolean allowInput, boolean auto) {
        compoundTag.putString("direction", direction.getName());
        var key = BuiltInRegistries.BLOCK.getKey(lvl.getBlockState(pos).getBlock());
        compoundTag.putString("block", key.toString());
        compoundTag.putBoolean("allowInput", allowInput);
        compoundTag.putBoolean("auto", auto);
        return compoundTag;
    }

    private void addAutoOutputInfo(ITooltip iTooltip, BlockAccessor blockAccessor, CompoundTag compoundTag,
                                   String text) {
        var direction = Direction.byName(compoundTag.getStringOr("direction", ""));
        boolean allowInput = compoundTag.getBooleanOr("allowInput", false);
        boolean auto = compoundTag.getBooleanOr("auto", false);
        if (direction != null) {
            iTooltip.add(Component.translatable(text, StringUtils.capitalize(direction.getName())));
            if (blockAccessor.showDetails()) {
                var block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(compoundTag.getStringOr("block", ""))).asItem()
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
