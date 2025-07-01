package com.gregtechceu.gtceu.integration.top.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.steam.SteamMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import org.jetbrains.annotations.Nullable;

public class RecipeLogicInfoProvider extends CapabilityInfoProvider<RecipeLogic> {

    @Override
    public ResourceLocation getID() {
        return GTCEu.id("recipe_logic_provider");
    }

    @Nullable
    @Override
    protected RecipeLogic getCapability(Level level, BlockPos pos, @Nullable Direction side) {
        return GTCapabilityHelper.getRecipeLogic(level, pos, side);
    }

    @Override
    protected void addProbeInfo(RecipeLogic capability, IProbeInfo probeInfo, Player player, BlockEntity blockEntity,
                                IProbeHitData data) {
        if (capability.isWorking()) {
            var recipe = capability.getLastRecipe();
            if (recipe != null) {
                EnergyStack EUt = RecipeHelper.getRealEUt(recipe);
                // do not show energy usage on machines that do not use energy
                if (EUt.isEmpty()) {
                    return;
                }
                boolean isInput = EUt.voltage() > 0;
                long voltage = Math.abs(EUt.voltage());
                String formatted = FormattingUtil.formatNumbers(voltage * EUt.amperage()) + TextStyleClass.INFO;
                Component text = null;

                if (blockEntity instanceof IMachineBlockEntity machineBlockEntity) {
                    var machine = machineBlockEntity.getMetaMachine();
                    if (machine instanceof SteamMachine) {
                        text = Component.literal(formatted + " mB/t").withStyle(ChatFormatting.GREEN);
                    }
                }

                if (text == null) {
                    text = Component.literal(formatted + " EU/t ").withStyle(ChatFormatting.RED)
                            .append(Component.literal("(").withStyle(ChatFormatting.GREEN))
                            .append(GTValues.VNF[GTUtil.getTierByVoltage(voltage)])
                            .append(Component.literal(")").withStyle(ChatFormatting.GREEN));
                }

                if (isInput) {
                    probeInfo.text(CompoundText.create()
                            .text(Component.translatable("gtceu.top.energy_consumption").append(" ").append(text))
                            .style(TextStyleClass.INFO));
                } else {
                    probeInfo.text(CompoundText.create()
                            .text(Component.translatable("gtceu.top.energy_production").append(" ").append(text))
                            .style(TextStyleClass.INFO));
                }
            }
        }
    }
}
