package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.trait.AutoOutputTrait;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.apache.commons.lang3.StringUtils;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class AutoOutputBlockProvider extends MachineTraitProvider<AutoOutputTrait> {

    public AutoOutputBlockProvider() {
        super(GTCEu.id("auto_output_info"), AutoOutputTrait.TYPE);
    }

    @Override
    protected void addTooltip(CompoundTag data, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        if (data.contains("autoOutputItem", Tag.TAG_COMPOUND)) {
            var tag = data.getCompound("autoOutputItem");
            addAutoOutputInfo(tooltip, block, tag, "gtceu.top.item_auto_output");
        }

        if (data.contains("autoOutputFluid", Tag.TAG_COMPOUND)) {
            var tag = data.getCompound("autoOutputFluid");
            addAutoOutputInfo(tooltip, block, tag, "gtceu.top.fluid_auto_output");
        }
    }

    @Override
    protected void write(CompoundTag data, BlockAccessor blockAccessor, AutoOutputTrait trait) {
        if (trait.supportsAutoOutputItems()) {
            var direction = trait.getItemOutputDirection();
            if (direction != null) {
                data.put("autoOutputItem", writeData(new CompoundTag(), direction, blockAccessor,
                        trait.allowsItemInputFromOutputSide(), trait.isAutoOutputItems()));
            }
        }
        if (trait.supportsAutoOutputFluids()) {
            var direction = trait.getFluidOutputDirection();
            if (direction != null) {
                data.put("autoOutputFluid", writeData(new CompoundTag(), direction, blockAccessor,
                        trait.allowsFluidInputFromOutputSide(), trait.isAutoOutputFluids()));
            }
        }
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
