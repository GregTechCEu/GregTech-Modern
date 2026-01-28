package com.gregtechceu.gtceu.integration.top.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.SteamParallelMultiblockMachine;
import com.gregtechceu.gtceu.integration.jade.provider.RecipeLogicProvider;
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
                var EUt = RecipeHelper.getRealEUtWithIO(recipe);
                if (EUt.isEmpty()) {
                    // do not show energy usage on machines that do not use energy
                    return;
                }
                Component text = null;

                if (blockEntity instanceof IMachineBlockEntity machineBlockEntity) {
                    var machine = machineBlockEntity.getMetaMachine();
                    long MBt = 0;
                    if (machine instanceof SimpleSteamMachine ssm) {
                        MBt = (long) Math.ceil(EUt.getTotalEU() * ssm.getConversionRate());
                    } else if (machine instanceof SteamParallelMultiblockMachine smb) {
                        MBt = (long) Math.ceil(EUt.getTotalEU() * smb.getConversionRate());
                    }
                    if (machine instanceof SteamMachine) {
                        text = Component.translatable("gtceu.jade.fluid_use",
                                FormattingUtil.formatNumbers(MBt) + TextStyleClass.INFO)
                                .withStyle(ChatFormatting.GREEN);
                    }
                }

                if (text == null) {
                    var tier = GTUtil.getTierByVoltage(RecipeLogicProvider.getVoltage(capability));
                    String minAmperage = FormattingUtil
                            .formatNumber2Places((float) (EUt.getTotalEU()) / GTValues.V[tier]) + TextStyleClass.INFO;

                    text = Component.translatable("gtceu.jade.amperage_use", minAmperage).withStyle(ChatFormatting.RED)
                            .append(Component.translatable("gtceu.jade.at").withStyle(ChatFormatting.GREEN))
                            .append(GTValues.VNF[tier])
                            .append(Component.translatable("gtceu.universal.padded_parentheses",
                                    (Component.translatable("gtceu.recipe.eu.total",
                                            FormattingUtil.formatNumbers(EUt.getTotalEU()) + TextStyleClass.INFO)))
                                    .withStyle(ChatFormatting.WHITE));
                }

                if (EUt.isInput()) {
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
