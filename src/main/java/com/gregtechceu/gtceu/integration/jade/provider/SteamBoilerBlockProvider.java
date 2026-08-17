package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class SteamBoilerBlockProvider extends MachineInfoProvider<SteamBoilerMachine, CompoundTag> {

    public SteamBoilerBlockProvider() {
        super(GTCEu.id("steam_boiler_info"), SteamBoilerMachine.class);
    }

    @Override
    protected CompoundTag write(SteamBoilerMachine machine) {
        CompoundTag data = new CompoundTag();
        RecipeLogic recipeLogic = machine.getTraitOptional(RecipeLogic.class).orElseThrow();
        data.putBoolean("isBurning", recipeLogic.isWorking() || recipeLogic.isWaiting());
        data.putBoolean("hasWater", !machine.isHasNoWater());
        data.putLong("steamProduction", machine.getTotalSteamOutput());
        data.putInt("currentTemperature", machine.getCurrentTemperature());
        data.putInt("maxTemperature", machine.getMaxTemperature());
        int fuelConsumptionRate = Mth.ceil(machine.getEffectiveFuelConsumptionRate() * 100.0f);
        data.putInt("fuelConsumptionRate", fuelConsumptionRate);
        return data;
    }

    @Override
    protected void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        boolean isBurning = capData.getBoolean("isBurning");
        boolean hasWater = capData.getBoolean("hasWater");
        long production = capData.getLong("steamProduction");
        int temperature = capData.getInt("currentTemperature");
        int maxTemperature = capData.getInt("maxTemperature");
        int fuelConsumptionRate = capData.getInt("fuelConsumptionRate");

        boolean makingSteam = hasWater && temperature >= 100;

        // Append fuel consumption discount first
        if (fuelConsumptionRate < 100) {
            // Round up to a nicer number. precision less than 5% is irrelevant here
            int fuelConsumptionDiscount = Math.round((100.0f - fuelConsumptionRate) / 5.0f) * 5;
            tooltip.add(Component.translatable("gtceu.machine.boiler.info.fuel_consumption_discount",
                    fuelConsumptionDiscount));
        }

        // Determine the first section
        MutableComponent root;
        if (isBurning && temperature >= maxTemperature) {
            // Fully heated
            root = Component.translatable("gtceu.machine.boiler.info.heated_up");
        } else if (isBurning) {
            // Heating up
            root = Component.translatable("gtceu.machine.boiler.info.heating.up");
        } else if (!isBurning && temperature > 0) {
            // Cooling down
            root = Component.translatable("gtceu.machine.boiler.info.cooling.down");
        } else {
            root = null; // neither heating nor cooling, is either max temperature or temperature of zero
        }

        // Determine the second section
        MutableComponent extra;
        if (makingSteam) {
            // Producing some amount of steam
            extra = Component.translatable("gtceu.machine.boiler.info.production.data",
                    Component.literal(FormattingUtil.formatNumbers(production / 10))
                            .withStyle(ChatFormatting.GREEN));
            if (root != null) {
                // append some nice separation here to the root
                extra = Component.literal(" | ").append(extra);
            }
        } else if (temperature > 0 && temperature < 100) {
            // Still warming up (or cooling down)
            extra = Component.literal(String.format(" (%s%%)", temperature))
                    // Either heating up or cooling down
                    .withStyle(isBurning ? ChatFormatting.RED : ChatFormatting.BLUE);
        } else {
            // Nothing to add
            extra = null;
        }

        if (root != null && extra != null) {
            tooltip.add(root.append(extra));
        } else if (root != null) {
            tooltip.add(root);
        } else if (extra != null) {
            tooltip.add(extra);
        }
    }
}
