package com.gregtechceu.gtceu.integration.top.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.steam.SteamMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

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
        // do not show energy usage on machines that do not use energy
        if (capability.isWorking()) {
            // TODO PrimitiveRecipeLogic
            // if (capability instanceof PrimitiveRecipeLogic) {
            // return; // do not show info for primitive machines, as they are supposed to appear powerless
            // }
            var recipe = capability.getLastRecipe();
            if (recipe != null) {
                var EUt = RecipeHelper.getInputEUt(recipe);
                var isInput = true;
                if (EUt == 0) {
                    isInput = false;
                    EUt = RecipeHelper.getOutputEUt(recipe);
                }
                long absEUt = Math.abs(EUt);
                String text = null;

                if (blockEntity instanceof IMachineBlockEntity machineBlockEntity) {
                    var machine = machineBlockEntity.getMetaMachine();
                    if (machine instanceof SteamMachine) {
                        text = ChatFormatting.RED.toString() + absEUt + TextStyleClass.INFO + " mB/t " +
                                LocalizationUtils.format("material.steam");
                    }
                }

                if (text == null) {
                    // Default behavior, if this TE is not a steam machine (or somehow not instanceof
                    // IGregTechBlockEntity...)
                    text = ChatFormatting.RED.toString() + absEUt + TextStyleClass.INFO + " EU/t" +
                            ChatFormatting.GREEN + " (" + GTValues.VNF[GTUtil.getTierByVoltage(absEUt)] +
                            ChatFormatting.GREEN + ")";
                }

                if (EUt > 0) {
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
}
