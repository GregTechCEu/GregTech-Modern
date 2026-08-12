package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.machine.trait.ProgrammableCircuitSlotTrait;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

public class ProgrammableCircuitProvider extends MachineTraitProvider<ProgrammableCircuitSlotTrait, CompoundTag> {

    public ProgrammableCircuitProvider() {
        super(GTCEu.id("recipe_output_info"), ProgrammableCircuitSlotTrait.class);
    }

    @Override
    protected CompoundTag write(ProgrammableCircuitSlotTrait circuit) {
        CompoundTag data = new CompoundTag();
        if (circuit.isEnabled()) {
            int configuration = circuit.getCurrentCircuit();
            if (configuration > 0) {
                data.putInt("Configuration", configuration);
            }
        }
        return data;
    }

    @Override
    protected void addTooltip(CompoundTag data, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        if (data.contains("Configuration")) {
            IElementHelper helper = tooltip.getElementHelper();

            int configuration = data.getInt("Configuration");
            ItemStack circuit = IntCircuitBehaviour.stack(configuration);

            MutableComponent text = Component.translatable("behaviour.setting.tooltip.circuit_config")
                    .append(Component.literal(Integer.toString(configuration)).withStyle(ChatFormatting.WHITE));

            tooltip.add(helper.smallItem(circuit));
            tooltip.append(text);
        }
    }
}
