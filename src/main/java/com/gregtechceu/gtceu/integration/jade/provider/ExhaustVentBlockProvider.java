package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.machine.trait.ExhaustVentMachineTrait;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.apache.commons.lang3.StringUtils;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class ExhaustVentBlockProvider extends MachineTraitProvider<ExhaustVentMachineTrait, CompoundTag> {

    public ExhaustVentBlockProvider() {
        super(GTCEu.id("exhaust_vent_info"), ExhaustVentMachineTrait.TYPE);
    }

    @Override
    protected CompoundTag write(ExhaustVentMachineTrait trait) {
        var compoundTag = new CompoundTag();
        var pos = trait.getBlockPos().relative(trait.getVentingDirection());
        var key = BuiltInRegistries.BLOCK.getKey(trait.getLevel().getBlockState(pos).getBlock());

        compoundTag.putString("ventDirection", trait.getVentingDirection().getName());
        compoundTag.putString("ventBlock", key.toString());
        compoundTag.putBoolean("ventBlocked", trait.isVentingBlocked());
        compoundTag.putBoolean("needsVenting", trait.isNeedsVenting());
        return compoundTag;
    }

    @Override
    protected void addTooltip(CompoundTag compoundTag, ITooltip iTooltip, Player player, BlockAccessor blockAccessor,
                              BlockEntity blockEntity, IPluginConfig iPluginConfig) {
        var direction = Direction.byName(compoundTag.getString("ventDirection"));
        if (direction != null) {
            iTooltip.add(Component.translatable("gtceu.top.exhaust_vent_direction",
                    StringUtils.capitalize(direction.getName())));
            if (!compoundTag.getBoolean("ventBlocked")) return;

            if (blockAccessor.showDetails()) {
                var block = BuiltInRegistries.BLOCK.get(new ResourceLocation(compoundTag.getString("ventBlock")))
                        .asItem().getDefaultInstance();
                iTooltip.append(iTooltip.getElementHelper().smallItem(block));
            }

            if (compoundTag.getBoolean("needsVenting")) {
                iTooltip.append(Component.literal(" ("));
                iTooltip.append(Component.translatable("gtceu.top.exhaust_vent_blocked").withStyle(ChatFormatting.RED)
                        .append(Component.literal(")").withStyle(ChatFormatting.GRAY)));
            }
        }
    }
}
